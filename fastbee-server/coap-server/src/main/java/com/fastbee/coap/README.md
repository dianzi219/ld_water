## Coap


## Coap Roadmap
- Coap消息编解码 消息头 可选项 消息内容
- Coap数据路由注册
- Coap观察机制
- Coap消息块处理
- Coap认证处理 Token Messageid
- Coap content上下文
- Coap TransportService北向接口

## Coap南向接口
- .well-known/core 查询服务端支持的uri资源
- /utc-time 查询服务端utc-time
- 其他拓展（设备注册,设备上下线,设备属性上报,设备）

## Coap北向接口 （TransportService 开放给业务层的接口类）
- 设备命令下发
- 设备属性设置
- 设备升级
- 设备重启

## Coap TransportContext 上下文
- 转发器 （http转发,mqtt broker转发）
- 消息格式适配器（物模型编码,json,字节编码,裸数据）
- 调度器
- 执行线程池
- 请求消息
- 响应消息
- 会话管理

## Coap TransportHandler 处理函数
- ReqDispatcher 消息分发处理

