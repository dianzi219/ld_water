package com.fastbee.oss.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.exception.GlobalException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.file.FileUtils;
import com.fastbee.oss.service.OssClient;
import com.fastbee.oss.entity.UploadResult;
import com.fastbee.oss.enums.AccessPolicyType;
import com.fastbee.oss.service.OssFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.fastbee.oss.mapper.OssDetailMapper;
import com.fastbee.oss.domain.OssDetail;
import com.fastbee.oss.service.IOssDetailService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 文件记录Service业务层处理
 *
 * @author zhuangpeng.li
 * @date 2024-04-19
 */
@Service
public class OssDetailServiceImpl implements IOssDetailService {
    @Autowired
    private OssDetailMapper ossDetailMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 查询文件记录
     *
     * @param id 文件记录主键
     * @return 文件记录
     */
    @Override
    public OssDetail selectOssDetailById(Integer id) {
        return ossDetailMapper.selectOssDetailById(id);
    }

    /**
     * 查询文件记录列表
     *
     * @param ossDetail 文件记录
     * @return 文件记录
     */
    @Override
    public List<OssDetail> selectOssDetailList(OssDetail ossDetail) {
        SysUser user = getLoginUser().getUser();
        ossDetail.setTenantId(user.getDept().getDeptUserId());
        return ossDetailMapper.selectOssDetailList(ossDetail);
    }

    /**
     * 新增文件记录
     *
     * @param ossDetail 文件记录
     * @return 结果
     */
    @Override
    public int insertOssDetail(OssDetail ossDetail) {
        SysUser user = getLoginUser().getUser();
        ossDetail.setTenantId(user.getDept().getDeptUserId());
        ossDetail.setCreateTime(DateUtils.getNowDate());
        return ossDetailMapper.insertOssDetail(ossDetail);
    }

    /**
     * 修改文件记录
     *
     * @param ossDetail 文件记录
     * @return 结果
     */
    @Override
    public int updateOssDetail(OssDetail ossDetail) {
        ossDetail.setUpdateTime(DateUtils.getNowDate());
        return ossDetailMapper.updateOssDetail(ossDetail);
    }

    /**
     * 批量删除文件记录
     *
     * @param ids 需要删除的文件记录主键
     * @return 结果
     */
    @Override
    public int deleteOssDetailByIds(Integer[] ids) {
        OssClient storage = OssFactory.instance(redisCache);
        for (Integer id : ids) {
            OssDetail detail = selectOssDetailById(id);
            if (detail != null) {
                storage.delete(detail.getUrl());
                return ossDetailMapper.deleteOssDetailById(id);
            } else {
                return -1;
            }
        }
        return -1;
    }

    /**
     * 删除文件记录信息
     *
     * @param id 文件记录主键
     * @return 结果
     */
    @Override
    public int deleteOssDetailById(Integer id) {
        OssDetail detail = selectOssDetailById(id);
        if (detail != null) {
            OssClient storage = OssFactory.instance(redisCache);
            storage.delete(detail.getUrl());
            return ossDetailMapper.deleteOssDetailById(id);
        } else {
            return -1;
        }
    }

    @Override
    public OssDetail upload(MultipartFile file) {
        String originalfileName = file.getOriginalFilename();
        String suffix = StringUtils.substring(originalfileName, originalfileName.lastIndexOf("."), originalfileName.length());
        OssClient storage = OssFactory.instance(redisCache);
        UploadResult uploadResult;
        try {
            uploadResult = storage.uploadSuffix(file.getBytes(), suffix, file.getContentType());
        } catch (IOException e) {
            throw new GlobalException(e.getMessage());
        }
        // 保存文件信息
        return buildResultEntity(originalfileName, suffix, storage.getConfigKey(), uploadResult);
    }

    @Override
    public OssDetail upload(File file) {
        String originalfileName = file.getName();
        String suffix = StringUtils.substring(originalfileName, originalfileName.lastIndexOf("."), originalfileName.length());
        OssClient storage = OssFactory.instance(redisCache);
        UploadResult uploadResult = storage.uploadSuffix(file, suffix);
        // 保存文件信息
        return buildResultEntity(originalfileName, suffix, storage.getConfigKey(), uploadResult);
    }

    private OssDetail buildResultEntity(String originalfileName, String suffix, String configKey, UploadResult uploadResult) {
        OssDetail oss = OssDetail.builder()
                .url(uploadResult.getUrl())
                .fileSuffix(suffix)
                .fileName(uploadResult.getFilename())
                .originalName(originalfileName)
                .service(configKey)
                .build();
        this.insertOssDetail(oss);
        return this.matchingUrl(oss);
    }

    private OssDetail matchingUrl(OssDetail oss) {
        OssClient storage = OssFactory.instance(oss.getService(), redisCache);
        // 仅修改桶类型为 private 的URL，临时URL时长为120s
        if (AccessPolicyType.PRIVATE == storage.getAccessPolicy()) {
            oss.setUrl(storage.getPrivateUrl(oss.getFileName(), 120));
        }
        return oss;
    }

    @Override
    public void download(Integer ossId, HttpServletResponse response) throws IOException {
        OssDetail sysOss = ossDetailMapper.selectOssDetailById(ossId);
        if (ObjectUtil.isNull(sysOss)) {
            throw new GlobalException("文件数据不存在!");
        }
        FileUtils.setAttachmentResponseHeader(response, sysOss.getOriginalName());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=UTF-8");
        OssClient storage = OssFactory.instance(sysOss.getService(), redisCache);
        try (InputStream inputStream = storage.getObjectContent(sysOss.getUrl())) {
            int available = inputStream.available();
            IoUtil.copy(inputStream, response.getOutputStream(), available);
            response.setContentLength(available);
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }
}
