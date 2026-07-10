#!/bin/bash
set -e

JAVA_VERSION="${JAVA_VERSION:-8}"

case "$JAVA_VERSION" in
  8)  export JAVA_HOME=/usr/lib/jvm/temurin-8-jdk-amd64 ;;
  11) export JAVA_HOME=/usr/lib/jvm/temurin-11-jdk-amd64 ;;
  17) export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 ;;
  *) echo "JAVA_VERSION inválido: use 8, 11 ou 17"; exit 1 ;;
esac
export PATH="$JAVA_HOME/bin:$PATH"

echo ">> Usando JDK $JAVA_VERSION"

cd "$TARGET_DIR"

echo ">> Compilando projeto-alvo..."
mvn clean compile test-compile dependency:copy-dependencies \
  ${TARGET_POM:+-f "$TARGET_POM"} \
  -DskipTests=true -DoutputDirectory=lib -DincludeScope=test

echo ">> Rodando Astor..."
exec java -cp /astor/astor.jar fr.inria.main.evolution.AstorMain \
  -mode custom \
  -customengine "${ENGINE_CLASS:-fr.inria.astor.approaches.flakydebug.FlakyDebugEngine}" \
  -javacompliancelevel "$JAVA_VERSION" \
  -srcjavafolder "${SRC_JAVA:-src/main/java/}" \
  -srctestfolder "${SRC_TEST:-src/test/java/}" \
  -binjavafolder target/classes/ \
  -bintestfolder target/test-classes/ \
  -location "$TARGET_DIR" \
  -dependencies "$TARGET_DIR/lib" \
  "$@"