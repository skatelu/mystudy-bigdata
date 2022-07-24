package com.yjl.statetest.functionImpl;

import com.yjl.chapter05.Event;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.LocalTime;

/**
 * 实现自定义的 KeydeProcessFunction
 * 设置一个定时器，周期性的输出相关的统计的 状态的结果
 */
public class PeriodicPvResult extends KeyedProcessFunction<String, Event, String> {

    // 定义状态，保存当前Pv的统计值,以及有没有定时器
    private ValueState<Long> countState;
    private ValueState<Long> timerTsState;

    /**
     * 富函数方法，在类加载完成后，执行一次，可用于创建资源数据
     *
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        countState = getRuntimeContext().getState(new ValueStateDescriptor<Long>("count", Long.class));
        timerTsState = getRuntimeContext().getState(new ValueStateDescriptor<Long>("timerTs", Long.class));
    }

    /**
     * 每来一条数据，就更新一次
     * 注意，注册定时器后，每注册一次，只会执行一次，不会循环往复执行
     * @param value
     * @param ctx
     * @param out
     * @throws Exception
     */
    @Override
    public void processElement(Event value, Context ctx, Collector<String> out) throws Exception {
        Long count = countState.value();
        countState.update(count == null ? 1 : count + 1);

        long timer = value.timestamp;

        // 如果没有注册过定时器，注册定时器
        if (timerTsState.value() == null) {
            // 从上下文中获取定时器服务，进行注册
            ctx.timerService().registerEventTimeTimer(timer + 10 * 1000L);
            timerTsState.update(timer + 10 * 1000L);
        }

    }

    /**
     * 注册定时器后，定时器执行时需要做什么，
     * 定时器具体处理的方法，在这里面执行
     * @param timestamp
     * @param ctx
     * @param out
     * @throws Exception
     */
    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
        // 定时器触发，输出一次结果
        out.collect(ctx.getCurrentKey() + " s Pv: " + countState.value()+ "时间为" + LocalTime.now());
        // 清空状态 定时器状态清空
        timerTsState.clear();
    }
}
