package edu.duke.cs.tpie;

import java.io.Closeable;

public abstract class OffHeapWrapper implements Closeable {
	
	private OffHeap thing;
	
	protected void setWrapped(OffHeap thing) {
		this.thing = thing;
	}
	
	public boolean isClosed() {
		return thing.isClosed();
	}
	
	public void close() {
		thing.close();
	}
}
