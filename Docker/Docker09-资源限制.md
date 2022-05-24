# Docker 资源限制

## 1、CGROUP 机制





## 内存资源限制

* 默认情况下，如果不对容器做任何限制，容器能够占用当前系统能给容器提供的所有资源
  * Dcoker 限制可以从 Memory、CPU、Block I/O 三个方面
  * OOME：Out Of Memory Exception
    * 一旦发生 OOME，任何进程都有可能被杀死，包括 docker daemon在内
    * 为此，Docker 调整了 docker daemon 的OOM优先级，以免内核关闭



## CPU资源限制

