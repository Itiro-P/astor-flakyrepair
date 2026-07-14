From IDOFT:
- https://github.com/bpsm/edn-java/pull/68
  - https://github.com/hsiangawang/edn-java.git

  - ./scripts/clone_pr.sh https://github.com/hsiangawang/edn-java.git samples
  - docker build --build-arg USER_UID=$(id -u) --build-arg USER_GID=$(id -g) -t astor .
  - ./scripts/run_astor_docker.sh edn-java 8 samples/edn-java

- https://github.com/google/guava/pull/8035
  - https://github.com/annhchen89/guava.git

- https://github.com/flowable/flowable-engine/pull/2197
  - https://github.com/cpugputpu/flowable-engine.git

- https://github.com/amzn/amazon-pay-api-sdk-java/pull/21
  - https://github.com/Sujishark/amazon-pay-api-sdk-java.git

Open-source projects with no revealed flaky tests: