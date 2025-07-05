@echo off
@rem ------------------------------------------------------------------------------
@rem Gradle startup script for Windows
@rem ------------------------------------------------------------------------------
@rem
@rem Uses JAVA_HOME if set, otherwise tries to find java on the path.
@rem

set DIR=%~dp0
set DEFAULT_JVM_OPTS=

set CLASSPATH=%DIR%\gradle\wrapper\gradle-wrapper.jar

@if not defined JAVA_HOME (
    set JAVA_EXE=java.exe
) else (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

@if exist "%JAVA_EXE%" (
    rem Found Java
) else (
    echo.
    echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
    echo.
    echo Please set the JAVA_HOME variable in your environment to match the
    echo location of your Java installation.
    exit /b 1
)

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

exit /b %ERRORLEVEL%
