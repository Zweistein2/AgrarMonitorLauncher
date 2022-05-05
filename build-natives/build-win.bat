@if [%1]==[] goto usage

premake5 --file=build-win.lua %1
msbuild plumber.sln /p:Configuration=ReleaseDLL /p:Platform=Win32 /t:Rebuild
copy bin\x32\ReleaseDLL\Plumber.dll ..\Plumber.dll
msbuild plumber.sln /p:Configuration=ReleaseDLL /p:Platform=x64 /t:Rebuild
copy bin\x64\ReleaseDLL\Plumber.dll ..\Plumber64.dll

@goto :eof

:usage
@echo call with [action] profile, e.g. "build-win vs2019"
@exit /B 1
