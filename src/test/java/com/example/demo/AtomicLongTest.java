package com.example.demo;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class AtomicLongTest {

	@Test
	public void test() {
		AtomicLong counter = new AtomicLong(0L);
		for (int i = 1; i <= 15; i++)
		{
			long l = counter.incrementAndGet();
			System.out.println(l);
			if(l%5 == 0) 
			{
				counter.set(0L);
			}
		}

		
		assertTrue(counter.get() == 0);
	}

}
