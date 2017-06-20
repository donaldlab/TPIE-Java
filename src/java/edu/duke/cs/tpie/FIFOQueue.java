package edu.duke.cs.tpie;

import java.nio.ByteBuffer;

/**
 * A first-in-first-out queue whose entries have ByteBuffer payloads.
 * <p>
 * This implementation wraps the <a href="http://madalgo.au.dk/tpie/doc/master/classtpie_1_1queue.html">tpie::queue</a> type.
 */
public class FIFOQueue extends OffHeap {
	
	private static native long create(int numBytes);
	private static native void cleanup(long handle);
	private static native void push(long handle, Entry entry);
	private static native Entry front(long handle, FIFOQueue queue);
	private static native void pop(long handle);
	private static native long size(long handle);
	private static native boolean empty(long handle);

	/**
	 * An entry for the FIFO queue.
	 */
	public class Entry {
		
		/**
		 * Payload for the entry.
		 * <p>
		 * Payload buffers are always fixed size, and are determined by the {@link EntrySize} value passed to {@link DoublePriorityQueue#DoublePriorityQueue(EntrySize)}.
		 */
		public ByteBuffer data;
		
		public Entry() {
			this.data = ByteBuffer.allocate(entrySize.numBytes);
		}
	}
	
	/**
	 * Size of the entries in this queue.
	 */
	public final EntrySize entrySize;
	
	/**
	 * Create a FIFO queue whose entries will be the specified size.
	 * <p>
	 * Be sure to call {@link TPIE#start(int)} before allocating any TPIE data
	 * structures to set shared internal memory limits.
	 */
	public FIFOQueue(EntrySize entrySize) {
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
	 * Retrieve the first entry from the queue.
	 * <p>
	 * Does not modify the queue.
	 */
	public Entry front() {
		checkClosed();
		return front(getHandle(), this);
	}
	
	/**
	 * Remove the first entry from the queue.
	 * 
	 * See {@link #front()}
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
