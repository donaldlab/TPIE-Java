package edu.duke.cs.tpie.serialization;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

import edu.duke.cs.tpie.EntrySize;
import edu.duke.cs.tpie.TestBase;
import edu.duke.cs.tpie.serialization.SerializingFIFOQueue.Serializer;

public class TestSerializaingFIFOQueue extends TestBase {
	
	@Test
	public void test() {
		
		class Thing {
			
			public final long num;
			
			public Thing(long num) {
				this.num = num;
			}
		}
		
		Serializer<Thing> serializer = new Serializer<Thing>() {

			@Override
			public EntrySize getEntrySize() {
				return EntrySize.Bytes8;
			}

			@Override
			public void serialize(Thing val, ByteBuffer buf) {
				buf.putLong(val.num);
			}

			@Override
			public Thing deserialize(ByteBuffer buf) {
				return new Thing(buf.getLong());
			}
		};
		
		useTPIE(() -> {
			try (SerializingFIFOQueue<Thing> q = new SerializingFIFOQueue<>(serializer)) {
			
				q.push(new Thing(0));
				q.push(new Thing(5));
				q.push(new Thing(Long.MIN_VALUE));
				q.push(new Thing(Long.MAX_VALUE));
				
				assertThat(q.empty(), is(false));
				assertThat(q.size(), is(4L));
				
				assertThat(q.front().num, is(0L));
				q.pop();
				assertThat(q.front().num, is(5L));
				q.pop();
				assertThat(q.front().num, is(Long.MIN_VALUE));
				q.pop();
				assertThat(q.front().num, is(Long.MAX_VALUE));
				q.pop();
				
				assertThat(q.empty(), is(true));
				assertThat(q.size(), is(0L));
			}
		});
	}
}
