# gwt-standortkarte

## develop

First Terminal:
```
mvn clean spring-boot:run
```

Second Terminal:
```
mvn gwt:generate-module gwt:codeserver
```

Or simple devmode (which worked better for java.xml.bind on client side):
```
mvn gwt:generate-module gwt:devmode 
``` 

```
BUILD_NUMBER=9999 mvn clean package -nsu
```

```
mvn dependency:tree
```

```
docker build -t sogis/standortkarte .
```


## run
```
docker run -p 8080:8080 sogis/standortkarte
```

