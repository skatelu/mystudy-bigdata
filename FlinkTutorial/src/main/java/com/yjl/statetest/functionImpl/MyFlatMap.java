package com.yjl.statetest.functionImpl;

import com.yjl.chapter05.Event;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * 状态编程中，自定义FlatMap
 * 用于Keyd State 测试
 */
public class MyFlatMap extends RichFlatMapFunction<Event,String> {

    // 定义状态
    private ValueState<Event> myValueState;

    @Override
    public void open(Configuration parameters) throws Exception {
        myValueState = getRuntimeContext().getState(new ValueStateDescriptor<Event>("my-state", Event.class));
    }

    @Override
    public void flatMap(Event value, Collector<String> out) throws Exception {

        // 访问和更新状态
        System.out.println(myValueState.value());

        myValueState.update(value);

        System.out.println("myvalue = " + myValueState.value());
    }

}
