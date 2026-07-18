#!/bin/bash
set -euo pipefail

JAVA_VERSION="${JAVA_VERSION:-8}"
TARGET_DIR="${TARGET_DIR:-/astor/target-project}"
LOG_DIR="/astor/output_astor"
trap 'rm -rf "${TARGET_DIR:?}"/* 2>/dev/null || true' EXIT

export JAVA_HOME=/usr/lib/jvm/temurin-26-jdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

mkdir -p "$LOG_DIR"
echo ">> Usando JDK $JAVA_VERSION" | tee -a "$LOG_DIR/entrypoint.log"

echo ">> Compilando projeto-alvo..." | tee -a "$LOG_DIR/entrypoint.log"
(
  cd "$TARGET_DIR"
  mvn clean compile test-compile dependency:copy-dependencies \
    ${TARGET_POM:+-f "$TARGET_POM"} \
    -DskipTests -DoutputDirectory=lib -DincludeScope=test
) 2>&1 | tee "$LOG_DIR/maven-build.log"

mkdir -p /astor/diffSolutions

echo ">> Rodando Astor..." | tee -a "$LOG_DIR/entrypoint.log"
java -cp /astor/target/astor.jar fr.inria.main.evolution.AstorMain \
-mode custom \
-customengine "${ENGINE_CLASS:-fr.inria.astor.approaches.flakyseeding.FsEngine}" \
-javacompliancelevel "$JAVA_VERSION" \
-srcjavafolder "${SRC_JAVA:-src/main/java/}" \
-srctestfolder "${SRC_TEST:-src/test/java/}" \
-binjavafolder target/classes/ \
-bintestfolder target/test-classes/ \
-location "$TARGET_DIR" \
-dependencies "$TARGET_DIR/lib" \
-out /astor/output_astor \
--stopfirst false \
--saveall true "$@" \
2>&1 | tee "$LOG_DIR/astor-run.log"

mv /astor/diffSolutions /astor/output_astor