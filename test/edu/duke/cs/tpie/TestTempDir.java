package edu.duke.cs.tpie;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Random;

import org.junit.Test;

public class TestTempDir extends TestBase {
	
	@Test
	public void setDir() {
		TPIE.setTempDir("/tmp");
	}
	
	@Test
	public void setSubdir()
	throws IOException {
		
		// pick a random name for the subdir
		String subdirName = "tpie-test-" + Math.abs(new Random().nextInt());

		// and make sure it doesn't exist
		Path subdir = Paths.get("/tmp", subdirName);
		deleteDir(subdir);
		
		try {
			
			TPIE.setTempDir("/tmp", subdirName);
			
			// it should exist now
			assertThat(Files.exists(subdir), is(true));
			
		} finally {
			deleteDir(subdir);
		}
	}
	
	private void deleteDir(Path dir)
	throws IOException {
		
		if (!Files.exists(dir)) {
			return;
		}
		
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
