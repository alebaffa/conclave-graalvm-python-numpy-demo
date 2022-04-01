# Conclave polyglot demo

## Goal
Run a Java program that calls a Python program that uses Numpy library, all inside an enclave

----
## Resources

This is a mix of:
- Conclave sample in Java provided by Conclave that can be found inside the `conclave-sdk/hello-world`. 
- [This GraalVM demo](https://github.com/abvijaykumar/graalvm-numpy-polyglot) in Java that calls a Python program that uses Numpy

## Configuration changes
In the `build.gradle` of the enclave (of the hello-world sample) you need to add two dependencies needed for GraalVM:
```
implementation 'org.graalvm.truffle:truffle-api:20.1.0'
implementation 'org.graalvm.sdk:graal-sdk:20.1.0'
```
## Code change
The ReverseEnclave.java has been changed in order to use Polyglot library and call the Python program:
``` java
 Context ctx = Context.newBuilder().allowAllAccess(true).build();
    try {
        File fibCal = new File("./heartAnalysis.py");
        ctx.eval(Source.newBuilder("python", fibCal).build());
        
        Value hearAnalysisFn = ctx.getBindings("python").getMember("heartAnalysis");

        Value heartAnalysisReport = hearAnalysisFn.execute();
        System.out.println("Average age of people who are getting level 3 and greater chest pain :" + heartAnalysisReport.toString());

    }   catch (Exception e) {
        System.out.println("Exception : " );
        e.printStackTrace();
    }
```
GraalVM parses the `heartAnalysis.py` source code in Python and allows to call its method with 
```java
ctx.getBindings("python").getMember("heartAnalysis").execute();
```

### Notes

1. It seems that GraalVM support is only for Linux at the moment. This demo has been performed on Ubuntu.
2. I had to install `sudo apt install zlib1g-dev` to compile the project with Polyglot.

## How to setup

- Download GraalVM (support for Python should already be included by default)
- Install numpy inside GraalVM
```
graalpython -m ginstall install numpy
```
- Update environmental variables
```
export JAVA_HOME=/path/to/graalvm-ce-java11-22.0.0.2
export PATH=JAVA_HOME/bin:$PATH
```

## How to run

Start the host on a Linux system, which will build the enclave and host:

```bash
./gradlew host:bootJar
java -jar host/build/libs/host-mock.jar
```

It should print out some info about the started enclave. Then you can use the client to send it strings to reverse:

```bash
./gradlew client:shadowJar
java -jar client/build/libs/client.jar "S:0000000000000000000000000000000000000000000000000000000000000000 PROD:1 SEC:INSECURE" "reverse me"
```

Check the output from the host:
```
55.869565217391305
55.869565217391305
Average age of people who are getting level 3 and greater chest pain :'55.869565217391305'
```
this is the output from the python program inside the enclave that is printed out in the console.
