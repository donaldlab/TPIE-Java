package edu.duke.cs.tpie.serialization;

import java.nio.ByteBuffer;

import edu.duke.cs.tpie.DoublePriorityQueue;
import edu.duke.cs.tpie.DoublePriorityQueue.Entry;
import edu.duke.cs.tpie.EntrySize;
import edu.duke.cs.tpie.OffHeapWrapper;

/**
 * Wrapper for {@link DoublePriorityQueue} that automatically serializes and deserializes Java types. 
 */
public class SerializingDoublePriorityQueue<T> extends OffHeapWrapper {
	
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
		 * Serialize {@code val} into {@code buf} and retrieve its priority.
		 * @param val The source object
		 * @param buf The destination buffer
		 * @return The priority of the source object
		 */
		double serialize(T val, ByteBuffer buf);
		
		/**
		 * Deserialize an instance of {@code T} from {@code buf}.
		 * @param priority The priority of the object
		 * @param buf The source buffer
		 * @return An instance of {@code T} represented by the given {@code priority} and data in {@code buf}.
		 */
		T deserialize(double priority, ByteBuffer buf);
	}
	
	public final Serializer<T> serializer;
	private DoublePriorityQueue queue;
	
	/**
	 * @param serializer Instance of {@link Serializer} that can serialize and deserialize instances of {@code T}.
	 */
	public SerializingDoublePriorityQueue(Serializer<T> serializer) {
		this.serializer = serializer;
		this.queue = new DoublePriorityQueue(serializer.getEntrySize());
		this.setWrapped(this.queue);
	}

	/**
	 * Adds {@code val} to the queue.
	 */
	public void push(T val) {
		Entry entry = queue.new Entry();
		entry.priority = serializer.serialize(val, entry.data);
		queue.push(entry);
	}

	/**
	 * Retrieve the entry from the queue with the smallest priority value.
	 * <p>
	 * Does not modify the queue.
	 */
	public T top() {
		Entry entry = queue.top();
		return serializer.deserialize(entry.priority, entry.data);
	}

	/**
	 * Remove the top entry from the queue.
	 * 
	 * See {@link #top()}
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
