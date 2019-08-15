package com.example.demo;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;

public class GCStats {

    private String youngPoolName;
    private String oldPoolName;
    private CircularFifoQueue<GCInfo> registry = new CircularFifoQueue<GCInfo>(100);

    public GCStats() {
        for (MemoryPoolMXBean mbean : ManagementFactory.getMemoryPoolMXBeans()) {
           String name = mbean.getName();
 //           System.out.println("GCStats - MemoryPoolMXBean: " + name);
            if (isYoungGenPool(name))
                youngPoolName = name;
            else if (isOldGenPool(name))
                oldPoolName = name;

        }
        final AtomicLong youngGenSizeAfter = new AtomicLong(0L);
 
        for(GarbageCollectorMXBean mbean: ManagementFactory.getGarbageCollectorMXBeans()) {
            if (!(mbean instanceof NotificationEmitter))
                continue;

//            System.out.println("GarbageCollectorMXBean: " + mbean.getName() + "\nCollection Count: " + mbean.getCollectionCount() + "\nCollection Time: " + mbean.getCollectionTime() );
            NotificationListener listener = (notification, ref) -> {
                long oldAfterMb = 0L;
                long youngAfterBytes = 0L;
                long youngBytes = 0L;

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
                  oldAfterMb = after.get(oldPoolName).getUsed()/ (1024*1024);
//                  System.out.println("MB promoted in old gen: " + (oldAfterMb - oldBeforeMb));
                }

                if (youngPoolName != null) {
                    youngAfterBytes = after.get(youngPoolName).getUsed();
                    youngBytes = (before.get(youngPoolName).getUsed() - youngGenSizeAfter.get())/(1024*1024);
                    youngGenSizeAfter.set(youngAfterBytes);
//                    if (delta > 0L)
//                    	System.out.println("Bytes increased in young pool: " + delta);
                }
                synchronized (this) {
                if (!isConcurrentPhase(gcCause)) {
                    registry.add(new GCInfo(duration, oldAfterMb, youngBytes, gcCause, gcAction ));
                }
                }
            };

            NotificationEmitter notificationEmitter = (NotificationEmitter) mbean;
            notificationEmitter.addNotificationListener(listener, null , null);
        }
    }

    public List<GCInfo> getGCInfo() {
        ArrayList<GCInfo> gcInfos = new ArrayList<>();
        synchronized(this) {
        Iterator<GCInfo> iterator = registry.iterator();
        while (iterator.hasNext())
        {
            gcInfos.add(iterator.next());
        }
        registry.clear();
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
        private long permGenSizeMb;
        @JsonProperty
        private long edenSizeMb;

        @JsonProperty
        private Timestamp creationTimestamp;
        
        @JsonCreator
        public GCInfo(
                final long gcPauseTimeMs,
                final long permGenSizeMb,
                final long edenSizeMb,
                final String gcCause,
                final String gcAction
        )
        {
            this.gcPauseTimeMs = gcPauseTimeMs;
            this.edenSizeMb = edenSizeMb;
            this.permGenSizeMb = permGenSizeMb;
            this.gcCause = gcCause;
            this.gcAction = gcAction;
            creationTimestamp = new Timestamp(System.currentTimeMillis());
        }
     }
}

