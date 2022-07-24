package com.yjl.statetest;

import com.yjl.chapter05.Event;
import com.yjl.chapter05.reducetest.ClickSource;
import com.yjl.statetest.functionImpl.PeriodicPvResult;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

/**
 * ValueState 使用场景，状态为缓存一个值  值状态
 * 周期性的输出总的 Pv数据，
 * 不进行开窗计算，统计所有时间段的 Pv
 * 实现：设置一个定时器，每隔一段时间，输出一下当前总的Pv数据量  ProcessFunction 实现，因为用到定时器，所以必须用KeyBy
 */
public class PeriodicPvExample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.getConfig().setAutoWatermarkInterval(100L);

        SingleOutputStreamOperator<Event> stream = env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ZERO)
                                .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                                    @Override
                                    public long extractTimestamp(Event element, long recordTimestamp) {
                                        return element.timestamp;
                                    }
                                })
                );

//        stream.print("input");

        // 统计每个用户的Pv
        stream.keyBy(data -> data.user)
                .process(new PeriodicPvResult())
                .print();

        env.execute();
    }

}
