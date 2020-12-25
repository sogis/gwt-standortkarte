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

## develop

First Terminal:
```
mvn spring-boot:run -pl *-server -am
```

Second Terminal:
```
mvn gwt:codeserver -pl *-client -am
```



**todo**:
```
BUILD_NUMBER=9999 mvn clean package -nsu
```

```
mvn dependency:tree
```

```
docker build -t sogis/standortkarte -f Dockerfile.layered .
```


## run
```
docker run -p 8080:8080 sogis/standortkarte
```

