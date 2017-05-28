
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

jclass find_class_or_throw(JNIEnv * env, const char * name) {
	jclass c = env->FindClass(name);
	if (c == NULL) {
		throw_exception("can't find class: %s", name);
	}
	c = promoteRef(env, c);
	return c;
}

jfieldID find_field_or_throw(JNIEnv * env, const jclass c, const char * name, const char * sig) {
	jfieldID id = env->GetFieldID(c, name, sig);
	if (id == 0) {
		throw_exception("can't find field: %s %s", name, sig);
	}
	return id;
}

jmethodID find_method_or_throw(JNIEnv * env, const jclass c, const char * name, const char * sig) {
	jmethodID id = env->GetMethodID(c, name, sig);
	if (id == 0) {
		throw_exception("can't find method: %s %s", name, sig);
	}
	return id;
}


// shared java ids

define_class(byte_buffer, "java/nio/ByteBuffer");
define_method_id(byte_buffer, array, "array", "()[B");

