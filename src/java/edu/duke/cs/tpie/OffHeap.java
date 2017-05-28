package edu.duke.cs.tpie;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import edu.duke.cs.tpie.Cleaner.Cleanable;
import edu.duke.cs.tpie.Cleaner.GarbageDetectable;


public abstract class OffHeap implements GarbageDetectable, Closeable {
	
	private static class HandleCleaner implements Cleanable {
		
		private AtomicLong handle;
		private Consumer<Long> cleanup;
		
		public HandleCleaner(long handle, Consumer<Long> cleanup) {
			this.handle = new AtomicLong(handle);
			this.cleanup = cleanup;
		}

		@Override
		public void clean() {
			long h = handle.getAndSet(-1);
			if (h >= 0) {
				cleanup.accept(h);
			}
		}
		
		public boolean isCleaned() {
			return handle.get() < 0;
		}
	}
	
	private HandleCleaner cleaner;
	
	protected OffHeap(long handle, Consumer<Long> cleanup) {
		cleaner = Cleaner.addCleaner(this, new HandleCleaner(handle, cleanup));
	}
	
	protected long getHandle() {
		return cleaner.handle.get();
	}

	/**
	 * Reclaim off-heap memory used by this instance.
	 */
	@Override
	public void close() {
		cleaner.clean();
	}
	
	public boolean isClosed() {
		return cleaner.isCleaned();
	}
}
