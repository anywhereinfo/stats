package com.example.demo;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.GCStats.GCDetail;
import com.example.demo.util.TimeUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
public class StatsController {

	private static GCStats gcStats = new GCStats();
	
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
	
   
	public class Stats {
        
		@JsonProperty
		private String id = UUID.randomUUID().toString();
		
		@JsonProperty
		private String eventTimestamp = TimeUtils.now();
		
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
        private GCDetail gcDetail = gcStats.getGCDetail();
        
 
	}
}
