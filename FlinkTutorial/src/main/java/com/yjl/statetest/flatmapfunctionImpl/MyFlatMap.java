package com.yjl.statetest.flatmapfunctionImpl;

import com.yjl.chapter05.Event;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * 状态编程中，自定义FlatMap
 * 用于Keyd State 测试
 */
public class MyFlatMap extends RichFlatMapFunction<Event, String> {

    // 定义状态 ValueState
    private ValueState<Event> myValueState;

    private ListState<Event> myListState;

    private MapState<String, Long> myMapState;

    private ReducingState<Event> myReducingState;

    private AggregatingState<Event, String> myAggregatingState;


    @Override
    public void open(Configuration parameters) throws Exception {
        myValueState = getRuntimeContext().getState(new ValueStateDescriptor<Event>("my-state", Event.class));
        myListState = getRuntimeContext().getListState(new ListStateDescriptor<Event>("my-listState", Event.class));
        myMapState = getRuntimeContext().getMapState(new MapStateDescriptor<String, Long>("my-mapState", String.class, Long.class));
        myReducingState = getRuntimeContext().getReducingState(new ReducingStateDescriptor<Event>("my-resucingState", new ReduceFunction<Event>() {
            @Override
            public Event reduce(Event value1, Event value2) throws Exception {
                return new Event(value1.user, value1.url, value2.timestamp);
            }
        }, Event.class));
        myAggregatingState = getRuntimeContext().getAggregatingState(new AggregatingStateDescriptor<Event, Long, String>("my-aggregatingState", new AggregateFunction<Event, Long, String>() {
            @Override
            public Long createAccumulator() {
                return 0L;
            }

            @Override
            public Long add(Event value, Long accumulator) {
                return accumulator + 1;
            }

            @Override
            public String getResult(Long accumulator) {
                return "count: " + accumulator;
            }

            @Override
            public Long merge(Long a, Long b) {
                return null;
            }
        }, Long.class));

    }

    @Override
    public void flatMap(Event value, Collector<String> out) throws Exception {

        // 访问和更新状态
        System.out.println(myValueState.value());
        myValueState.update(value);
        System.out.println("myvalue = " + myValueState.value());

        myListState.add(value);

        myMapState.put(value.user, myMapState.get(value.user) == null ? 0 : myMapState.get(value.user) + 1);
        System.out.println("my map value: " + value.user + " " + myMapState.get(value.user));

        myReducingState.add(value);

        myAggregatingState.add(value);
        System.out.println("my-aggregatingState: " + myAggregatingState.get());

    }
}
