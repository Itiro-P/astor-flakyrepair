FROM maven:3.9.16-eclipse-temurin-11 AS astor_build

WORKDIR /astor
COPY . .
RUN mvn clean package -DskipTests=true

FROM eclipse-temurin:25 AS final_stage

RUN apt-get update && apt-get install -y --no-install-recommends wget gnupg software-properties-common git maven

RUN wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
RUN echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list
RUN apt-get update
RUN apt-get install -y --no-install-recommends temurin-8-jdk temurin-11-jdk temurin-17-jdk
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /astor
COPY --from=astor_build /astor/target/astor-*-jar-with-dependencies.jar ./astor.jar
COPY --from=astor_build /astor/lib ./lib
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh
