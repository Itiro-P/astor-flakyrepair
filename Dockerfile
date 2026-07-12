FROM maven:3.9.16-eclipse-temurin-26 AS astor_build

WORKDIR /astor
COPY . .
RUN mvn clean package -DskipTests=true

FROM eclipse-temurin:26 AS final_stage

RUN apt-get update && apt-get install -y --no-install-recommends wget gnupg software-properties-common git maven

RUN wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
RUN echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list
RUN apt-get update
RUN apt-get install -y --no-install-recommends temurin-8-jdk temurin-11-jdk temurin-17-jdk temurin-25-jdk temurin-26-jdk
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# --- criação do usuário com UID/GID configuráveis ---
ARG USER_UID=1000
ARG USER_GID=1000

RUN groupadd -g "$USER_GID" astoruser \
  && useradd -u "$USER_UID" -g "$USER_GID" -m -s /bin/bash astoruser

WORKDIR /astor
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod a+x /usr/local/bin/entrypoint.sh

COPY --from=astor_build /astor/target/astor-*-jar-with-dependencies.jar ./astor.jar
COPY --from=astor_build /astor/target/classes ./target/classes
COPY --from=astor_build /astor/target/test-classes /astor/target/test-classes

# garante que as pastas usadas pelo entrypoint (destino do docker cp e do volume de saída)
# já nasçam com o dono certo, antes de trocar pro usuário não-root
RUN mkdir -p /target-project /astor/output_astor \
  && chown -R "$USER_UID:$USER_GID" /astor /target-project

USER astoruser
ENTRYPOINT ["entrypoint.sh"]