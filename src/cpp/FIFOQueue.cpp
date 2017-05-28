
#include <cstring>
#include <tpie/tpie.h>
#include <tpie/queue.h>
#include "FIFOQueue.hpp"
#include "jni.hpp"


template <int NumBytes>
struct FIFOQEntry {
	uint8_t bytes[NumBytes];
};


class IFIFOQueue {
public:

	static IFIFOQueue * make(const uint32_t & num_bytes);

	virtual ~IFIFOQueue() {}

	virtual uint64_t to_handle() const {
		return (uint64_t)this;
	}

	static IFIFOQueue * from_handle(const uint64_t handle) {
		return (IFIFOQueue *)handle;
	}

	virtual uint32_t get_num_bytes() = 0;

	virtual void push(const uint8_t * bytes) = 0;
	virtual const uint8_t * front() = 0;
	virtual void pop() = 0;
	virtual uint64_t size() = 0;
	virtual bool empty() = 0;
};

template <int NumBytes>
class FIFOQueue : public IFIFOQueue {
public:

	FIFOQueue() : m_num_bytes(NumBytes) {}

	uint32_t get_num_bytes() {
		return m_num_bytes;
	}

	void push(const uint8_t * bytes) {
		FIFOQEntry<NumBytes> entry;
		std::memcpy(entry.bytes, bytes, m_num_bytes);
		m_queue.push(entry);
	}

	const uint8_t * front() {
		return m_queue.front().bytes;
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
	tpie::queue<FIFOQEntry<NumBytes>> m_queue;
};


IFIFOQueue * IFIFOQueue::make(const uint32_t & num_bytes) {
	switch (num_bytes) {
		case     8: return new FIFOQueue<    8>();
		case    16: return new FIFOQueue<   16>();
		case    32: return new FIFOQueue<   32>();
		case    64: return new FIFOQueue<   64>();
		case   128: return new FIFOQueue<  128>();
		case   256: return new FIFOQueue<  256>();
		case   512: return new FIFOQueue<  512>();
		case  1024: return new FIFOQueue< 1024>();
	}
	throw_exception("unsupported entry size for queue: %d", num_bytes);
	return NULL;
}


// java ids

define_class(fifoq_entry, "edu/duke/cs/tpie/FIFOQueue$Entry");

define_field_id(fifoq_entry, data, "data", "Ljava/nio/ByteBuffer;");

define_method_id(fifoq_entry, ctor, "<init>", "(Ledu/duke/cs/tpie/FIFOQueue;)V");


// java methods

JNIEXPORT jlong JNICALL Java_edu_duke_cs_tpie_FIFOQueue_create(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jint num_bytes
) {
	try_jni_exceptions();

	IFIFOQueue * q = IFIFOQueue::make((uint32_t)num_bytes);
	return (jlong)q->to_handle();

	catch_jni_exceptions(env);
	return -1;
}

JNIEXPORT void JNICALL Java_edu_duke_cs_tpie_FIFOQueue_cleanup(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle
) {
	try_jni_exceptions();

	delete IFIFOQueue::from_handle((uint64_t)handle);

	catch_jni_exceptions(env);
}

JNIEXPORT void JNICALL Java_edu_duke_cs_tpie_FIFOQueue_push(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle,
	jobject jentry
) {
	try_jni_exceptions();

	// get the jentry bytes
	jobject jbuf = get_field(env, fifoq_entry, data, GetObjectField, jentry);
	jbyteArray jbufArray = (jbyteArray)call_method(env, byte_buffer, array, CallObjectMethod, jbuf);
	jbyte * jbufBytes = env->GetByteArrayElements(jbufArray, NULL);

	// push to the queue
	IFIFOQueue & q = *IFIFOQueue::from_handle(handle);
	q.push((uint8_t *)jbufBytes);

	// jni cleanup
	env->ReleaseByteArrayElements(jbufArray, jbufBytes, JNI_ABORT);

	catch_jni_exceptions(env);
}

JNIEXPORT jobject JNICALL Java_edu_duke_cs_tpie_FIFOQueue_front(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle,
	jobject jqueue
) {
	try_jni_exceptions();

	// get our queue
	IFIFOQueue & q = *IFIFOQueue::from_handle(handle);

	// make the jentry and get the bytes
	jobject jentry = new_class(env, fifoq_entry, ctor, jqueue);
	jobject jbuf = get_field(env, fifoq_entry, data, GetObjectField, jentry);
	jbyteArray jbufArray = (jbyteArray)call_method(env, byte_buffer, array, CallObjectMethod, jbuf);
	jbyte * jbufBytes = env->GetByteArrayElements(jbufArray, NULL);

	// convert entry
	std::memcpy((uint8_t *)jbufBytes, q.front(), q.get_num_bytes());

	// jni cleanup
	env->ReleaseByteArrayElements(jbufArray, jbufBytes, JNI_COMMIT);

	return jentry;

	catch_jni_exceptions(env);
	return NULL;
}

JNIEXPORT void JNICALL Java_edu_duke_cs_tpie_FIFOQueue_pop(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle
) {
	try_jni_exceptions();

	IFIFOQueue & q = *IFIFOQueue::from_handle(handle);
	q.pop();

	catch_jni_exceptions(env);
}

JNIEXPORT jlong JNICALL Java_edu_duke_cs_tpie_FIFOQueue_size(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle
) {
	try_jni_exceptions();

	IFIFOQueue & q = *IFIFOQueue::from_handle(handle);
	return (jlong)q.size();

	catch_jni_exceptions(env);
	return 0;
}

JNIEXPORT jboolean JNICALL Java_edu_duke_cs_tpie_FIFOQueue_empty(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jlong handle
) {
	try_jni_exceptions();

	IFIFOQueue & q = *IFIFOQueue::from_handle(handle);
	return (jboolean)q.empty();

	catch_jni_exceptions(env);
	return true;
}

