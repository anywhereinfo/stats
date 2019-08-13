package com.example.demo;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;

public class GCStats {

    private String youngPoolName;
    private String oldPoolName;
    private ArrayList<GCInfo> registry = new ArrayList<>();

    public GCStats() {
        for (MemoryPoolMXBean mbean : ManagementFactory.getMemoryPoolMXBeans()) {
            String name = mbean.getName();

            if (isYoungGenPool(name))
                youngPoolName = name;
            else if (isOldGenPool(name))
                oldPoolName = name;
        }

        for(GarbageCollectorMXBean mbean: ManagementFactory.getGarbageCollectorMXBeans()) {
            if (!(mbean instanceof NotificationEmitter))
                continue;

            NotificationListener listener = (notification, ref) -> {
                long oldBeforeMb = 0L;
                long oldAfterMb = 0L;
                long youngBeforeMb = 0L;
                long youngAfterBytes = 0L;

                if(!notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION))
                  return;

                CompositeData cd = (CompositeData) notification.getUserData();
                GarbageCollectionNotificationInfo notificationInfo = GarbageCollectionNotificationInfo.from(cd);

                String gcCause = notificationInfo.getGcCause();
                String gcAction = notificationInfo.getGcAction();
                GcInfo gcInfo = notificationInfo.getGcInfo();
                long duration = gcInfo.getDuration();


                final Map<String, MemoryUsage> before = gcInfo.getMemoryUsageBeforeGc();
                final Map<String, MemoryUsage> after = gcInfo.getMemoryUsageAfterGc();

                if(oldPoolName != null) {
                  oldBeforeMb = before.get(oldPoolName).getUsed()/ (1024*1024);
                  oldAfterMb = after.get(oldPoolName).getUsed()/ (1024*1024);
                }

                if (youngPoolName != null) {
                   youngBeforeMb  = before.get(youngPoolName).getUsed() / (1024*1024);
                    youngAfterBytes = after.get(youngPoolName).getUsed();
                }

                if (!isConcurrentPhase(gcCause)) {
                    registry.add(new GCInfo(duration, oldBeforeMb, oldAfterMb, youngBeforeMb, youngAfterBytes, gcCause, gcAction ));
                }
            };

            NotificationEmitter notificationEmitter = (NotificationEmitter) mbean;
            notificationEmitter.addNotificationListener(listener, null , null);
        }
    }

    public List<GCInfo> getGCInfo() {
        ArrayList<GCInfo> gcInfos = new ArrayList<>();
        ListIterator<GCInfo> iterator = registry.listIterator();
        while (iterator.hasNext())
        {
            gcInfos.add(iterator.next());
        }

        return gcInfos;
    }

    private boolean isConcurrentPhase(String cause) {
        return "No GC".equals(cause);
    }

    private boolean isYoungGenPool(String name) {
        return name.endsWith("Eden Space");
    }

    private boolean isOldGenPool(String name) {
        return name.endsWith("Old Gen") || name.endsWith("Tenured Gen");
     }

     public class GCInfo {

        @JsonProperty
        private String gcCause;

        @JsonProperty
        private String gcAction;

        @JsonProperty
        private long gcPauseTimeMs;
        @JsonProperty
        private long oldSizeBeforeGC_Mb;
        @JsonProperty
        private long oldSizeAfterGC_Mb;
        @JsonProperty
        private long newSizeBeforeGC_Mb;
        @JsonProperty
        private long newSizeAfterGC_Bytes;

        @JsonCreator
        public GCInfo(
                final long gcPauseTimeMs,
                final long oldSizeBeforeGC_Mb,
                final long oldSizeAfterGC,
                final long newSizeBeforeGC_Mb,
                final long newSizeAfterGC_Bytes,
                final String gcCause,
                final String gcAction
        )
        {
            this.gcPauseTimeMs = gcPauseTimeMs;
            this.newSizeAfterGC_Bytes = newSizeAfterGC_Bytes;
            this.newSizeBeforeGC_Mb = newSizeBeforeGC_Mb;
            this.oldSizeAfterGC_Mb = oldSizeAfterGC;
            this.oldSizeBeforeGC_Mb = oldSizeBeforeGC_Mb;
            this.gcCause = gcCause;
            this.gcAction = gcAction;
        }
     }
}

