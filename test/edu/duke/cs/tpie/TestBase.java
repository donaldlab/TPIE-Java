package edu.duke.cs.tpie;

import org.junit.BeforeClass;

public class TestBase {
	
	@BeforeClass
	public static void beforeClass() {
		TPIE.start(16);
	}
	
	protected static void doGC() {
		
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
	
	protected void assertCleanedUp() {
		// then try to make another queue
		// if we didn't cleanup everything before, this will fail
		// because we'll be out of internal memory available to TPIE
		// (we're giving TPIE the smallest amount of internal memory it will accept,
		// so I guess that's room enough for one data structure)
		new DoublePriorityQueue(EntrySize.Bytes8).close();
	}
}
