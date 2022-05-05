#include <jni.h>
#include <stdio.h>
#include <windows.h>
#include <tchar.h>
#include <strsafe.h>

#ifndef _Included_Plumber
#define _Included_Plumber
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_de_zweistein2_Plumber_createNamedPipe(JNIEnv *, jobject);

DWORD WINAPI InstanceThread(LPVOID lpvParam);

#ifdef __cplusplus
}
#endif
#endif