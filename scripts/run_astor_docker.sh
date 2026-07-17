#!/usr/bin/env bash
set -euo pipefail

# Uso: ./run_astor.sh <PROJECT_NAME> [JAVA_VERSION] [PROJECT_PATH] [RESULT_PATH] [SRC_JAVA] [SRC_TEST]
PROJECT_NAME="${1:?Uso: $0 <PROJECT_NAME> [JAVA_VERSION] [PROJECT_PATH] [RESULT_PATH] [SRC_JAVA] [SRC_TEST]}"
JAVA_VERSION="${2:-11}"
PROJECT_PATH="${3:-$(pwd)/samples/${PROJECT_NAME}}"
RESULT_PATH="${4:-$(pwd)/results/${PROJECT_NAME}}"
# Fallback para o layout padrão do Maven quando o projeto não segue outra convenção
SRC_JAVA="${5:-src/main/java/}"
SRC_TEST="${6:-src/test/java/}"
CONTAINER_NAME="astor-${PROJECT_NAME}"

if [[ ! -d "$PROJECT_PATH" ]]; then
  echo "Erro: projeto não encontrado em $PROJECT_PATH" >&2
  exit 1
fi

mkdir -p "$RESULT_PATH"

# Se o container já existe (rodando ou parado), remove — o entrypoint dispara
# a análise assim que o container inicia, então não dá pra reaproveitar com
# segurança (limpar por dentro via exec correria contra o entrypoint).
if docker container inspect "$CONTAINER_NAME" > /dev/null 2>&1; then
  echo "Container '$CONTAINER_NAME' já existe, removendo..."
  docker rm -f "$CONTAINER_NAME" > /dev/null
fi

echo "Criando container '$CONTAINER_NAME'..."
docker create --name "$CONTAINER_NAME" \
  -v "$RESULT_PATH:/astor/output_astor" \
  -e JAVA_VERSION="$JAVA_VERSION" \
  -e ENGINE_CLASS=fr.inria.astor.approaches.flakyseeding.FsEngine \
  -e SRC_JAVA="$SRC_JAVA" \
  -e SRC_TEST="$SRC_TEST" \
  astor > /dev/null 2>&1

echo "Copiando projeto '$PROJECT_NAME' para o container..."
docker cp "$PROJECT_PATH/." "$CONTAINER_NAME:/astor/target-project"

echo "Iniciando análise..."
docker start "$CONTAINER_NAME"