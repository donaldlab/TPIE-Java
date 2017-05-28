package edu.duke.cs.tpie;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.duke.cs.tpie.DoublePriorityQueue.Entry;

public class TestDoublePriorityQueue {
	
	private static void doGC() {
		
		// hint that the GC should run
		System.gc();
		
		// sleep the thread to make a good time for gc to run
		// usually this works, but there are no guarantees
		try {
			Thread.sleep(10);
		} catch (InterruptedException ex) {
			// java's checked exceptions are really starting to get annoying...
			throw new Error(ex);
		}
	}
	
	@BeforeClass
	public static void beforeClass() {
		TPIE.start(16);
	}
	
	@Test
	public void createCleanupOne()
	throws Exception {
		new DoublePriorityQueue(EntrySize.Bytes8).cleanup();
	}
	
	@Test
	public void createOneThenGC()
	throws Exception {
		new DoublePriorityQueue(EntrySize.Bytes8).cleanup();
		doGC();
	}
	
	@Test
	public void createCleanupMany()
	throws Exception {
		for (EntrySize size : EntrySize.values()) {
			new DoublePriorityQueue(size).cleanup();
		}
	}
	
	@Test
	public void createCleanupManyThenGC()
	throws Exception {
		for (EntrySize size : EntrySize.values()) {
			new DoublePriorityQueue(size).cleanup();
		}
		doGC();
	}
	
	@Test
	public void pushTop8() {
		
		DoublePriorityQueue q = new DoublePriorityQueue(EntrySize.Bytes8);
		
		Entry entry = q.new Entry();
		entry.priority = 1.0;
		entry.data.putDouble(3.141592654);
		entry.data.clear();
		q.push(entry);
		
		Entry entry2 = q.top();
		assertThat(entry2.priority, is(entry.priority));
		assertThat(entry2.data.getDouble(), is(3.141592654));
		
		q.cleanup();
	}
	
	private static interface EntryDoubleFactory {
		Entry make(double priority, double data);
	}
	
	private static interface EntryDoubleChecker {
		void check(Entry entry, double priority, double data);
	}
	
	@Test
	public void sort() {
		
		DoublePriorityQueue q = new DoublePriorityQueue(EntrySize.Bytes8);
		try {
		
			EntryDoubleFactory f = (priority, data) -> {
				Entry entry = q.new Entry();
				entry.priority = priority;
				entry.data.putDouble(data);
				entry.data.clear();
				return entry;
			};
			
			q.push(f.make(9.0, 5.3));
			q.push(f.make(3.0, 4.2));
			q.push(f.make(5.0, 7.3));
			q.push(f.make(7.0, 2.9));
			q.push(f.make(4.0, 1.0));
			q.push(f.make(2.0, 8.5));
			
			assertThat(q.size(), is(6L));
			assertThat(q.empty(), is(false));
			
			EntryDoubleChecker c = (e, priority, data) -> {
				assertThat(e.priority, is(priority));
				assertThat(e.data.getDouble(), is(data));
			};
			
			c.check(q.top(), 2.0, 8.5);
			q.pop();
			c.check(q.top(), 3.0, 4.2);
			q.pop();
			c.check(q.top(), 4.0, 1.0);
			q.pop();
			c.check(q.top(), 5.0, 7.3);
			q.pop();
			c.check(q.top(), 7.0, 2.9);
			q.pop();
			c.check(q.top(), 9.0, 5.3);
			q.pop();
			
			assertThat(q.size(), is(0L));
			assertThat(q.empty(), is(true));
		
		} finally {
			q.cleanup();
		}
	}
	
	@Test
	public void dataSizes() {
		for (EntrySize size : EntrySize.values()) {
			DoublePriorityQueue q = new DoublePriorityQueue(size);
			try {
				
				Entry entry = q.new Entry();
				entry.priority = 5;
				entry.data.putInt(0, 0x12345678);
				entry.data.putInt(size.numBytes - 4, 0x98765432);
				
				q.push(entry);
				
				Entry entry2 = q.top();
				String msg = "size: " + size;
				assertThat(msg, entry2.data.getInt(0), is(0x12345678));
				assertThat(msg, entry2.data.getInt(size.numBytes - 4), is(0x98765432));
				
			} finally {
				q.cleanup();
			}
		}
	}
}
