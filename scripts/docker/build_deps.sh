mvn dependency:copy-dependencies -DoutputDirectory=lib -DincludeScope=test -pl . -am
cp -r target/classes/* lib/ 2>/dev/null;
cp -r target/test-classes/* lib/ 2>/dev/null