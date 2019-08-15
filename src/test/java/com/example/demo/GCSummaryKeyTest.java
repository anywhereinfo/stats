package com.example.demo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.example.demo.GCStats.GCSummaryKey;

public class GCSummaryKeyTest {

	@Test
	public void test() {
		String gcCause = "G1 Evacuation Pause";
		String gcAction = "end of minor GC";
		GCSummaryKey key =  new GCSummaryKey(gcCause, gcAction);
		GCSummaryKey key1 =new GCSummaryKey(gcCause, gcAction);
		assertEquals(key1, key);

		
	}

}
