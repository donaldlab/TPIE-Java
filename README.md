
# TPIE-Java

*glue code to use [TPIE][tpie] in Java projects*

[tpie]: http://madalgo.au.dk/tpie

TPIE-Java was created by the [Donald Lab][dlab] in the [Department of Computer Science][cs] at [Duke University][duke].

[dlab]: http://www.cs.duke.edu/donaldlab/
[cs]: http://www.cs.duke.edu/
[duke]: https://www.duke.edu/


## Platforms

TPIE-Java is currently supported on Linux platforms.

The C++ source code should be portable though, so supporting other platforms should just be a matter of compiling the native library and publishing the binaries.


## Features

TPIE-Java currently implements a subset of TPIE features:

1. FIFO Queues
2. Priority Queues


## Fixed-length queue entries

Native applications using TPIE can use TPIE's elegant entry serialization powered by the C++ compiler. Since information about Java types is obviously not availble to the C++ compiler, we must serialize Java types at runtime into byte buffers before sending them to TPIE. Sadly, the current implementation of TPIE qeues support only fixed-length entries, so we must serialize Java type information into fixed-length buffers.

TPIE-Java supports fixed-length buffers in the following (arbitrary) sizes:

 * 8 bytes
 * 16 bytes
 * 32 bytes
 * 64 bytes
 * 128 bytes
 * 256 bytes
 * 512 bytes
 * 1024 bytes

These sizes won't be optimal for all purposes, but they should enable external memory queues for a wide variety of Java applications without wasting too much space.

Supporting other sizes is possible with modifications to the native library within TPIE-Java, but each implemented size increases the size of the shared library, since the templated C++ code must be compiled separately for each entry size.

## Download

Downloads can be found on the [Releases][releases] page.

[releases]: TPIE-Java/releases


## Getting started

[Download][releases] the latest jar and add it to your project's classpath. Adding the `javadoc` and `sources` jars to your IDE can also be helpful.

Eventually, we can host a maven repo for TPIE-Java which will let your build tool (e.g., Jerkar, Maven, Gradle) handle depedencies automatically, but for now, you'll have to add the dependencies to your classpath manually:

 * [commons-io:commons-io:2.5](http://search.maven.org/remotecontent?filepath=commons-io/commons-io/2.5/commons-io-2.5.jar)
 * [org.apache.commons:commons-collections4:4.1](http://search.maven.org/remotecontent?filepath=org/apache/commons/commons-collections4/4.1/commons-collections4-4.1.jar)


### Raw queues

The following code snippet shows how to use the double priority queue.

```java
// initialize TPIE and set the internal memory limit in MiB.
TPIE.start(128);

// create a priority queue with a payload size of 8 bytes,
// but do it in a try-with-resources block.
// TPIE-Java objects use off-heap memory that needs to be
// explicitly cleaned up when we're done using them.
try (DoublePriorityQueue q = new DoublePriorityQueue(EntrySize.Bytes8)) {
    
    // add an entry to the queue
    Entry entry = q.new Entry();
    entry.priority = 5.0;
    entry.data.putLong(42);
    q.push(entry);
    
    // read it back
    Entry entryAgain = q.top();
    assert (entryAgain.priority == 5.0);
    assert (entryAgain.data.getLong() == 42);
    
    // pop the queue
    q.pop();
}
```


### Serializing queues

The following code snippet shows how to use the serializing double priority queue with a custom `Thing` type and a custom `Serializer`.

```java
class Thing {
	
	public final double priority;
	public final long num;
	
	public Thing(double priority, long num) {
		this.priority = priority;
		this.num = num;
	}
}

Serializer<Thing> serializer = new Serializer<Thing>() {

	@Override
	public EntrySize getEntrySize() {
		return EntrySize.Bytes8;
	}

	@Override
	public double serialize(Thing val, ByteBuffer buf) {
		buf.putLong(val.num);
		return val.priority;
	}

	@Override
	public Thing deserialize(double priority, ByteBuffer buf) {
		return new Thing(priority, buf.getLong());
	}
};

try (SerializingDoublePriorityQueue<Thing> q = new SerializingDoublePriorityQueue<>(serializer)) {

	q.push(new Thing(4.2, 0));
	q.push(new Thing(0.1, 5));
	q.push(new Thing(9.9, Long.MIN_VALUE));
	q.push(new Thing(5.0, Long.MAX_VALUE));
	
	assert(q.top().priority == 0.1);
	q.pop();
	assert(q.top().priority == 4.2);
	q.pop();
	assert(q.top().priority == 5.0;
	q.pop();
	assert(q.top().priority == 9.9);
	q.pop();
}
```

## License

TPIE-Java is published under the LGPL 3.0 license. See [LICENSE.md](LICENSE.md) for the complete license.

TPIE-Java is open source. Contributions are welcome!



## Compiling

First, clone this repo from GitHub.

### C++ side

This project is a bit of a hack job, so the native code uses a very simple custom Makefile.
The Makefile makes a lot of assumptions:

 * You're running on a Linux platform
 * A Java 8 JDK (amd64 arch) is installed in the default location for Debian-based distros
 * You've [compiled TPIE from source][compile-tpie] into the folder `../tpie`

NOTE: make sure you compile TPIE using `-fPIC` so we can include TPIE into a shared library.

[compile-tpie]: http://madalgo.au.dk/tpie/doc/master/setup.html

If all that is actually true for your system, then you can compile the native code without much hassle.

To compile the native code, run:
```
$ make
```
The compiled `tpie-java.so` should appear in `build/natives`.

If you don't meet the prerequisites, you can try editing the settings at the top of the Makefile until it works?
I'm sure using a real C++ build system could make this process much easier though. Contributions are welcome!


### Java side

The Java code is compiled using the included [Jerkar][jerkar] build system. Just run the shell script:

[jerkar]: http://project.jerkar.org

```
$ ./jerkar doPack
```
Dependencies will be downloaded and included automatically. When it's done, you can find the skinny jar, javadocs, and sources at `build/output`.





