package edu.duke.cs.tpie;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import edu.duke.cs.tpie.Cleaner.Cleanable;
import edu.duke.cs.tpie.Cleaner.GarbageDetectable;


public abstract class OffHeap implements GarbageDetectable, Closeable {
	
	public static class ClosedException extends RuntimeException {
		
		private static final long serialVersionUID = 5770897278934983267L;

		public ClosedException() {
			super("Off-heap resources have been closed. This object can no longer be used.");
		}
	}
	
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
	
	private static List<WeakReference<OffHeap>> liveRefs = new ArrayList<>();
	
	public static void cleanAll() {
		for (WeakReference<OffHeap> ref : liveRefs) {
			OffHeap obj = ref.get();
			if (obj != null) {
				obj.close();
			}
		}
		liveRefs.clear();
	}
	
	private HandleCleaner cleaner;
	
	protected OffHeap(long handle, Consumer<Long> cleanup) {
		
		cleaner = Cleaner.addCleaner(this, new HandleCleaner(handle, cleanup));
		
		// keep a weak ref to this obj
		// it won't keep the cleaner from working correctly, and it will let us
		// forcibly clean any dangling objects when we call TPIE.stop()
		// TPIE will segfault if we access any objects after finishing TPIE
		liveRefs.add(new WeakReference<>(this));
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
	
	public void checkClosed() {
		if (isClosed()) {
			throw new ClosedException();
		}
	}
}
