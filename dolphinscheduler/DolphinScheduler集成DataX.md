# Dolphinscheduler 集成DataX

## DataX 下载地址

http://datax-opensource.oss-cn-hangzhou.aliyuncs.com/datax.tar.gz



## 需要提前安装python环境，最好是 python2.X

* Centos7 默认安装，python2.7

* 可以通过命令查找

  ```SHELL
  [dolphinscheduler@localhost datax]$ which python
  /usr/bin/python
  ```

* 可运行文件的目录在

  * /user 目录下，有python2.7，python等命令



## DataX Centos7 安装

* 将下载的 datax.tar.gz 上传到服务器上，并解压到指定的目录

  ```shell
  tar -zxvf datax.tar.gz -C /opt
  ```

* 解压到当前目录后，删除相关文件中带. 的隐藏文件

* 删除`datax/plugin/reader`下所有`._xxxx`隐藏文件 **注意：**一定要`._*er`这种方式匹配文件，否则会匹配到里面的隐藏jar包

  ```shell
  find /opt/datax/plugin/reader/ -type f -name "._*er" | xargs rm -rf
  ```

  ```shell
  find /opt/datax/plugin/writer/ -type f -name "._*er" | xargs rm -rf
  ```

  * 如果不删除的话，后续运行数据抽取时，就会产生报错信息

  ```verilog
  [INFO] 2022-03-31 10:27:17.523 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.datax.DataxTask:[66] -  -> welcome to use bigdata scheduling system...
  	
  	DataX (DATAX-OPENSOURCE-3.0), From Alibaba !
  	Copyright (C) 2010-2017, Alibaba Group. All Rights Reserved.
  	
  	
  	2022-03-31 10:27:17.452 [main] WARN  ConfigParser - 插件[mysqlreader,mysqlwriter]加载失败，1s后重试... Exception:Code:[Common-00], Describe:[您提供的配置文件存在错误信息，请检查您的作业配置 .] - 配置信息错误，您提供的配置文件[/opt/datax/plugin/reader/._drdsreader/plugin.json]不存在. 请检查您的配置文件. 
  [INFO] 2022-03-31 10:27:18.469 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.datax.DataxTask:[200] - process has exited, execute path:/tmp/dolphinscheduler/exec/process/5036516745088/5036589750656_1/22/39, processId:13073 ,exitStatusCode:1 ,processWaitForStatus:true ,processExitValue:1
  [INFO] 2022-03-31 10:27:18.525 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.datax.DataxTask:[66] -  -> 2022-03-31 10:27:18.457 [main] ERROR Engine - 
  	
  	经DataX智能分析,该任务最可能的错误原因是:
  	com.alibaba.datax.common.exception.DataXException: Code:[Common-00], Describe:[您提供的配置文件存在错误信息，请检查您的作业配置 .] - 配置信息错误，您提供的配置文件[/opt/datax/plugin/reader/._drdsreader/plugin.json]不存在. 请检查您的配置文件.
  		at com.alibaba.datax.common.exception.DataXException.asDataXException(DataXException.java:26)
  		at com.alibaba.datax.common.util.Configuration.from(Configuration.java:95)
  		at com.alibaba.datax.core.util.ConfigParser.parseOnePluginConfig(ConfigParser.java:153)
  		at com.alibaba.datax.core.util.ConfigParser.parsePluginConfig(ConfigParser.java:125)
  		at com.alibaba.datax.core.util.ConfigParser.parse(ConfigParser.java:63)
  		at com.alibaba.datax.core.Engine.entry(Engine.java:137)
  		at com.alibaba.datax.core.Engine.main(Engine.java:204)
  	
  [INFO] 2022-03-31 10:27:18.527 TaskLogLogger-class org.apache.dolphinscheduler.plugin.task.datax.DataxTask:[60] - FINALIZE_SESSION
  ```

* 最后，datax的安装目录在
  * /opt/datax

## 将DataX配置集成到 DolphinScheduler中

* 因为 dolphinScheduler 中Master节点只是用来进行任务分配，并不直接处理任务，Worker节点用来具体处理相关任务，因此需要给每个 worker接口安装DataX，并进行配置

* 修改配置文件 

  ```shell
  [dolphinscheduler@localhost datax]$ vim dolphinscheduler/conf/env/dolphinscheduler_env.sh 
  ```

  ```properties
  # 修改python环境变量 因为可执行文件在 /user目录下 后面的export PATH 中会引导到具体文件
  export PYTHON_HOME=/usr
  # 修改DataX 文件的安装路径
  export DATAX_HOME=/opt/datax
  
  export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$SPARK_HOME2/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH
  
  ```



## 执行

* 最后在 dolphinscheduler 中配置 DataX  流程即可执行