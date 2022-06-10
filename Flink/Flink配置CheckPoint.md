# Flink 配置CheckPoint(检查点)

```properties
# 设置flink 保存checkpoint的频率，此为 5000ms 一次 周期性的触发保存 
execution.checkpointing.interval: 5000
# 状态一致性，设置为精确一次
execution.checkpointing.mode: EXACTLY_ONCE
# 存储方式为 外部文件系统
state.backend: filesystem
# 保存到hdfs的什么位置
state.checkpoints.dir: hdfs://10.166.147.61:38500/flink/checkpoints
# savepoint 保存到hdfs的什么位置
state.savepoints.dir: hdfs://10.166.147.61:38500/flink/savepoint
# checkpoint 多长时间没保存完成既算失败
execution.checkpointing.timeout: 600000
# checkpoint 保存执行的时间间隔
execution.checkpointing.min-pause: 500
# checkpoint 执行的并行度
execution.checkpointing.max-concurrent-checkpoints: 1
# 目前代码不能设置保留的checkpoint个数 默认值时保留一个 假如要保留3个
state.checkpoints.num-retained: 1
# 作业取消的时候也会保留外部检查点。
execution.checkpointing.externalized-checkpoint-retention: RETAIN_ON_CANCELLATION
# 允许checkpoint 保存失败的次数，失败后会进行重试
execution.checkpointing.tolerable-failed-checkpoints: 3
```







* 需要修改flink 的 bin目录下的 config.sh 文件

  * 将程序启动的 PID 保存在 /opt/flink/tmp 文件下，防止时间长了，tmp目录下的PID被清除，导致 stop-cluster.sh 关闭任务失败

  ```shell
  # WARNING !!! , these values are only used if there is nothing else is specified in
  # conf/flink-conf.yaml
  
  DEFAULT_ENV_PID_DIR="/opt/flink/tmp"                          # Directory to store *.pid files to
  ```

  