package com.example.demo;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ThreadStats {
    @JsonProperty
    int activeThreadCount = 0;
    @JsonProperty
    int peakThreadCount = 0;
    @JsonProperty
    int daemonThreadCount = 0;
    @JsonProperty
    int blockedCount = 0;
    @JsonProperty
    private List<String> blockedThreadNames = new ArrayList<>();

    @JsonCreator
    public ThreadStats() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        activeThreadCount = threadBean.getThreadCount();
        peakThreadCount = threadBean.getPeakThreadCount();
        daemonThreadCount = threadBean.getDaemonThreadCount();

        for (long id : threadBean.getAllThreadIds()) {
           if ((threadBean.getThreadInfo(id).getThreadState().compareTo(Thread.State.BLOCKED)) ==0 ) {
               blockedCount++;
               blockedThreadNames.add(threadBean.getThreadInfo(id).getThreadName());
           }
        }
    }

}