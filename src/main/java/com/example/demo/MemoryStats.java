package com.example.demo;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MemoryStats {

	
	@JsonProperty
	private long maxMemoryMb;
	
	@JsonProperty
	private double availableRamMb;
	
	@JsonProperty
	private MemoryByType memoryByTypes[] = new MemoryByType[2];
	
	@JsonCreator
	public MemoryStats() {
	      for (MemoryPoolMXBean memoryPoolBean : ManagementFactory.getPlatformMXBeans(MemoryPoolMXBean.class)) {
	            String area = MemoryType.HEAP.equals(memoryPoolBean.getType()) ? "heap" : "nonheap"; //G1 Eden Space, G1 Old Gen, 
	            System.out.println(area + ": " + memoryPoolBean.getName());
	            
	            if (memoryPoolBean.getName().equals("G1 Eden Space"))
	            {
	            	MemoryUsage memoryUsage = memoryPoolBean.getUsage();
	            	memoryByTypes[0] = new MemoryByType("G1 Eden Space", memoryUsage.getUsed()/(1024*1024), memoryUsage.getCommitted()/(1024*1024));
	            } else if (memoryPoolBean.getName().equals("G1 Old Gen"))
	            {
	            	MemoryUsage memoryUsage = memoryPoolBean.getUsage();
	            	memoryByTypes[1] = new MemoryByType("G1 Old Gen", memoryUsage.getUsed()/(1024*1024), memoryUsage.getCommitted()/(1024*1024));
	            }

	      }
	      DecimalFormat df = new DecimalFormat("#####0.0");
	      df.setRoundingMode(RoundingMode.DOWN);
	      
	      maxMemoryMb = Runtime.getRuntime().maxMemory()/(1024*1024);
	      availableRamMb = Double.parseDouble(df.format(((Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()))/(double) (1024*1024))));
	}
	

	public class MemoryByType {
		@JsonProperty
		private String type;
		
		@JsonProperty
		private long memoryUsedMb;
		
		@JsonProperty
		private long memoryCommitedMb;
		
		@JsonCreator
		public MemoryByType(final String type, final long memoryUsedMb, final long memoryCommitedMb) {
			this.type = type;
			this.memoryCommitedMb = memoryCommitedMb;
			this.memoryUsedMb = memoryUsedMb;
		}
		
	}
	      
}
