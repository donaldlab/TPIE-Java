package edu.duke.cs.tpie;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import edu.duke.cs.tpie.DoublePriorityQueue.Entry;

public class TestDoublePriorityQueue extends TestBase {
	
	@Test
	public void createCloseOne() {
		DoublePriorityQueue q = new DoublePriorityQueue(EntrySize.Bytes8);
		q.close();
		assertThat(q.isClosed(), is(true));
		assertCleanedUp();
	}
	
	@Test
	public void tryCreateOne() {
		try (DoublePriorityQueue q = new DoublePriorityQueue(EntrySize.Bytes8)) {}
		assertCleanedUp();
	}
	
	@Test
	@SuppressWarnings("resource")
	public void createOneThenGC() {
		new DoublePriorityQueue(EntrySize.Bytes8);
		// don't explicitly close, let GC handle it
		doGC();
		assertCleanedUp();
	}
	
	@Test
	public void pushTop8() {
		try (DoublePriorityQueue q = new DoublePriorityQueue(EntrySize.Bytes8)) {
		
			Entry entry = q.new Entry();
			entry.priority = 1.0;
			entry.data.putDouble(3.141592654);
			entry.data.clear();
			q.push(entry);
			
			Entry entry2 = q.top();
			assertThat(entry2.priority, is(entry.priority));
			assertThat(entry2.data.getDouble(), is(3.141592654));
		}
	}
	
	private static interface EntryDoubleFactory {
		Entry make(double priority, double data);
	}
	
	private static interface EntryDoubleChecker {
		void check(Entry entry, double priority, double data);
	}
	
	@Test
	public void sort() {
		try (DoublePriorityQueue q = new DoublePriorityQueue(EntrySize.Bytes8)) {
		
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
		}
	}
	
	@Test
	public void dataSizes() {
		for (EntrySize size : EntrySize.values()) {
			try (DoublePriorityQueue q = new DoublePriorityQueue(size)) {
				
				Entry entry = q.new Entry();
				entry.priority = 5;
				entry.data.putInt(0, 0x12345678);
				entry.data.putInt(size.numBytes - 4, 0x98765432);
				
				q.push(entry);
				
				Entry entry2 = q.top();
				String msg = "size: " + size;
				assertThat(msg, entry2.data.getInt(0), is(0x12345678));
				assertThat(msg, entry2.data.getInt(size.numBytes - 4), is(0x98765432));
			}
		}
	}
	
	@Test(expected=Exception.class)
	public void empty() {
		try (FIFOQueue q = new FIFOQueue(EntrySize.Bytes8)) {
			q.pop();
		}
	}
}
