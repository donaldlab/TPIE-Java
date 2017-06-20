package edu.duke.cs.tpie;

import java.nio.ByteBuffer;

/**
 * A priority queue whose entries have double-valued keys and ByteBuffer payloads.
 * Entries are retrieved from the queue such that entries with the smallest priorities are returned first.
 * <p>
 * This implementation wraps the <a href="http://madalgo.au.dk/tpie/doc/master/classtpie_1_1priority__queue.html">tpie::priority_queue</a> type.
 * 
 * @see <a href="http://madalgo.au.dk/tpie/doc/master/priority_queue.html">TPIE Priority queue example</a>
 */
public class DoublePriorityQueue extends OffHeap {
	
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
	
	/**
	 * Size of the entries in this queue.
	 */
	public final EntrySize entrySize;
	
	/**
	 * Create a priority queue whose entries will be the specified size.
	 * <p>
	 * Be sure to call {@link TPIE#start(int)} before allocating any TPIE data
	 * structures to set shared internal memory limits.
	 */
	public DoublePriorityQueue(EntrySize entrySize) {
		super(create(entrySize.numBytes), (handle) -> cleanup(handle));
		this.entrySize = entrySize;
	}
	
	/**
	 * Add an entry to the queue.
	 */
	public void push(Entry entry) {
		checkClosed();
		push(getHandle(), entry);
	}
	
	/**
	 * Retrieve the entry from the queue with the smallest priority value.
	 * <p>
	 * Does not modify the queue.
	 */
	public Entry top() {
		checkClosed();
		return top(getHandle(), this);
	}
	
	/**
	 * Remove the top entry from the queue.
	 * 
	 * See {@link #top()}
	 */
	public void pop() {
		checkClosed();
		pop(getHandle());
	}
	
	/**
	 * Get the number of elements currently in the queue.
	 * <p>
	 * Since TPIE queues use external memory, some of the entries may be currently residing on disk.
	 */
	public long size() {
		checkClosed();
		return size(getHandle());
	}
	
	/**
	 * Return true if the queue contains no entries.
	 */
	public boolean empty() {
		checkClosed();
		return empty(getHandle());
	}
}
