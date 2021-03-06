package edu.duke.cs.tpie;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import edu.duke.cs.tpie.FIFOQueue.Entry;
import edu.duke.cs.tpie.OffHeap.ClosedException;

public class TestFIFOQueue extends TestBase {
	
	@Test
	public void createCloseOne() {
		useTPIE(() -> {
			FIFOQueue q = new FIFOQueue(EntrySize.Bytes8);
			q.close();
			assertThat(q.isClosed(), is(true));
			assertCleanedUp();
		});
	}
	
	@Test
	public void tryCreateOne() {
		useTPIE(() -> {
			try (FIFOQueue q = new FIFOQueue(EntrySize.Bytes8)) {}
			assertCleanedUp();
		});
	}
	
	@Test
	public void createOneThenGC() {
		useTPIE(() -> {
			new FIFOQueue(EntrySize.Bytes8);
			// don't explicitly close, let GC handle it
			doGC();
			assertCleanedUp();
		});
	}
	
	private static FIFOQueue leakedq = null;
	
	@Test
	public void createOneCleanAfterUse() {
		assert (leakedq == null);
		useTPIE(() -> {
			leakedq = new FIFOQueue(EntrySize.Bytes8);
		});
		leakedq = null;
		doGC();
	}
	
	@Test(expected=ClosedException.class)
	public void createOneDontClean() {
		assert (leakedq == null);
		try {
			useTPIE(() -> {
				leakedq = new FIFOQueue(EntrySize.Bytes8);
			});
			leakedq.front();
		} finally {
			leakedq = null;
		}
	}
	
	@Test
	public void pushFront8() {
		useTPIE(() -> {
			try (FIFOQueue q = new FIFOQueue(EntrySize.Bytes8)) {
			
				Entry entry = q.new Entry();
				entry.data.putDouble(3.141592654);
				entry.data.clear();
				q.push(entry);
				
				Entry entry2 = q.front();
				assertThat(entry2.data.getDouble(), is(3.141592654));
			}
		});
	}
	
	private static interface EntryFactory {
		Entry make(double data);
	}
	
	private static interface EntryDoubleChecker {
		void check(Entry entry, double data);
	}
	
	@Test
	public void fifo() {
		useTPIE(() -> {
			try (FIFOQueue q = new FIFOQueue(EntrySize.Bytes8)) {
			
				EntryFactory f = (data) -> {
					Entry entry = q.new Entry();
					entry.data.putDouble(data);
					entry.data.clear();
					return entry;
				};
				
				q.push(f.make(5.3));
				q.push(f.make(4.2));
				q.push(f.make(7.3));
				q.push(f.make(2.9));
				q.push(f.make(1.0));
				q.push(f.make(8.5));
				
				assertThat(q.size(), is(6L));
				assertThat(q.empty(), is(false));
				
				EntryDoubleChecker c = (e, data) -> {
					assertThat(e.data.getDouble(), is(data));
				};
				
				c.check(q.front(), 5.3);
				q.pop();
				c.check(q.front(), 4.2);
				q.pop();
				c.check(q.front(), 7.3);
				q.pop();
				c.check(q.front(), 2.9);
				q.pop();
				c.check(q.front(), 1.0);
				q.pop();
				c.check(q.front(), 8.5);
				q.pop();
				
				assertThat(q.size(), is(0L));
				assertThat(q.empty(), is(true));
			}
		});
	}
	
	@Test
	public void dataSizes() {
		useTPIE(() -> {
			for (EntrySize size : EntrySize.values()) {
				try (FIFOQueue q = new FIFOQueue(size)) {
					
					Entry entry = q.new Entry();
					entry.data.putInt(0, 0x12345678);
					entry.data.putInt(size.numBytes - 4, 0x98765432);
					
					q.push(entry);
					
					Entry entry2 = q.front();
					String msg = "size: " + size;
					assertThat(msg, entry2.data.getInt(0), is(0x12345678));
					assertThat(msg, entry2.data.getInt(size.numBytes - 4), is(0x98765432));
				}
			}
		});
	}
	
	@Test(expected=Exception.class)
	public void empty() {
		useTPIE(() -> {
			try (FIFOQueue q = new FIFOQueue(EntrySize.Bytes8)) {
				q.pop();
			}
		});
	}

	// this test can do bad things to your computer when it fails
	// only run it if you're closely monitoring the process
	// and can kill it if things get out of hand
	//@Test
	public void checkForMemoryLeaks() {
		useTPIE(() -> {
			try (FIFOQueue q = new FIFOQueue(EntrySize.Bytes256)) {
				
				// do this a lot
				for (long i=0; i<4e6; i++) {
					
					// push an entry, check it, then pop
					q.push(q.new Entry());
					q.front();
					q.pop();
				}
			}
		});
	}
}
