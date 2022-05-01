# Hadoop 学习

## Hadoop生态圈

### 什么是大数据？

* 短时间内，快速产生大量有价值的信息

### Hadoop  HDFS 文件存储系统

* 是以 block块 为单位存放的
  * 会将大文件线性切割成块(block)，分散存放在集群的节点中
  * 单一文件的block块大小一致，不同文件可以不一样，最后一个块还是128M，但内部存储的数据可能小于128M
  * 为了安全----机制（副本机制）
  * 追加数据   --append（只在尾部追加数据）
  * 只支持一次写入，多次读取，同一时刻只有一个写入者

### 读写操作

#### 写操作

* namenode :老板
  * 掌控全局，管理DataNode，以及元数据（描述数据的数据）	-- 内存中
  * 接收客户端的读写服务（秘书----申请，能不能存这个文件）
  * 接受DataNode的信息
  * 给Client一些DataNode的信息
* DataNode 干活的
  * 存储文件（block块），向NameNode发送信息（存了什么东西，以及自己是否还存活）
  * 存储block的元数据信息（nameNode 存储的是文件整体的元数据，DataNode存放每一个 block块的信息）
  * 接收client的请求

### 写操作的具体实践

* 大文件线性切割成bolck块：client会优先计算一下大文件的block的块数，block（默认128M）block数=大文件/128M  向前进一位
* client会向NameNode 汇报情况：
  * 当前大文件的block数、
  * 当前大文件的属于谁
  * 当前大文件的权限
  * 当前大文件的上传时间

以下步骤持续循环进行，直到将大文件的block块切割，存储完成

* client 切割一个block块
* client 请求block的id，还有DataNode地址
* NameNode就会将一些负载不高的DataNode的地址返回给client
* client已经拿到地址，就会找到这个DataNode进行数据上传并且会做好备份
* DataNode将block存储完毕后会向NameNode进行汇报



### 读操作

* client 会向NameNode请求block的位置信息，NameNode掌握元数据信息
* NameNode返回Block的信息给client
* client拿到block的信息之后去 读取block的数据---- 读的时候有 就近原则 即距离 主节点最近的 物理DataNode节点数据



### 备份机制

* 

















