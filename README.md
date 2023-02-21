## 基于AIDL的SDK封装和使用示例

把Client端对Service的绑定、重连、线程切换等细节隐藏到SDK中并封装，
使用时只需要继承BaseConnectManager并传入Service的包名、类名和期望的断线重连时间即可。

在大多数车载系统应用架构中，一个完整的应用往往会包含三层：

- `HMI` Human Machine Interface，显示UI信息，进行人机交互。

- `Service` 在系统后台进行数据处理，监控数据状态。

- `SDK` 根据业务逻辑Service对外暴露的通信接口，其他模块通过它来完成IPC通信。

****

### 在Binder中注册回调，请使用`RemoteCallbackList`
*参考：*
[跨进程监听](https://www.jianshu.com/p/69e5782dd3c3)


### 对于Android11及以上版本，推荐使用`aar`
服务端提供数据，并提供接口打包成SDK给第三方使用
- 打包成`jar`
- 打包成`aar`


由于Android 11 中的软件包可见性，第三方需要在`manifest`中使用`<queries>`声明`service`组件。打包成`aar`可以保留SDK模块中的manifest文件，第三方无需接入。

[Android 11 中的软件包可见性](https://developer.android.google.cn/training/basics/intents/package-visibility?hl=zh-cn#package-name)

![<queries>声明](https://user-images.githubusercontent.com/65901383/220333639-dca5eaba-4284-43d2-9474-8012da24cd03.png)
