package com.yjl.timeandwindows.windows.processwindowfunction;

import com.yjl.chapter05.Event;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.util.HashSet;

/**
 * 自定义窗口处理函数
 * 全窗口函数中，处理窗口函数（ProcessWindowFunction）
 */
public class UvCountByWindow extends ProcessWindowFunction<Event, String, Boolean, TimeWindow> {


    @Override
    public void process(Boolean aBoolean, Context context, Iterable<Event> element, Collector<String> out) throws Exception {

        // 创建 hashSet 对人名进行去重
        HashSet<String> userSet = new HashSet<>();
        int count = 0;


        for (Event event : element) {
            userSet.add(event.user);
            count++;
        }

        // 结合窗口信息，包装输出内容
        // 从上下文中，获取开始和结束时间
        long start = context.window().getStart();
        long end = context.window().getEnd();

        // 调用 Collector<String> 的 collect 方法 将整体计算的窗口数据进行输出
        out.collect("窗口： " + new Timestamp(start) + "~" + new Timestamp(end)
                + " 的独立访客数量是：" + userSet.size()
                + "请求访问量是： " + count);

    }
}
