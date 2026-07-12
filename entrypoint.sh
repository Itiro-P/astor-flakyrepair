#!/bin/bash
set -e

JAVA_VERSION="${JAVA_VERSION:-8}"
TARGET_DIR="${TARGET_DIR:-/target-project}"
LOG_DIR="/astor/output_astor"
trap 'rm -rf "${TARGET_DIR:?}"/* 2>/dev/null || true' EXIT

export JAVA_HOME=/usr/lib/jvm/temurin-26-jdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

mkdir -p "$LOG_DIR"
echo ">> Usando JDK $JAVA_VERSION" | tee -a "$LOG_DIR/entrypoint.log"

cd "$TARGET_DIR"

echo ">> Compilando projeto-alvo..." | tee -a "$LOG_DIR/entrypoint.log"
mvn clean compile test-compile dependency:copy-dependencies \
  ${TARGET_POM:+-f "$TARGET_POM"} \
  -DskipTests=true -DoutputDirectory=lib -DincludeScope=test \
  2>&1 | tee "$LOG_DIR/maven-build.log"

echo ">> Rodando Astor..." | tee -a "$LOG_DIR/entrypoint.log"
java -cp /astor/astor.jar fr.inria.main.evolution.AstorMain \
  -mode custom \
  -customengine "${ENGINE_CLASS:-fr.inria.astor.approaches.flakydebug.FlakyDebugEngine}" \
  -javacompliancelevel "$JAVA_VERSION" \
  -srcjavafolder "${SRC_JAVA:-src/main/java/}" \
  -srctestfolder "${SRC_TEST:-src/test/java/}" \
  -binjavafolder target/classes/ \
  -bintestfolder target/test-classes/ \
  -location "$TARGET_DIR" \
  -dependencies "$TARGET_DIR/lib" \
  -out /astor/output_astor "$@" \
  2>&1 | tee "$LOG_DIR/astor-run.log"