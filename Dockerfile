FROM maven:3.9.16-eclipse-temurin-11 AS astor_build

WORKDIR /astor
COPY . .
RUN --mount=type=cache,target=/root/.m2/repository mvn clean package -DskipTests -B

FROM eclipse-temurin:11 AS final_stage

RUN apt-get update && apt-get install -y --no-install-recommends wget gnupg software-properties-common git maven nano

RUN wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
RUN echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list
RUN apt-get update
RUN apt-get install -y --no-install-recommends temurin-8-jdk temurin-11-jdk
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# --- criação do usuário com UID/GID configuráveis ---
ARG USER_UID=1000
ARG USER_GID=1000

RUN groupadd -f -g "$USER_GID" astoruser 2>/dev/null || true \
  && if id -u "$USER_UID" > /dev/null 2>&1; then \
       echo "UID $USER_UID já existe, reaproveitando usuário existente"; \
     else \
       useradd -u "$USER_UID" -g "$USER_GID" -m -s /bin/bash astoruser; \
     fi

RUN mkdir -p /astor /astor/target-project/target

COPY --from=astor_build /astor/target/astor-*-jar-with-dependencies.jar ./astor/target/astor.jar
COPY --from=astor_build /astor/target/classes ./astor/target/classes
COPY --from=astor_build /astor/target/test-classes ./astor/target/test-classes
COPY scripts/docker/ /usr/local/bin/
COPY scripts/command.txt /astor/
RUN chmod a+x /usr/local/bin/* /astor/command.txt

RUN chown -R "$USER_UID:$USER_GID" /astor

USER $USER_UID:$USER_GID
CMD ["tail", "-f", "/dev/null"]