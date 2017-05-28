
#ifndef __JNI_HPP__
#define __JNI_HPP__


extern void throw_exception(const char * msg, ...);

#define try_jni_exceptions() \
	try {

# define catch_jni_exceptions(env) \
	} catch (char * msg) { \
		env->ThrowNew(env->FindClass("java/lang/Exception"), msg); \
		delete[] msg; \
	}


template <typename T>
T promoteRef(JNIEnv * env, const T & ref) {
	// "promote" the ref to a global ref,
	// so the JVM doesn't delete it before the next call
	// technically, we should free these with DeleteGLobalRef,
	// but I just don't care... we'll leak them. what's the worst that can happen? =P
	return (T)env->NewGlobalRef((jobject)ref);
}

extern jclass find_class_or_throw(JNIEnv * env, const char * name);

extern jfieldID find_field_or_throw(JNIEnv * env, const jclass c, const char * name, const char * sig);

extern jmethodID find_method_or_throw(JNIEnv * env, const jclass c, const char * name, const char * sig);


#define define_class(class_name, name) \
	jclass class_##class_name = NULL; \
	jclass get_class_##class_name(JNIEnv * env) { \
		if (class_##class_name == NULL) { \
			class_##class_name = find_class_or_throw(env, name); \
		} \
		return class_##class_name; \
	}

#define get_class(env, class_name) \
	get_class_##class_name(env)


#define define_field_id(class_name, field_name, name, sig) \
	jfieldID field_##class_name##_##field_name = NULL; \
	jfieldID get_field_##class_name##_##field_name(JNIEnv * env) { \
		if (field_##class_name##_##field_name == NULL) { \
			field_##class_name##_##field_name = find_field_or_throw(env, get_class_##class_name(env), name, sig); \
		} \
		return field_##class_name##_##field_name; \
	}

#define get_field_id(env, class_name, field_name) \
	get_field_##class_name##_##field_name(env)

#define get_field(env, class_name, field_name, getter, obj) \
	env->getter(obj, get_field_id(env, class_name, field_name))

#define set_field(env, class_name, field_name, setter, obj, val) \
	env->setter(obj, get_field_id(env, class_name, field_name), val)


#define define_method_id(class_name, method_name, name, sig) \
	jmethodID method_##class_name##_##method_name = NULL; \
	jmethodID get_method_##class_name##_##method_name(JNIEnv * env) { \
		if (method_##class_name##_##method_name == NULL) { \
			method_##class_name##_##method_name = find_method_or_throw(env, get_class_##class_name(env), name, sig); \
		} \
		return method_##class_name##_##method_name; \
	}

#define get_method_id(env, class_name, method_name) \
	get_method_##class_name##_##method_name(env)

#define new_class(env, class_name, method_name, ...) \
	env->NewObject(get_class(env, class_name), get_method_id(env, class_name, method_name), ##__VA_ARGS__)

#define call_method(env, class_name, method_name, caller, obj, ...) \
	env->caller(obj, get_method_id(env, class_name, method_name), ##__VA_ARGS__)

#endif

