package edu.duke.cs.tpie.serialization;

import java.nio.ByteBuffer;

import edu.duke.cs.tpie.EntrySize;
import edu.duke.cs.tpie.FIFOQueue;
import edu.duke.cs.tpie.FIFOQueue.Entry;
import edu.duke.cs.tpie.OffHeapWrapper;

/**
 * Wrapper for {@link FIFOQueue} that automatically serializes and deserializes Java types. 
 */
public class SerializingFIFOQueue<T> extends OffHeapWrapper {
	
	/**
	 * Serializes/deserializes instances of {@code T} to/from a {@link ByteBuffer}.
	 * @param <T> Any Java class
	 */
	public static interface Serializer<T> {
		
		/**
		 * Returns the maximum size of {@link ByteBuffer} needed for serialization.
		 */
		EntrySize getEntrySize();
		
		/**
		 * Serialize {@code val} into {@code buf}.
		 * @param val The source object
		 * @param buf The destination buffer
		 */
		void serialize(T val, ByteBuffer buf);

		/**
		 * Deserialize an instance of {@code T} from {@code buf}.
		 * @param buf The source buffer
		 * @return An instance of {@code T} represented by the given data in {@code buf}.
		 */
		T deserialize(ByteBuffer buf);
	}
	
	public final Serializer<T> serializer;
	private FIFOQueue queue;
	
	/**
	 * @param serializer Instance of {@link Serializer} that can serialize and deserialize instances of {@code T}.
	 */
	public SerializingFIFOQueue(Serializer<T> serializer) {
		this.serializer = serializer;
		this.queue = new FIFOQueue(serializer.getEntrySize());
		this.setWrapped(this.queue);
	}

	/**
	 * Adds {@code val} to the queue.
	 */
	public void push(T val) {
		Entry entry = queue.new Entry();
		serializer.serialize(val, entry.data);
		queue.push(entry);
	}

	/**
	 * Retrieve the first entry from the queue.
	 * <p>
	 * Does not modify the queue.
	 */
	public T front() {
		Entry entry = queue.front();
		return serializer.deserialize(entry.data);
	}

	/**
	 * Remove the first entry from the queue.
	 * 
	 * See {@link #front()}
	 */
	public void pop() {
		queue.pop();
	}
	
	/**
	 * Get the number of elements currently in the queue.
	 * <p>
	 * Since TPIE queues use external memory, some of the entries may be currently residing on disk.
	 */
	public long size() {
		return queue.size();
	}
	
	/**
	 * Return true if the queue contains no entries.
	 */
	public boolean empty() {
		return queue.empty();
	}
}
