# Apigee Edge callout: Java Properties

This directory contains the Java source code for a simple Java callout that
works with Apigee Edge. It does two things:  
* collects Java System and OS properties and formats them into the message payload
* interrogates and lists the 3rd party Jars available at runtime in Apigee Edge

## Building

1. unpack (if you can read this, you've already done that).

2. Before building the first time, you must import the apigee jars into your local Maven cache.
   Do this with the [buildsetup.sh](buildsetup.sh) script:
   ```
   buildsetup.sh
   ```

3. ```mvn clean package```


Congratulations! You have built the Jar.


## Using this Custom Policy in a bundle

If you are using [the sample proxy](../bundle) included here, you don't need to do the first two steps; the sample already has the custom policy JAR present in the proxy. 


1. copy target/edge-callout-javaprops.jar to your apiproxy/resources/java directory
   also copy all the lib/*.jar files to the same directory.
   This pom file copies the appropriate jar to the resources/java directory for
   the sample proxy included here. 

2. Be sure to include a Java callout policy in your
   apiproxy/resources/policies directory. It should look like
   this:
    ```xml
    <JavaCallout name="JavaProps">
      <DisplayName>Java Props Interrogator</DisplayName>
      <ClassName>com.dinochiesa.edgecallouts.JavaPropsCallout</ClassName>
      <ResourceURL>java://edge-callout-javaprops.jar</ResourceURL>
    </JavaCallout>
   ```
   
3. Import and deploy the API proxy bundle into your Edge organization with a tool like [pushapi](https://github.com/carloseberhardt/apiploy) or [apigeetool](https://github.com/apigee/apigeetool-node).

4. invoke the API proxy.



## Dependencies

Jars available in Edge: 
* Apigee Edge expressions v1.0
* Apigee Edge message-flow v1.0
* Apache commons lang v2.6 - for ExceptionUtils
* Jackson v1.9.7 - for JSON serialization
* Google Guava v16.0.1 - for Collections and Function

All these jars must be available on the classpath for the compile to
succeed. They are available within Apigee Edge, so there's no need to
include them when you use the callout jar in an Edge proxy.


## Notes:

There are two callout classes, one to generate and return the list of Java properties running in the Message Processor.  Another to generate and return the list of 3rd party jars in the Message Processor.


