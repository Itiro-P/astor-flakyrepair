From IDOFT:
- https://github.com/chinabugotech/hutool/pull/3378
  - https://github.com/yesh385/hutool.git
  - (cd "$(pwd)/samples/hutool" && \
    mvn clean compile test-compile package dependency:copy-dependencies -pl hutool-json -am \
    -DskipTests=true -DoutputDirectory=lib -DincludeScope=test) && \
    java -cp target/astor-*-jar-with-dependencies.jar fr.inria.main.evolution.AstorMain \
    -mode custom -stopfirst false \
    -customengine fr.inria.astor.approaches.flakydebug.FlakyDebugEngine \
    -javacompliancelevel 8 \
    -srcjavafolder src/main/java \
    -srctestfolder src/test/java  \
    -binjavafolder target/classes \
    -bintestfolder target/test-classes \
    -location "$(pwd)/samples/hutool/hutool-json" \
    -dependencies "$(pwd)/samples/hutool/hutool-json/lib"

- https://github.com/Graylog2/graylog2-server/pull/7473
  - https://github.com/cpugputpu/graylog2-server.git
  - (cd "$(pwd)/samples/graylog2-server" && \
    mvn clean compile test-compile package dependency:copy-dependencies \
    -DskipTests=true -DoutputDirectory=lib -DincludeScope=test) && \
    java -cp target/astor-*-jar-with-dependencies.jar fr.inria.main.evolution.AstorMain \
    -mode custom -stopfirst false \
    -customengine fr.inria.astor.approaches.flakydebug.FlakyDebugEngine \
    -javacompliancelevel 11 \
    -srcjavafolder src/main/java \
    -srctestfolder src/test/java  \
    -binjavafolder target/classes \
    -bintestfolder target/test-classes \
    -location "$(pwd)/samples/graylog2-server" \
    -dependencies "$(pwd)/samples/graylog2-server/lib"

- https://github.com/google/guava/pull/8035
  - https://github.com/annhchen89/guava.git

- https://github.com/flowable/flowable-engine/pull/2197
  - https://github.com/cpugputpu/flowable-engine.git

- https://github.com/amzn/amazon-pay-api-sdk-java/pull/21
  - https://github.com/Sujishark/amazon-pay-api-sdk-java.git

Open-source projects with no revealed flaky tests: