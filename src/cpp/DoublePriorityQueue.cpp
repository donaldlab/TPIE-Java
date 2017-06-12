
#include <cstring>
#include <tpie/tpie.h>
#include <tpie/priority_queue.h>
#include "DoublePriorityQueue.hpp"
#include "jni.hpp"


template <int NumBytes>
struct DPQEntry {
	double priority;
	uint8_t bytes[NumBytes];
};

template <int NumBytes>
inline bool operator == (const DPQEntry<NumBytes> & a, const DPQEntry<NumBytes> & b) {
	return a.priority == b.priority;
}

template <int NumBytes>
inline bool operator != (const DPQEntry<NumBytes> & a, const DPQEntry<NumBytes> & b) {
	return a.priority != b.priority;
}

template <int NumBytes>
inline bool operator < (const DPQEntry<NumBytes> & a, const DPQEntry<NumBytes> & b) {
	return a.priority < b.priority;
}

template <int NumBytes>
inline bool operator <= (const DPQEntry<NumBytes> & a, const DPQEntry<NumBytes> & b) {
	return a.priority <= b.priority;
}

template <int NumBytes>
inline bool operator > (const DPQEntry<NumBytes> & a, const DPQEntry<NumBytes> & b) {
	return a.priority > b.priority;
}

template <int NumBytes>
inline bool operator >= (const DPQEntry<NumBytes> & a, const DPQEntry<NumBytes> & b) {
	return a.priority >= b.priority;
}


class IDoublePriorityQueue {
public:

	static IDoublePriorityQueue * make(const uint32_t & num_bytes);

	virtual ~IDoublePriorityQueue() {}

	virtual uint64_t to_handle() const {
		return (uint64_t)this;
	}

	static IDoublePriorityQueue * from_handle(const uint64_t handle) {
		return (IDoublePriorityQueue *)handle;
	}

	virtual uint32_t get_num_bytes() = 0;

	virtual void push(const double & priority, const uint8_t * bytes) = 0;
	virtual double top_priority() = 0;
	virtual const uint8_t * top_bytes() = 0;
	virtual void pop() = 0;
	virtual uint64_t size() = 0;
	virtual bool empty() = 0;
};

template <int NumBytes>
class DoublePriorityQueue : public IDoublePriorityQueue {
public:

	DoublePriorityQueue() : m_num_bytes(NumBytes) {}

	uint32_t get_num_bytes() {
		return m_num_bytes;
	}

	void push(const double & priority, const uint8_t * bytes) {
		DPQEntry<NumBytes> entry;
		entry.priority = priority;
		std::memcpy(entry.bytes, bytes, m_num_bytes);
		m_queue.push(entry);
	}

	double top_priority() {
		return m_queue.top().priority;
	}

	const uint8_t * top_bytes() {
		return m_queue.top().bytes;
	}

	void pop() {
		m_queue.pop();
	}

	uint64_t size() {
		return m_queue.size();
	}

	bool empty() {
		return m_queue.empty();
	}

private:

	uint32_t m_num_bytes;
	tpie::priority_queue<DPQEntry<NumBytes>> m_queue;
};


IDoublePriorityQueue * IDoublePriorityQueue::make(const uint32_t & num_bytes) {
	switch (num_bytes) {
		case     8: return new DoublePriorityQueue<    8>();
		case    16: return new DoublePriorityQueue<   16>();
		case    32: return new DoublePriorityQueue<   32>();
		case    64: return new DoublePriorityQueue<   64>();
		case   128: return new DoublePriorityQueue<  128>();
		case   256: return new DoublePriorityQueue<  256>();
		case   512: return new DoublePriorityQueue<  512>();
		case  1024: return new DoublePriorityQueue< 1024>();
	}
	throw_exception("unsupported entry size for queue: %d", num_bytes);
	return NULL;
}


// java ids

define_class(dpq_entry, "edu/duke/cs/tpie/DoublePriorityQueue$Entry");

define_field_id(dpq_entry, priority, "priority", "D");
define_field_id(dpq_entry, data, "data", "Ljava/nio/ByteBuffer;");

define_method_id(dpq_entry, ctor, "<init>", "(Ledu/duke/cs/tpie/DoublePriorityQueue;)V");


// java methods

JNIEXPORT jlong JNICALL Java_edu_duke_cs_tpie_DoublePriorityQueue_create(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jint num_bytes
) {
	try_jni_exceptions();

	IDoublePriorityQueue * q = IDoublePriorityQueue::make((uint32_t)num_bytes);
	return (jlong)q->to_handle();

	catch_jni_exceptions(env);
	return -1;
}

JNIEXPORT void JNICALL Java_edu_duke_cs_tpie_DoublePriorityQueue_cleanup(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle
) {
	try_jni_exceptions();

	delete IDoublePriorityQueue::from_handle((uint64_t)handle);

	catch_jni_exceptions(env);
}

JNIEXPORT void JNICALL Java_edu_duke_cs_tpie_DoublePriorityQueue_push(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle,
	jobject jentry
) {
	try_jni_exceptions();

	// get the jentry bytes
	jobject jbuf = get_field(env, dpq_entry, data, GetObjectField, jentry);
	jbyteArray jbufArray = (jbyteArray)call_method(env, byte_buffer, array, CallObjectMethod, jbuf);
	check_jni_exception(env);
	jbyte * jbufBytes = env->GetByteArrayElements(jbufArray, NULL);
	check_jni_exception(env);

	// push to the queue
	IDoublePriorityQueue & q = *IDoublePriorityQueue::from_handle(handle);
	q.push(
		get_field(env, dpq_entry, priority, GetDoubleField, jentry),
		(uint8_t *)jbufBytes
	);
	check_jni_exception(env);

	// jni cleanup
	env->ReleaseByteArrayElements(jbufArray, jbufBytes, JNI_ABORT);
	check_jni_exception(env);

	catch_jni_exceptions(env);
}

JNIEXPORT jobject JNICALL Java_edu_duke_cs_tpie_DoublePriorityQueue_top(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle,
	jobject jqueue
) {
	try_jni_exceptions();
	
	// get our queue
	IDoublePriorityQueue & q = *IDoublePriorityQueue::from_handle(handle);

	// make the jentry and get the bytes
	jobject jentry = new_class(env, dpq_entry, ctor, jqueue);
	jobject jbuf = get_field(env, dpq_entry, data, GetObjectField, jentry);
	check_jni_exception(env);
	jbyteArray jbufArray = (jbyteArray)call_method(env, byte_buffer, array, CallObjectMethod, jbuf);
	check_jni_exception(env);
	jbyte * jbufBytes = env->GetByteArrayElements(jbufArray, NULL);
	check_jni_exception(env);

	// convert entry
	set_field(env, dpq_entry, priority, SetDoubleField, jentry, (jdouble)q.top_priority());
	check_jni_exception(env);
	std::memcpy((uint8_t *)jbufBytes, q.top_bytes(), q.get_num_bytes());

	// jni cleanup
	env->ReleaseByteArrayElements(jbufArray, jbufBytes, 0);
	check_jni_exception(env);

	return jentry;

	catch_jni_exceptions(env);
	return NULL;
}

JNIEXPORT void JNICALL Java_edu_duke_cs_tpie_DoublePriorityQueue_pop(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle
) {
	try_jni_exceptions();

	IDoublePriorityQueue & q = *IDoublePriorityQueue::from_handle(handle);
	try {
		q.pop();
	} catch (tpie::end_of_stream_exception ex) {
		throw_exception("end of stream");
	}

	catch_jni_exceptions(env);
}


JNIEXPORT jlong JNICALL Java_edu_duke_cs_tpie_DoublePriorityQueue_size(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle
) {
	try_jni_exceptions();

	IDoublePriorityQueue & q = *IDoublePriorityQueue::from_handle(handle);
	return (jlong)q.size();

	catch_jni_exceptions(env);
	return 0;
}

JNIEXPORT jboolean JNICALL Java_edu_duke_cs_tpie_DoublePriorityQueue_empty(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle
) {
	try_jni_exceptions();

	IDoublePriorityQueue & q = *IDoublePriorityQueue::from_handle(handle);
	return (jboolean)q.empty();

	catch_jni_exceptions(env);
	return true;
}

