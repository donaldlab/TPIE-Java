
#include <jni.h>
#include "jni.hpp"


void throw_exception(const char * msg, ...) {

	// printf the message old-school style
	char * buf = new char[4096];
	va_list args;
	va_start(args, msg);
	vsprintf(buf, msg, args);
	va_end(args);

	throw buf;
}


void check_jni_exception(JNIEnv *env) {
	if (env->ExceptionCheck() == JNI_TRUE) {

		// a Java exception happened inside JNI
		// it's waiting for us to get back to Java code so it can finish throwing
		// throw a special type to jump immediately to the catch_jni_exceptions block
		throw new JNIExceptionSignal();
	}
}

jclass find_class_or_throw(JNIEnv * env, const char * name) {
	jclass c = env->FindClass(name);
	check_jni_exception(env);
	if (c == NULL) {
		throw_exception("can't find class: %s", name);
	}

	// "promote" this class to a global reference so the class is still available
	// NOTE: this does leak the ref since we don't clean it up, but we'll only
	// even need a few class refs, so it's not a big deal.
	jclass ref = (jclass)env->NewGlobalRef(c);
	check_jni_exception(env);
	return ref;
}

jfieldID find_field_or_throw(JNIEnv * env, const jclass c, const char * name, const char * sig) {
	jfieldID id = env->GetFieldID(c, name, sig);
	check_jni_exception(env);
	if (id == 0) {
		throw_exception("can't find field: %s %s", name, sig);
	}
	return id;
}

jmethodID find_method_or_throw(JNIEnv * env, const jclass c, const char * name, const char * sig) {
	jmethodID id = env->GetMethodID(c, name, sig);
	check_jni_exception(env);
	if (id == 0) {
		throw_exception("can't find method: %s %s", name, sig);
	}
	return id;
}


// shared java ids

define_class(byte_buffer, "java/nio/ByteBuffer");
define_method_id(byte_buffer, array, "array", "()[B");

