# DolphinScheduler 源码分析

## 任务提交请求

### 运行流程实例

```http
http: //10.166.147.18:38364/dolphinscheduler/projects/5029181762240/executors/start-process-instance
```

* 入参

  |                           |                                                              |       |       |         |      |
  | :------------------------ | ------------------------------------------------------------ | ----- | ----- | ------- | ---: |
  | warningType               | 发送策略,可用值:NONE,SUCCESS,FAILURE,ALL                     | query | true  | ref     |      |
  | dryRun                    | dryRun                                                       | query | false | integer |      |
  | environmentCode           | ENVIRONMENT_CODE                                             | query | false | integer |      |
  | execType                  | 指令类型,可用值:START_PROCESS,START_CURRENT_TASK_PROCESS,RECOVER_TOLERANCE_FAULT_PROCESS,RECOVER_SUSPENDED_PROCESS,START_FAILURE_TASK_PROCESS,COMPLEMENT_DATA,SCHEDULER,REPEAT_RUNNING,PAUSE,STOP,RECOVER_WAITING_THREAD | query | false | ref     |      |
  | expectedParallelismNumber | 补数任务自定义并行度                                         | query | false | integer |      |
  | runMode                   | 运行模式,可用值:RUN_MODE_SERIAL,RUN_MODE_PARALLEL            | query | false | ref     |      |
  | startNodeList             | 开始节点列表(节点name)                                       | query | false | string  |      |
  | startParams               | 启动参数                                                     | query | false | string  |      |
  | taskDependType            | 任务依赖类型,可用值:TASK_ONLY,TASK_PRE,TASK_POST             | query | false | ref     |      |
  | timeout                   | 超时时间                                                     | query | false | integer |      |
  | workerGroup               | worker群组                                                   | query | false | string  |      |

