#!/bin/bash
set -e

# Remove any module-info.java files if they exist
echo "=== Checking for module-info.java files ==="
MODULE_INFO_FILES=$(find . -name "module-info.java" -type f 2>/dev/null)
if [ ! -z "$MODULE_INFO_FILES" ]; then
  echo "⚠ Found module-info.java files - removing for classpath compilation:"
  echo "$MODULE_INFO_FILES"
  find . -name "module-info.java" -type f -delete
  echo "✓ Removed module-info.java files"
else
  echo "✓ No module-info.java files found"
fi

# Repository structure
echo "=== Repository Structure ==="
ls -la
find . -maxdepth 2 -type d -name "src" 2>/dev/null

# Setup H2 Database
echo "=== Setting up H2 Database ==="
H2_FOUND=$(find . -name "h2*.jar" -type f | head -1)
if [ ! -z "$H2_FOUND" ]; then
  cp "$H2_FOUND" build/lib/
  echo "✓ H2 JAR copied to build/lib"
  export H2_JAR="build/lib/$(basename $H2_FOUND)"
else
  echo "⚠ No H2 JAR found, continuing without database support"
  export H2_JAR=""
fi

# Set up classpaths
if [ ! -z "$H2_JAR" ]; then
  export COMPILE_CLASSPATH="$H2_JAR:$JUNIT_JAR:$PATH_TO_FX/*:build/classes"
  export TEST_CLASSPATH="$H2_JAR:$JUNIT_JAR:$PATH_TO_FX/*:build/classes:build/test-classes"
else
  export COMPILE_CLASSPATH="$JUNIT_JAR:$PATH_TO_FX/*:build/classes"
  export TEST_CLASSPATH="$JUNIT_JAR:$PATH_TO_FX/*:build/classes:build/test-classes"
fi
echo "✓ Classpaths configured"

# Save classpaths for other scripts
echo "export H2_JAR='$H2_JAR'" > .travis/env.sh
echo "export COMPILE_CLASSPATH='$COMPILE_CLASSPATH'" >> .travis/env.sh
echo "export TEST_CLASSPATH='$TEST_CLASSPATH'" >> .travis/env.sh

# Compile main classes
echo "=== Compiling FoundationCode (Main Classes) ==="
if [ -d "FoundationCode/src" ]; then
  echo "Found FoundationCode/src directory"
  
  MAIN_FILES=$(find FoundationCode/src -name "*.java" -not -name "*Test.java" -type f)
  MAIN_COUNT=$(echo "$MAIN_FILES" | grep -c ".java" || echo "0")
  
  echo "Found $MAIN_COUNT main source files"
  
  if [ $MAIN_COUNT -gt 0 ]; then
    echo "$MAIN_FILES" | xargs javac -d build/classes \
      -cp "$COMPILE_CLASSPATH" \
      --module-path "$PATH_TO_FX" \
      --add-modules javafx.controls,javafx.fxml \
      2>&1 | tee foundation.log
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
      echo "✓ Main classes compiled successfully"
      COMPILED_MAIN=$(find build/classes -name "*.class" -type f | wc -l)
      echo "  Compiled $COMPILED_MAIN class files"
    else
      echo "✗ Main class compilation failed:"
      cat foundation.log
      exit 1
    fi
  else
    echo "⚠ No main Java files found"
  fi
else
  echo "✗ FoundationCode/src directory not found"
  exit 1
fi

# Compile test classes
echo "=== Compiling FoundationCode Tests ==="
if [ -d "FoundationCode/src" ]; then
  TEST_FILES=$(find FoundationCode/src -name "*Test.java" -type f)
  TEST_COUNT=$(echo "$TEST_FILES" | grep -c ".java" || echo "0")
  
  echo "Found $TEST_COUNT test source files"
  
  if [ $TEST_COUNT -gt 0 ]; then
    echo "$TEST_FILES" | xargs javac -d build/test-classes \
      -cp "$COMPILE_CLASSPATH" \
      --module-path "$PATH_TO_FX" \
      --add-modules javafx.controls,javafx.fxml \
      2>&1 | tee foundation-tests.log
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
      echo "✓ Test classes compiled successfully"
      COMPILED_TESTS=$(find build/test-classes -name "*.class" -type f | wc -l)
      echo "  Compiled $COMPILED_TESTS test class files"
      find build/test-classes -name "*Test.class" -type f
    else
      echo "✗ Test compilation failed:"
      cat foundation-tests.log
      exit 1
    fi
  else
    echo "⚠ No test files found"
  fi
fi

# Summary
echo "=== Compilation Summary ==="
TOTAL_MAIN=$(find build/classes -name "*.class" -type f 2>/dev/null | wc -l)
TOTAL_TESTS=$(find build/test-classes -name "*.class" -type f 2>/dev/null | wc -l)
TOTAL_JAVA=$(find FoundationCode/src -name "*.java" -type f 2>/dev/null | wc -l)

echo "📊 Compilation Statistics:"
echo "  - Total Java sources: $TOTAL_JAVA"
echo "  - Compiled main classes: $TOTAL_MAIN"
echo "  - Compiled test classes: $TOTAL_TESTS"

if [ $TOTAL_MAIN -eq 0 ]; then
  echo "✗ No main classes compiled!"
  exit 1
fi

echo "✓ Compilation successful"
