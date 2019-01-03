# pay-client-springboot-javafx

#### 介绍
基于javafx + spring boot 实现的自助充值查询机,在不修改现有的消费系统前提下，独立运行的自助充值客户端。
通过向聚合支付平台申请支付二维码，用户扫码后，向本地数据库中插入充值记录。

#### 软件架构
javafx + springboot + spring data jpa + lombok

1. javafx:不确定充值机将来会不会使用linux，所以用javafx做比较保险，毕竟界面也不多，配合sceneBuilder工具也很方便
2. springboot:主要是使用了springboot-javafx-support这个插件，使用项目扩展很容易，将来配合spring-boot-admin做监控也很方便。
3. spring data jpa：虽然直接操作数据库有点危险，局域网内这个应该不重要了。
4. lombok：太懒，感觉懒人必备。

#### 安装教程

1. git clone https://gitee.com/panmingzhi/pay-client-springboot-javafx.git
2. mvn jfx:jar

#### 使用说明

1. 系统默认使用sqlserver数据库，如果其他数据库，请先其他数据库dependency
2. 使用前，请先修改application.yml中的数据库配置

#### 参与贡献

1. 有实际需要的朋友，请留言联系哈


#### 截图
##### 启动
![输入图片说明](https://images.gitee.com/uploads/images/2018/1225/205854_f66d3c15_87848.png "QQ截图20181225205252.png")
##### 主页
![输入图片说明](https://images.gitee.com/uploads/images/2018/1225/205919_4d0dda7b_87848.png "QQ截图20181225205122.png")
##### 读卡
![输入图片说明](https://images.gitee.com/uploads/images/2018/1225/205937_3471d694_87848.png "QQ截图20181225205137.png")
##### 选择充值金额
![输入图片说明](https://images.gitee.com/uploads/images/2018/1225/205946_9258bbac_87848.png "QQ截图20181225205201.png")
##### 充值付款码
![输入图片说明](https://images.gitee.com/uploads/images/2018/1225/205955_cdc0d04d_87848.png "QQ截图20181225205209.png")
##### 查询界面
![输入图片说明](https://images.gitee.com/uploads/images/2018/1225/210019_d4b3f10d_87848.png "QQ截图20181225205227.png")