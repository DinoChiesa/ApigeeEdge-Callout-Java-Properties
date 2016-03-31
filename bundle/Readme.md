# API Proxy bundle to demonstrate Java Props callout

This API Proxy returns information about the JVM running the Message
Processors. It's possibly useful for diagnostic purposes. It can be used
on private cloud or public cloud instances of Edge. 

## Example usage

Invoke this proxy with something like this: 

### Example 1:

To get the java properties, if running in the Apigee-managed public cloud:
```
  curl -i -X GET http://ORGNAME-ENVNAME.apigee.net/java-props/props 
```
Result: 

```
  200 OK

  ....LARGE LIST OF JVM PROPERTIES HERE...
```

To get the java properties, if running in a self-managed Edge cluster:
```
  curl -i -X GET http://VHOSTIP:VHOSTPORT/java-props/props 
```


## Example 2: 

```
  curl -i -X GET http://ORGNAME-ENVNAME.apigee.net/java-props/3rdpartyjars
```

Result: 

```
  200 OK 

  ...long list of 3rd party jars here...
```



