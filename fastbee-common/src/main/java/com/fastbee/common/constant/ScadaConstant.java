package com.fastbee.common.constant;

/**
 * @author fastb
 * @version 1.0
 * @description: 组态常量类
 * @date 2024-01-02 14:40
 */
public class ScadaConstant {

    public static final String COMPONENT_TEMPLATE_DEFAULT = "<div id=\"app\" class=\"h2-text\">\n    <h2>自定义组件案例</h2>\n    <h4>支持element ui、样式自定义、vue的语法等</h4>\n    <el-button type=\"primary\" @click=\"handleClick\">点击按钮</el-button>\n</div>";
    public static final String COMPONENT_SCRIPT_DEFAULT = "export default {\n    data() {\n        return {}\n    },\n    created() {\n\n    },\n    mounted(){\n\n    },\n    methods:{\n        handleClick(){\n             this.$message('这是一条消息提示');\n        }\n    }\n}";
    public static final String COMPONENT_STYLE_DEFAULT = "h2 {\n    color:#409EFF\n}\n\nh4 {\n    color:#F56C6C\n}";
}
