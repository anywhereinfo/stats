package com.example.demo;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import org.apache.commons.collections4.QueueUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import com.example.demo.util.TimeUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;


public class GCStats {

    private String youngPoolName;
    private String oldPoolName;
    private Queue<GCInfo> registry = QueueUtils.synchronizedQueue(new CircularFifoQueue<GCInfo>(60));
    private Map<GCSummaryKey, GCSummary> map = new HashMap<GCSummaryKey, GCSummary>();

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
        final AtomicLong counter = new AtomicLong(0L);
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

                GCSummaryKey key = new GCSummaryKey(gcCause, gcAction);
                synchronized(map) {
                if (map.get(key) == null) {
                	GCSummary summary = new GCSummary();
                	summary.increment();
                	map.put(key, summary);
                	}
                else {
                	GCSummary summary = map.get(key);
                	summary.increment();
                	map.put(key, summary);
                }
                }

                final Map<String, MemoryUsage> before = gcInfo.getMemoryUsageBeforeGc();
                final Map<String, MemoryUsage> after = gcInfo.getMemoryUsageAfterGc();

                if(oldPoolName != null) {
                  oldAfterMb = after.get(oldPoolName).getUsed()/ (1024*1024);

                }

                if (youngPoolName != null) {
                    youngAfterBytes = after.get(youngPoolName).getUsed();
                    youngBytes = (before.get(youngPoolName).getUsed() - youngGenSizeAfter.get())/(1024*1024);
                    youngGenSizeAfter.set(youngAfterBytes);

                }
                	long i = counter.incrementAndGet();
                	if (i%5 ==0)
                	{
                		registry.add(new GCInfo(duration, oldAfterMb, youngBytes, gcCause, gcAction ));
                		counter.set(0L);
                	}
                    

            };

            NotificationEmitter notificationEmitter = (NotificationEmitter) mbean;
            notificationEmitter.addNotificationListener(listener, null , null);
        }
    }
    

    public GCDetail getGCDetail() {
        ArrayList<GCInfo> gcInfos = new ArrayList<>();
        Map<GCSummaryKey, GCSummary> summaryMap = new HashMap<>();
        synchronized(registry) {
        	Iterator<GCInfo> iterator = registry.iterator();
        	while (iterator.hasNext())
        	{
        		GCInfo gcInfo = iterator.next();
        		if (gcInfo != null)
        			gcInfos.add(gcInfo);
        	}
        }
        synchronized(map) {
        	 for(GCSummaryKey key : map.keySet()) {
        		 summaryMap.put(key, map.get(key));
        	 }
        }
        return new GCDetail(gcInfos, summaryMap);
    }


    private boolean isYoungGenPool(String name) {
        return name.endsWith("Eden Space");
    }

    private boolean isOldGenPool(String name) {
        return name.endsWith("Old Gen") || name.endsWith("Tenured Gen");
     }

    public static class GCDetail {
       	@JsonProperty
    	private Map<GCSummaryKey, GCSummary> gcSummary;
       	
    	@JsonProperty
    	private List<GCInfo> gcInfo ;
    	
    	@JsonCreator
    	public GCDetail(final List<GCInfo> gcInfo, final Map<GCSummaryKey, GCSummary> map) {
    		this.gcInfo = gcInfo;
    		this.gcSummary = map;
    	}
    	
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
        private String creationTimestamp;
        
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
            creationTimestamp = TimeUtils.now();
        }
     }
     
     public static class GCSummaryKey {
    	 @JsonProperty
    	 private String gcCause;
    	 @JsonProperty
    	 private String gcAction;
    	 
    	 @JsonCreator
    	 public GCSummaryKey (final String gcCause, final String gcAction) {
    		 this.gcAction= gcAction;
    		 this .gcCause = gcCause;
    	 }
    	 
    	 @Override
    	 public boolean equals(Object obj) {
    		 if (obj instanceof GCSummaryKey) {
    			 GCSummaryKey temp = (GCSummaryKey) obj;
    			 return( (this.gcAction.equals(temp.gcAction)) && (this.gcCause.equals(temp.gcCause)));
    		 }
    		 return false;
    	 }
    	 
    	 @Override
    	 public int hashCode() {
    		 int result = 31 * gcCause.hashCode();
    		 result += 31 * gcAction.hashCode();
    		 return result;
    	 }
    	 
    	 @Override
    	 public String toString() {
    		 return gcCause + "--" + gcAction;
    	 }
     }
     
     public static class GCSummary {
    	 @JsonIgnore
    	 private AtomicLong counter = new AtomicLong(0L);
    	 @JsonProperty
    	 private String timestamp;
    	 
    	 
    	 @JsonCreator
    	 public GCSummary() {
    		 timestamp = TimeUtils.now();
    	 }
    	 
    	 @JsonIgnore
    	 public void increment() {
    		 counter.incrementAndGet();
    		 timestamp = TimeUtils.now();
    	 }
    	 
    	 @JsonProperty
    	 public long counter() {
    		 return counter.get();
    	 }
    	 
    	 @Override
    	 
    	 public String toString() {
    		 return counter.get() + "--" + timestamp;
    	 }
     }
}

