package com.yjl.statetest.functionImpl;

import com.yjl.chapter05.Event;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 开窗口，统计用户的 pv 数据，并且，每隔10s向下游输出一次
 */
public class PeriodicPvResult extends KeyedProcessFunction<String, Event, String> {

    // 定义两个状态，保存当前 pv 值，以及定时器时间戳
    ValueState<Long> couintState;


    @Override
    public void processElement(Event event, Context context, Collector<String> collector) throws Exception {

    }
}
