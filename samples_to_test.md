From IDOFT:
- https://github.com/bpsm/edn-java/pull/68
  - https://github.com/hsiangawang/edn-java.git

  - ./scripts/clone_pr.sh https://github.com/hsiangawang/edn-java.git samples
  - docker build --build-arg USER_UID=$(id -u) --build-arg USER_GID=$(id -g) -t astor .
  - ./scripts/run_astor_docker.sh edn-java 8 samples/edn-java


- https://github.com/amzn/amazon-pay-api-sdk-java/pull/21
  - https://github.com/Sujishark/amazon-pay-api-sdk-java.git

  - ./scripts/clone_pr.sh https://github.com/Sujishark/amazon-pay-api-sdk-java.git samples
  - docker build --build-arg USER_UID=$(id -u) --build-arg USER_GID=$(id -g) -t astor .
  - ./scripts/run_astor_docker.sh amazon-pay-api-sdk-java 8 samples/amazon-pay-api-sdk-java src/com/amazon/pay/api tst/com/amazon/pay/api

- https://github.com/eclipse-vertx/vert.x/pull/4998
  - https://github.com/219sansim/vert.x.git

  - ./scripts/clone_pr.sh https://github.com/219sansim/vert.x.git samples
  - docker build --build-arg USER_UID=$(id -u) --build-arg USER_GID=$(id -g) -t astor .
  - ./scripts/run_astor_docker.sh vert-x 11 samples/vert.x


Open-source projects with no revealed flaky tests: