package com.yjl.timeandwindows.windows.aggregatefunction;

import java.sql.Timestamp;

/**
 * 自定义窗口聚合函数 aggregateFunction 统计URL的访问量
 */
public class UrlCount {

    private String Url;
    private Long count;
    private Long windowStart;
    private Long WindowEnd;

    public UrlCount() {
    }

    public UrlCount(String url, Long count, Long windowStart, Long windowEnd) {
        Url = url;
        this.count = count;
        this.windowStart = windowStart;
        WindowEnd = windowEnd;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(Long windowStart) {
        this.windowStart = windowStart;
    }

    public Long getWindowEnd() {
        return WindowEnd;
    }

    public void setWindowEnd(Long windowEnd) {
        WindowEnd = windowEnd;
    }

    @Override
    public String toString() {
        return "UrlCount{" +
                "Url='" + Url + '\'' +
                ", count=" + count +
                ", windowStart=" + new Timestamp(windowStart) +
                ", WindowEnd=" + new Timestamp(WindowEnd) +
                '}';
    }
}
