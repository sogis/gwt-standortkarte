# gwt-standortkarte

## create empty project from archetype
```
mvn org.apache.maven.plugins:maven-archetype-plugin:2.2:generate \
   -DarchetypeGroupId=com.github.nalukit.archetype \
   -DarchetypeVersion=LATEST \
   -DarchetypeArtifactId=modular-springboot-webapp
```
With Java 11 there are empty lines in parent pom.xml. Use Java 8 to create empty project.

For creating a fat jar we need to adjust the `env-prod` profile in the server module: Unpack gwt war and copy it into the public folder of the server module. But there is some build smell: https://stackoverflow.com/questions/30642630/artifact-has-not-been-packaged-yet

Use application.yml instead of *.properties.

## develop

First Terminal:
```
./mvnw spring-boot:run -Penv-dev -pl *-server -am
```

Second Terminal:
```
./mvnw gwt:codeserver -pl *-client -am
./mvnw gwt:codeserver -pl *-client -am -nsu
```

Build only server module
```
./mvnw clean install -pl :standortkarte-server -nsu
```

## build

### jvm
```
./mvnw clean package
docker build -t sogis/standortkarte-jvm -f Dockerfile.jvm .
```

### native image
Do not forget to:

a) use Java GraalVM distribution
b) install native-image: `gu install native-image`

```
./mvnw -Penv-prod,native clean package
docker build -t sogis/standortkarte -f Dockerfile.native .
```

```
docker build -t sogis/standortkarte -f Dockerfile.native-build .
```

## run
```
docker run -p 8080:8080 sogis/standortkarte
docker run -e TZ=Europe/Zurich -e SPRING_PROFILES_ACTIVE=prod -p 8080:8080 sogis/standortkarte

```

## examples

```
http://localhost:8080/index.html?egid=2122818
http://localhost:8080/index.html?egid=1766088&edid=1
```