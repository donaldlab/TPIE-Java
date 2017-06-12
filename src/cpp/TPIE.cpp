
#include <tpie/tpie.h>
#include <tpie/memory.h>
#include <tpie/tempname.h>
#include <tpie/stats.h>
#include "TPIE.hpp"
#include "jni.hpp"


JNIEXPORT void JNICALL Java_edu_duke_cs_tpie_TPIE_init(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jint internalMiB
) {
	try_jni_exceptions();

	tpie::tpie_init();
	tpie::get_memory_manager().set_limit(internalMiB*1024*1024);

	catch_jni_exceptions(env);
}

JNIEXPORT void JNICALL Java_edu_duke_cs_tpie_TPIE_cleanup(
	JNIEnv * env,
	jclass c __attribute__((unused))
) {
	try_jni_exceptions();

	tpie::tpie_finish();

	catch_jni_exceptions(env);
}

JNIEXPORT void JNICALL Java_edu_duke_cs_tpie_TPIE_setTempDir(
	JNIEnv * env,
	jclass c __attribute__((unused)),
	jstring dir,
	jstring subdir
) {
	try_jni_exceptions();
	
	// get the dir string from java
	const char * dirbuf = env->GetStringUTFChars(dir, NULL);
	check_jni_exception(env);

	if (subdir == NULL) {

		tpie::tempname::set_default_path(dirbuf);

	} else {

		// get the subdir string too
		const char * subdirbuf = env->GetStringUTFChars(subdir, NULL);
		check_jni_exception(env);

		tpie::tempname::set_default_path(dirbuf, subdirbuf);

		// cleanup JNI
		env->ReleaseStringUTFChars(subdir, subdirbuf);
	}

	// cleanup JNI
	env->ReleaseStringUTFChars(dir, dirbuf);

	catch_jni_exceptions(env);
}

JNIEXPORT jlong JNICALL Java_edu_duke_cs_tpie_TPIE_getExternalBytes(
	JNIEnv * env,
	jclass c __attribute__((unused))
) {
	try_jni_exceptions();

	return (jlong)tpie::get_temp_file_usage();

	catch_jni_exceptions(env);
	return -1;
}

