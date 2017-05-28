package edu.duke.cs.tpie;

/**
 * Fixed size to use for queue entries.
 * 
 * @see DoublePriorityQueue
 * @see FIFOQueue
 */
public enum EntrySize {
	
	Bytes8(8),
	Bytes16(16),
	Bytes32(32),
	Bytes64(64),
	Bytes128(128),
	Bytes256(256),
	Bytes512(512),
	Bytes1024(1024);
	
	/**
	 * The number of bytes to allocate for each queue entry.
	 */
	public final int numBytes;
	
	private EntrySize(int numBytes) {
		this.numBytes = numBytes;
	}
	
	public static EntrySize findBigEnoughSizeFor(int numBytes) {
		for (EntrySize size : EntrySize.values()) {
			if (size.numBytes >= numBytes) {
				return size;
			}
		}
		return null;
	}
}
