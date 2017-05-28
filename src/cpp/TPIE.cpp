
#include <tpie/tpie.h>
#include <tpie/memory.h>
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

