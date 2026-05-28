@echo off
REM ============================================================================
REM LucidOS Gradle Build Script for Windows
REM ============================================================================
REM
REM This script optimizes Gradle builds for the LucidOS multi-module project
REM on Windows by setting optimal JVM parameters and build options.
REM
REM Usage:
REM   build.bat                    - Full clean build
REM   build.bat --profile          - Build with performance profiling
REM   build.bat --debug            - Debug build (faster)
REM   build.bat --release          - Release build (optimized)
REM   build.bat --clean-cache      - Clear all caches and rebuild
REM   build.bat --help             - Show this help message
REM
REM ============================================================================

setlocal enabledelayedexpansion

REM Color codes for output
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "RESET=[0m"

REM Check if gradlew exists
if not exist "gradlew.bat" (
    echo %RED%Error: gradlew.bat not found in current directory%RESET%
    echo Please run this script from the LucidOS root directory
    exit /b 1
)

REM Parse command line arguments
set "BUILD_PROFILE=false"
set "BUILD_DEBUG=false"
set "BUILD_RELEASE=false"
set "CLEAN_CACHE=false"
set "SHOW_HELP=false"

:parse_args
if "%1"=="" goto args_done
if "%1"=="--profile" set "BUILD_PROFILE=true" && goto next_arg
if "%1"=="--debug" set "BUILD_DEBUG=true" && goto next_arg
if "%1"=="--release" set "BUILD_RELEASE=true" && goto next_arg
if "%1"=="--clean-cache" set "CLEAN_CACHE=true" && goto next_arg
if "%1"=="--help" set "SHOW_HELP=true" && goto next_arg
:next_arg
shift
goto parse_args

:args_done
if "%SHOW_HELP%"=="true" goto show_help

echo.
echo %GREEN%╔════════════════════════════════════════════════════════════════════════╗%RESET%
echo %GREEN%║                  LucidOS Gradle Build Script                            ║%RESET%
echo %GREEN%╚════════════════════════════════════════════════════════════════════════╝%RESET%
echo.

REM Get system core count
for /f "tokens=2 delims==" %%A in ('wmic cpu get NumberOfCores /value') do set CPU_CORES=%%A
if "%CPU_CORES%"=="" set CPU_CORES=8
set /a GRADLE_WORKERS=%CPU_CORES%-1
if %GRADLE_WORKERS% lss 1 set GRADLE_WORKERS=1

echo [INFO] System Configuration:
echo [INFO]   CPU Cores Detected: %CPU_CORES%
echo [INFO]   Gradle Workers: %GRADLE_WORKERS%
echo.

REM Set optimal JVM parameters for Windows
set "JAVA_OPTS=-Xmx5g"
set "JAVA_OPTS=!JAVA_OPTS! -XX:MaxMetaspaceSize=768m"
set "JAVA_OPTS=!JAVA_OPTS! -XX:CompressedClassSpaceSize=256m"
set "JAVA_OPTS=!JAVA_OPTS! -XX:MaxDirectMemorySize=512m"
set "JAVA_OPTS=!JAVA_OPTS! -XX:+UseG1GC"
set "JAVA_OPTS=!JAVA_OPTS! -XX:MaxGCPauseMillis=200"
set "JAVA_OPTS=!JAVA_OPTS! -XX:+ParallelRefProcEnabled"
set "JAVA_OPTS=!JAVA_OPTS! -XX:+HeapDumpOnOutOfMemoryError"

echo [INFO] JVM Configuration:
echo [INFO]   Heap Size: 5GB
echo [INFO]   Metaspace: 768MB
echo [INFO]   GC Algorithm: G1GC
echo.

REM Handle cache cleanup
if "%CLEAN_CACHE%"=="true" (
    echo %YELLOW%[WARN] Cleaning Gradle caches...%RESET%
    if exist ".gradle" (
        echo [INFO] Removing .gradle directory...
        rmdir /s /q .gradle 2>nul
    )
    if exist "build" (
        echo [INFO] Removing build directories...
        for /d /r . %%d in (build) do @if exist "%%d" rmdir /s /q "%%d" 2>nul
    )
    echo %GREEN%[OK] Cache cleanup complete%RESET%
    echo.
)

REM Prepare build command
set "BUILD_CMD=call gradlew.bat clean build"
set "BUILD_OPTS=--parallel --workers=%GRADLE_WORKERS% --daemon"

if "%BUILD_DEBUG%"=="true" (
    echo %YELLOW%[INFO] Building DEBUG variant (faster)%RESET%
    set "BUILD_CMD=call gradlew.bat assembleDebug"
    echo.
) else if "%BUILD_RELEASE%"=="true" (
    echo %YELLOW%[INFO] Building RELEASE variant (optimized)%RESET%
    set "BUILD_CMD=call gradlew.bat assembleRelease"
    echo.
) else if "%BUILD_PROFILE%"=="true" (
    echo %YELLOW%[INFO] Building with performance profiling%RESET%
    set "BUILD_OPTS=!BUILD_OPTS! --profile"
    echo [INFO] Profile report will be generated in build/reports/profile/
    echo.
)

REM Display build configuration
echo [INFO] Build Configuration:
echo [INFO]   Command: %BUILD_CMD%
echo [INFO]   Options: %BUILD_OPTS%
echo [INFO]   Environment: JAVA_OPTS=%JAVA_OPTS%
echo.

echo %GREEN%[INFO] Starting build...%RESET%
echo [INFO] This may take several minutes on first build
echo.

REM Execute build
setlocal
set JAVA_OPTS=%JAVA_OPTS%
set GRADLE_OPTS=!JAVA_OPTS!
%BUILD_CMD% %BUILD_OPTS%
set BUILD_EXIT_CODE=%ERRORLEVEL%
endlocal

REM Handle build result
echo.
if %BUILD_EXIT_CODE% equ 0 (
    echo %GREEN%╔════════════════════════════════════════════════════════════════════════╗%RESET%
    echo %GREEN%║                     BUILD SUCCESSFUL ✓                                 ║%RESET%
    echo %GREEN%╚════════════════════════════════════════════════════════════════════════╝%RESET%
    echo.
    echo [INFO] Build artifacts available at:
    echo [INFO]   Debug: packages/apps/*/build/outputs/apk/debug/
    echo [INFO]   Release: packages/apps/*/build/outputs/apk/release/
    if "%BUILD_PROFILE%"=="true" (
        echo [INFO]   Profile: build/reports/profile/
    )
    echo.
    exit /b 0
) else (
    echo %RED%╔════════════════════════════════════════════════════════════════════════╗%RESET%
    echo %RED%║                     BUILD FAILED ✗                                     ║%RESET%
    echo %RED%╚════════════════════════════════════════════════════════════════════════╝%RESET%
    echo.
    echo [ERROR] Build failed with exit code: %BUILD_EXIT_CODE%
    echo [ERROR] Check output above for error details
    echo [ERROR] Try: build.bat --clean-cache
    echo.
    exit /b %BUILD_EXIT_CODE%
)

:show_help
echo.
echo %GREEN%LucidOS Gradle Build Script%RESET%
echo.
echo Usage: build.bat [OPTIONS]
echo.
echo Options:
echo   --profile       Build with Gradle profiling enabled
echo   --debug         Assemble debug APKs (faster than full build)
echo   --release       Assemble release APKs (optimized)
echo   --clean-cache   Clear .gradle and build/ directories before building
echo   --help          Show this help message
echo.
echo Examples:
echo   build.bat                    Full clean build with optimization
echo   build.bat --debug            Quick debug build
echo   build.bat --profile          Build with profiling analysis
echo   build.bat --clean-cache      Full clean build from scratch
echo.
echo Environment Variables:
echo   CPU_CORES     Number of CPU cores (auto-detected)
echo   GRADLE_OPTS   Custom Gradle options
echo   JAVA_OPTS     Custom JVM options
echo.
exit /b 0
