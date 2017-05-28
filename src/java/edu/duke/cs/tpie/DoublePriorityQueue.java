package edu.duke.cs.tpie;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import sun.misc.Cleaner;

/**
 * A priority queue whose entries have double-valued keys and ByteBuffer payloads.
 * Entries are retrieved from the queue such that entries with the smallest priorities are returned first.
 * <p>
 * This implementation wraps the <a href="http://madalgo.au.dk/tpie/doc/master/classtpie_1_1priority__queue.html">tpie::priority_queue</a> type.
 * 
 * @see <a href="http://madalgo.au.dk/tpie/doc/master/priority_queue.html">TPIE Priority queue example</a>
 */
public class DoublePriorityQueue {
	
	private static native long create(int numBytes);
	private static native void cleanup(long handle);
	private static native void push(long handle, Entry entry);
	private static native Entry top(long handle, DoublePriorityQueue queue);
	private static native void pop(long handle);
	private static native long size(long handle);
	private static native boolean empty(long handle);
	
	/**
	 * An entry for the priority queue.
	 */
	public class Entry {
		
		/**
		 * Priority for the entry.
		 * <p>
		 * Entries are ordered such that the smallest values are retrieved first from the queue.
		 */
		public double priority;
		
		/**
		 * Payload for the entry.
		 * <p>
		 * Payload buffers are always fixed size, and are determined by the {@link EntrySize} value passed to {@link DoublePriorityQueue#DoublePriorityQueue(EntrySize)}.
		 */
		public ByteBuffer data;
		
		public Entry() {
			this.priority = 0;
			this.data = ByteBuffer.allocate(entrySize.numBytes);
		}
	}
	
	private EntrySize entrySize;
	private AtomicLong handle;
	
	private Consumer<AtomicLong> cleanup = (AtomicLong handle) -> {
		long h = handle.getAndSet(-1);
		if (h >= 0) {
			cleanup(h);
		}
	};
	
	/**
	 * Create a priority queue whose entries will be the specified size.
	 * <p>
	 * Be sure to call {@link TPIE#start(int)} before allocating any TPIE data
	 * structures to set shared internal memory limits.
	 */
	public DoublePriorityQueue(EntrySize entrySize) {
		
		AtomicLong handle = new AtomicLong(create(entrySize.numBytes));
		Cleaner.create(this, () -> cleanup.accept(handle));
		
		this.entrySize = entrySize;
		this.handle = handle;
	}
	
	/**
	 * Reclaim off-heap memory used by this queue.
	 */
	public void cleanup() {
		cleanup.accept(handle);
	}
	
	/**
	 * Add an entry to the queue.
	 */
	public void push(Entry entry) {
		push(handle.get(), entry);
	}
	
	/**
	 * Retrieve the entry from the queue with the smallest priority value.
	 * <p>
	 * Does not modify the queue.
	 */
	public Entry top() {
		return top(handle.get(), this);
	}
	
	/**
	 * Remove the top entry from the queue.
	 * 
	 * See {@link #top()}
	 */
	public void pop() {
		pop(handle.get());
	}
	
	/**
	 * Get the number of elements currently in the queue.
	 * <p>
	 * Since TPIE queues use external memory, some of the entries may be currently residing on disk.
	 */
	public long size() {
		return size(handle.get());
	}
	
	/**
	 * Return true if the queue contains no entries.
	 */
	public boolean empty() {
		return empty(handle.get());
	}
}
