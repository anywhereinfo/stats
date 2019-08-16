package com.example.demo;

import static java.util.Objects.requireNonNull;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Instant;

import javax.annotation.Nullable;

import com.example.demo.util.TimeUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;





public class CPUStats {

    private final OperatingSystemMXBean operatingSystemMXBean;

    @Nullable
    private final Class<?> operatingSystemBeanClass = resolveClass();

    @JsonIgnore
    @Nullable
    private final Method processCPUUsageMethod;

    @JsonIgnore
    @Nullable
    private final Method systemCPUUsageMethod;

    @JsonProperty
    private int cpus;
    @JsonProperty
    private double systemLoadAverageForLastMinute;
    @JsonProperty
    private double systemCPUUsage;
    @JsonProperty
    private double processCPUUsage;
    @JsonProperty
    private String eventTime = TimeUtils.now();

    @JsonCreator
    public CPUStats() {
        this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        DecimalFormat nf = new DecimalFormat("##.00");
        nf.setRoundingMode(RoundingMode.DOWN);
        
        this.systemCPUUsageMethod = detectMethod("getSystemCpuLoad");
        
        this.processCPUUsageMethod = detectMethod("getProcessCpuLoad");
        cpus = Runtime.getRuntime().availableProcessors();

        systemLoadAverageForLastMinute =  Double.parseDouble(nf.format(operatingSystemMXBean.getSystemLoadAverage()));
        systemCPUUsage = Double.parseDouble(nf.format(invoke(systemCPUUsageMethod)));
        processCPUUsage = Double.parseDouble(nf.format(invoke(processCPUUsageMethod)));

    }


    @Nullable
    private Method detectMethod(String name) {
        requireNonNull(name);
        if (operatingSystemBeanClass == null)
            return null;

        try {
            operatingSystemBeanClass.cast(operatingSystemMXBean);
            return operatingSystemBeanClass.getDeclaredMethod(name);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private double invoke(@Nullable Method method) {
        try {
            return method != null ? (double) method.invoke(operatingSystemMXBean) : Double.NaN;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return Double.NaN;
        }
    }

    @Nullable
    private Class<?> resolveClass() {
        try {
            return Class.forName("com.sun.management.OperatingSystemMXBean");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
