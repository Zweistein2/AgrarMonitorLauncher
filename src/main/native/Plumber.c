#include "Plumber.h"

#define BUFSIZE 16384

LPCWSTR nameOfPipe = TEXT("\\\\.\\pipe\\plumber");  // global variable representing the named pipe
JavaVM* jvm = NULL;                                 // global variable representing jvm
JNIEnv *t_env;                                      // global variable representing a thread-safe JNIenv

void sendJavaMsg(JNIEnv *env, jobject obj, jmethodID func, const char* msg) {
    jstring javaMsg = (*env)->NewStringUTF(env, msg);
    (*env)->CallVoidMethod(env, obj, func, javaMsg);
    (*env)->DeleteLocalRef(env, javaMsg);
}

struct ThreadData {
    HANDLE pipeHandle;
    jobject obj;
};

JNIEXPORT jint JNICALL Java_de_zweistein2_Plumber_createNamedPipe(JNIEnv *env, jobject obj) {
    BOOL fConnected = FALSE;
    DWORD dwThreadId = 0;
    HANDLE pipeHandle = INVALID_HANDLE_VALUE, hThread;
    jclass clz = (*env)->FindClass(env, "de/zweistein2/Plumber");
    jmethodID printMessageID = (*env)->GetMethodID(env, (*env)->NewGlobalRef(env, clz), "printMessage", "(Ljava/lang/String;)V");
    (*env)->GetJavaVM(env, &jvm);

    for (;;) {
        printf("\nPipe Server: Main thread awaiting client connection on %ls\n", nameOfPipe);
        pipeHandle = CreateNamedPipe(
            nameOfPipe,                 // pipe name
            PIPE_ACCESS_DUPLEX,         // read/write access
            PIPE_TYPE_MESSAGE |         // message type pipe
            PIPE_READMODE_MESSAGE |     // message-read mode
            PIPE_WAIT,                  // blocking mode
            PIPE_UNLIMITED_INSTANCES,   // max. instances
            BUFSIZE,                    // output buffer size
            BUFSIZE,                    // input buffer size
            0,                          // client time-out
            NULL                        // default security attribute
        );

        if (pipeHandle == INVALID_HANDLE_VALUE) {
            printf("CreateNamedPipe failed, GLE=%lu.\n", GetLastError());
            return -1;
        }

        // Print verbose messages. In production code, this should be for debugging only.
        printf("InstanceThread created, receiving and processing messages.\n");

        // Wait for the client to connect; if it succeeds,
        // the function returns a nonzero value. If the function
        // returns zero, GetLastError returns ERROR_PIPE_CONNECTED.
        fConnected = ConnectNamedPipe(pipeHandle, NULL) ? TRUE : (GetLastError() == ERROR_PIPE_CONNECTED);

        if (fConnected) {
            printf("Client connected, creating a processing thread.\n");

            struct ThreadData threadData;
            threadData.pipeHandle = pipeHandle;
            threadData.obj = obj;

            // Create a thread for this client.
            hThread = CreateThread(
                NULL,                   // no security attribute
                0,                      // default stack size
                InstanceThread,         // thread proc
                &threadData,            // thread parameter
                0,                      // not suspended
                &dwThreadId             // returns thread ID
            );

            DWORD exitCode = 0;

            if (hThread == NULL) {
                printf("CreateThread failed, GLE=%lu.\n", GetLastError());
                return -1;
            } else {
                while(TRUE) {
                    GetExitCodeThread(hThread, &exitCode);
                    if(exitCode == STILL_ACTIVE){
                        Sleep(20);
                        continue;
                    }
                    printf("Thread exit code was %d.\n", exitCode);
                    if(exitCode == 1) {
                        CloseHandle(hThread);
                        CloseHandle(pipeHandle);

                        return 0;
                    }
                    break;
                }

                CloseHandle(hThread);
            }
        } else {
            // The client could not connect, so close the pipe.
            CloseHandle(pipeHandle);
        }
    }

    return 0;
}

// This routine is a thread processing function to read from and reply to a client
// via the open pipe connection passed from the main loop. Note this allows
// the main loop to continue executing, potentially creating more threads
// of this procedure to run concurrently, depending on the number of incoming
// client connections.
DWORD WINAPI InstanceThread(LPVOID lpvParam) {
    HANDLE hHeap = GetProcessHeap();
    TCHAR* pchRequest = (TCHAR*)HeapAlloc(hHeap, 0, BUFSIZE*sizeof(TCHAR));
    TCHAR* pchReply = (TCHAR*)HeapAlloc(hHeap, 0, BUFSIZE*sizeof(TCHAR));
    struct ThreadData* threadData;

    DWORD cbBytesRead = 0;
    BOOL fSuccess = FALSE;
    HANDLE hPipe;

    // Do some extra error checking since the app will keep running even if this
    // thread fails.
    if (lpvParam == NULL) {
        printf("\nERROR - Pipe Server Failure:\n");
        printf("   InstanceThread got an unexpected NULL value in lpvParam.\n");
        printf("   InstanceThread exiting.\n");
        if (pchReply != NULL) HeapFree(hHeap, 0, pchReply);
        if (pchRequest != NULL) HeapFree(hHeap, 0, pchRequest);
        (*jvm)->DetachCurrentThread(jvm);
        return (DWORD)-1;
    }

    if (pchRequest == NULL) {
        printf("\nERROR - Pipe Server Failure:\n");
        printf("   InstanceThread got an unexpected NULL heap allocation.\n");
        printf("   InstanceThread exiting.\n");
        if (pchReply != NULL) HeapFree(hHeap, 0, pchReply);
        (*jvm)->DetachCurrentThread(jvm);
        return (DWORD)-1;
    }

    if (pchReply == NULL) {
        printf("\nERROR - Pipe Server Failure:\n");
        printf("   InstanceThread got an unexpected NULL heap allocation.\n");
        printf("   InstanceThread exiting.\n");
        HeapFree(hHeap, 0, pchRequest);
        (*jvm)->DetachCurrentThread(jvm);
        return (DWORD)-1;
    }

    threadData = (struct ThreadData*) lpvParam;
    (*jvm)->AttachCurrentThread(jvm, (void**)&t_env, NULL);
    jclass clz = (*t_env)->FindClass(t_env, "de/zweistein2/Plumber");
    jmethodID printMessageID = (*t_env)->GetMethodID(t_env, (*t_env)->NewGlobalRef(t_env, clz), "printMessage", "(Ljava/lang/String;)V");

    // The thread's parameter is a handle to a pipe object instance.
    hPipe = threadData->pipeHandle;

    // Loop until done reading
    while(TRUE) {
        // Read client requests from the pipe. This simplistic code only allows messages
        // up to BUFSIZE characters in length.

        do {
            fSuccess = ReadFile(
                hPipe,                  // handle to pipe
                pchRequest,             // buffer to receive data
                BUFSIZE*sizeof(TCHAR),  // size of buffer
                &cbBytesRead,           // number of bytes read
                NULL);                  // not overlapped I/O

            if(!fSuccess && GetLastError() != ERROR_MORE_DATA) {
                break;
            }

            char *ret = strstr(pchRequest, "STOPSTOPSTOP");
            if(ret) {
                // Flush the pipe to allow the client to read the pipe's contents
                // before disconnecting. Then disconnect the pipe, and close the
                // handle to this pipe instance.
                FlushFileBuffers(hPipe);
                DisconnectNamedPipe(hPipe);
                CloseHandle(hPipe);

                HeapFree(hHeap, 0, pchRequest);
                HeapFree(hHeap, 0, pchReply);
                (*jvm)->DetachCurrentThread(jvm);

                printf("InstanceThread stopped.\n");
                return (DWORD)1;
            } else {
                TCHAR* content = (TCHAR*)HeapAlloc(hHeap, 0, cbBytesRead*sizeof(TCHAR));

                sprintf(content, "%.*s", cbBytesRead, pchRequest);
                sendJavaMsg(t_env, threadData->obj, printMessageID, content);

                HeapFree(hHeap, 0, content);
            }
        } while (!fSuccess);  // repeat loop if ERROR_MORE_DATA

        if (!fSuccess || cbBytesRead == 0) {
            if (GetLastError() == ERROR_BROKEN_PIPE) {
                printf("InstanceThread: client disconnected.\n");
            } else {
                printf("InstanceThread ReadFile failed, GLE=%lu.\n", GetLastError());
            }
            break;
        }
    }

    // Flush the pipe to allow the client to read the pipe's contents
    // before disconnecting. Then disconnect the pipe, and close the
    // handle to this pipe instance.
    FlushFileBuffers(hPipe);
    DisconnectNamedPipe(hPipe);
    CloseHandle(hPipe);

    HeapFree(hHeap, 0, pchRequest);
    HeapFree(hHeap, 0, pchReply);
    (*jvm)->DetachCurrentThread(jvm);

    printf("InstanceThread exiting.\n");
    return (DWORD)0;
}
