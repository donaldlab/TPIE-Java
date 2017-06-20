package edu.duke.cs.tpie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public class TPIE {
	
	private static boolean isStarted = false;
	
	private static native void init(int internalMiB);
	private static native void cleanup();
	
	/**
	 * Start the TPIE library and configure the size of the internal memory.
	 * 
	 * @param internalMiB Size of the internal memory (in MiB) to reserve exclusively for TPIE.
	 * <p>
	 * All TPIE data structures share this internal memory pool. Data structures needing more memory
	 * than this value will use temporary disk space, rather than attempt to request more internal memory.
	 * <p>
	 * This memory is allocated off of the Java heap and will not be shown by JVM heap profilers.
	 */
	public static void start(int internalMiB) {
		
		if (isStarted) {
			return;
		}
		isStarted = true;
		
		// export the native lib and load it
		// TODO: cross-platform support
		try {
			File libFile = File.createTempFile("libtpie-java", ".so");
			libFile.deleteOnExit();
			try (InputStream in = TPIE.class.getResourceAsStream("libtpie-java.so")) {
				try (OutputStream out = new FileOutputStream(libFile)) {
					IOUtils.copy(in, out);
				}
			}
			System.load(libFile.getAbsolutePath());
			
		} catch (IOException ex) {
			throw new Error(ex);
		}
		
		// tpie doesn't seem to be happy with less than 16 MiB
		internalMiB = Math.max(internalMiB, 16);
		
		init(internalMiB);
		
		// make sure cleanup gets called
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				TPIE.stop();
			}
		});
	}
	
	/**
	 * Stop the TPIE library and let it clean up any unused resources.
	 * <p>
	 * Under normal circumstances, this will be automatically called with the JVM shuts down
	 * and you won't need to call it manually.
	 * <p>
	 * You can call it manually though if you want to cleanup TPIE before the JVM shuts down,
	 * or if for some reason the JVM shutdown hooks aren't operating (e.g., in a JPype environment).
	 */
	public static void stop() {
		if (isStarted) {
			isStarted = false;
			OffHeap.cleanAll();
			cleanup();
		}
	}
	
	public static interface Block {
		void run() throws Exception;
	}
	
	/**
	 * Convenience method to initialize TPIE, run a block of code, and then make sure TPIE
	 * is cleaned up before returning.
	 * 
	 * @param internalMiB internalMiB Size of the internal memory (in MiB) to reserve exclusively for TPIE.
	 * @param block A block of code to run using TPIE.
	 */
	public static void use(int internalMiB, Block block) {
		TPIE.start(internalMiB);
		try {
			block.run();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			TPIE.stop();
		}
	}
	
	/**
	 * Set temporary directory path
	 * 
	 * @param dir
	 */
	public static void setTempDir(String dir) {
		setTempDir(dir, null);
	}
	
	/**
	 * Set temporary directory path of the form dir/subdir.
	 * subdir will be created within dir if necessary.
	 * 
	 * @param dir
	 * @param subdir
	 */
	public static native void setTempDir(String dir, String subdir);
	
	/**
	 * Get the total number of bytes currently used in temporary storage.
	 */
	public static native long getExternalBytes();
}
