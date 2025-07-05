#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support
cygwin=false
msys=false
darwin=false
case "$(uname)" in
  CYGWIN* )
    cygwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  Darwin* )
    darwin=true
    ;;
esac

# Attempt to set JAVA_HOME if not already set
if [ -z "$JAVA_HOME" ] ; then
  if [ -x "/usr/libexec/java_home" ] ; then
    JAVA_HOME=$(/usr/libexec/java_home)
  fi
fi

if [ -z "$JAVA_HOME" ] ; then
  die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

JAVA_BIN="$JAVA_HOME/bin/java"

CLASSPATH=$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar

exec "$JAVA_BIN" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
