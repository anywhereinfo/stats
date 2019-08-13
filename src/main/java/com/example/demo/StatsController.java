package com.example.demo;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
public class StatsController {

	static AtomicReference<GCStats> gcStatsRef = new AtomicReference<GCStats>(new GCStats());
	
	@GetMapping("/api/v1/stats")
	public Stats getStats() {
		return new Stats();
	}
	
    @GetMapping("/api/v1/do")
    public HttpStatus doSomething() {

        AtomicLong stuff[] = new AtomicLong[5000000];
        for(int i = 0; i<5000000; i++)
              stuff[i] = new AtomicLong(i);

          return HttpStatus.OK;
    }
	
    private static void resetGCStats() {
        gcStatsRef.set(new GCStats());
    }
    
	public class Stats {
        
		@JsonProperty
        private UptimeStats uptimeStats = new UptimeStats();
		
        @JsonProperty
        private CPUStats cpuStats = new CPUStats();		

        @JsonProperty
        private TomcatStats tomcatStats = new TomcatStats();
        
        @JsonProperty
        private ThreadStats threadStats = new ThreadStats();        
        
        @JsonProperty
        private MemoryStats memoryStats = new MemoryStats();
        
        @JsonProperty
        private List<GCStats.GCInfo> gcInfos = gcStatsRef.get().getGCInfo();
        
        @JsonCreator
        public Stats() {
        	resetGCStats();
        }
        
	}
}
