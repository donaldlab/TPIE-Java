package edu.duke.cs.tpie.serialization;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

import edu.duke.cs.tpie.EntrySize;
import edu.duke.cs.tpie.TestBase;
import edu.duke.cs.tpie.serialization.SerializingDoublePriorityQueue.Serializer;

public class TestSerializaingDoublePriorityQueue extends TestBase {
	
	@Test
	public void test() {
		
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
		
		useTPIE(() -> {
			try (SerializingDoublePriorityQueue<Thing> q = new SerializingDoublePriorityQueue<>(serializer)) {
			
				q.push(new Thing(4.2, 0));
				q.push(new Thing(0.1, 5));
				q.push(new Thing(9.9, Long.MIN_VALUE));
				q.push(new Thing(5.0, Long.MAX_VALUE));
				
				assertThat(q.empty(), is(false));
				assertThat(q.size(), is(4L));
				
				assertThat(q.top().priority, is(0.1));
				assertThat(q.top().num, is(5L));
				q.pop();
				
				assertThat(q.top().priority, is(4.2));
				assertThat(q.top().num, is(0L));
				q.pop();
				
				assertThat(q.top().priority, is(5.0));
				assertThat(q.top().num, is(Long.MAX_VALUE));
				q.pop();
				
				assertThat(q.top().priority, is(9.9));
				assertThat(q.top().num, is(Long.MIN_VALUE));
				q.pop();
				
				assertThat(q.empty(), is(true));
				assertThat(q.size(), is(0L));
			}
		});
	}
}
