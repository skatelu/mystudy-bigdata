package com.yjl.timeandwindows.windows.processwindowfunction;

import com.yjl.timeandwindows.windows.aggregatefunction.UrlCount;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 实现 TopN 的 KeyedProcessFunction
 */
public class TopNProcessResult extends KeyedProcessFunction<Long, UrlCount, String> {

    // 定义一个属性 n
    private Integer n;

    // 定义一个列表状态
    private ListState<UrlCount> urlCountListState;


    public TopNProcessResult(Integer n) {
        this.n = n;
    }

    // 在环境中获取状态


    @Override
    public void open(Configuration parameters) throws Exception {
        urlCountListState = getRuntimeContext().getListState(new ListStateDescriptor<UrlCount>("url-count-list", Types.POJO(UrlCount.class)));

    }

    /**
     * 每当一个元素来的时候，就执行一次定时器
     * @param value
     * @param ctx
     * @param out
     * @throws Exception
     */
    @Override
    public void processElement(UrlCount value, Context ctx, Collector<String> out) throws Exception {
        // 将数据保存到状态中
        urlCountListState.add(value);
        // 注册一个定时器 window.end + 1 ms
        ctx.timerService().registerEventTimeTimer(ctx.getCurrentKey() + 1);
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {

        List<UrlCount> urlCountList = new ArrayList<>();

        for (UrlCount urlCount : urlCountListState.get()) {
            urlCountList.add(urlCount);
        }

        urlCountList.sort(new Comparator<UrlCount>() {
            @Override
            public int compare(UrlCount o1, UrlCount o2) {
                return o2.getCount().intValue() - o1.getCount().intValue();
            }

        });

        StringBuilder result = new StringBuilder();
        result.append("-------------------------------");
        result.append("窗口结束时间：" + new Timestamp(ctx.getCurrentKey()) + "\n");

        // 取List前两个，包装信息打印输出
        for (int i = 0; i < 2; i++) {
            UrlCount urlCount = urlCountList.get(i);
            String info = "No. " + (i + 1) + " "
                    + "url: " + urlCount.getUrl() + " "
                    + "访问量: " + urlCount.getCount() + "\n";
            result.append(info);
        }

        result.append("-------------------------------\n");

        out.collect(result.toString());
    }
}
