solution "plumber"
	configurations { "ReleaseDLL" }
	platforms { "x32", "x64" }

    -- Premake 5.0.0 alpha 11 : SDK version needs to be specified for VS2019
	systemversion("10.0.18362.0")

	includedirs {
		"../src/main/resources/native/include/jni",
		"../src/main/resources/native/include/jni/win32",
	}

	defines {
		"NDEBUG",
		"WINDOWS"
	}

	flags {
		"Optimize",
		"StaticRuntime"
	}

	buildoptions {
		"/wd4800",
		"/wd4996"
	}

	project "Plumber"

		kind "SharedLib"
		language "C"

		files {
			"../src/main/native/**.c",
		}

		includedirs {
			"../src/main/native",
		}