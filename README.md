# TxManager
LCN分布式事务管理器，协调分布式事务的事务管理，完成三阶段事务提交。

### 使用教程
1. 启动redis服务，并配置redis.properties配置文件。
2. 启动springcloud-eureka服务。
3. 配置application.properties,redis.properties 配置文件。
4. 启动TxManager run: TxManagerApplication.main()方法。


# LCN分布式事务框架的设计原理

该框架分布式事务是基于spring事务框架基础之上做的再次封装，通过控制协调本地事务与全局事务的一致从而达到分布式事务控制。该框架依赖Redis服务，将事务控制数据存放在redis下，因此在集群TxManager时只需要让其共享Redis服务即可。


## 三节段提交事务
1. 锁定事务单元
2. 确认事务模块状态
3. 通知事务


## 锁定事务单元

![ ](readme/WX20170613-161341.png)

我们假设方法A是分布式事务发起方法，A调用了B和C，B有调用了B1 B1有调用了B11和B12  
那么他们的流程为  
. 当执行A方法时，会先创建事务组。然后A将自己的事务单元添加到TxManager。此时A的业务方法会被调用。  
. B被执行，B也会将自己的事务单元添加到TxManager，然后执行B的业务单元。   
. B1被执行，B1也会将自己的事务单元添加到TxManager，然后执行B1的业务单元。  
. B11被执行，B11也会将自己的事务单元添加到TxManager，然后执行B11的业务单元，B11的业务执行完以后返回数据并通知TxManager事务单元状态，然后进入等待通知状态。  
. B12被执行，B12也会将自己的事务单元添加到TxManager，然后执行B12的业务单元，B12的业务执行完以后返回数据并通知TxManager事务单元状态，然后进入等待通知状态。  
. B1事务执行次完毕，通知TxManager事务单元状态，然后进入等待通知状态。  
. B事务执行次完毕，通知TxManager事务单元状态，然后进入等待通知状态。  
. C被执行，C也会将自己的事务单元添加到TxManager，然后执行C的业务单元，C的业务执行完以后返回数据并通知TxManager事务单元状态，然后进入等待通知状态。  
. A事务执行次完毕，通知TxManager事务单元状态，然后进入等待通知状态。  
. A发起通知TxManager第一阶段已经执行完毕。  


## 确认事务模块状态

当A通知TxManager第一阶段已经执行完毕后，TxManager会检查事务单元模块是否都有效，若有效则进入通知确认阶段，否则直接进入第三阶段回滚事务。看似确认事务模块状态没有太大作用，其实主要用意是两点，1：确认与事务模块是否正常通讯，2：确认事务单元模块是否等待超时事务已经回滚。当事务单元接受到TxManager的事务单元模块确认通知以后，事务单元模块将不会自动回滚事务等待TxManager通知。


## 通知事务

当事务模块存在异常，那么在第一阶段时就能得知整个事务状态，然后直接通知各个事务单元事务回滚。此时即便事务没有收到通知，事务也会自动回滚。若事务都正常但在确认事务模块状态时，发现事务无法访问，则依旧会通知事务模块回滚，那么那些无法访问的模块由于没有接受到TxManager的任何指令也会自动回滚，那些已经被通知到事务单元模块会等待TxManager通知事务回滚。若都正常的情况下会通知事务全部提交。


