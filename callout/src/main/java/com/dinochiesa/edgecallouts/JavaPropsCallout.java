package com.dinochiesa.edgecallouts;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.IOIntensive;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;

// commons-lang v2.6 is included in Edge
import org.apache.commons.lang.exception.ExceptionUtils;
// jackson v1.9.7 is included in Edge
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;

@IOIntensive
public class JavaPropsCallout implements Execution {
    private final static String varprefix = "java_";
    private Map properties; // read-only
    private final static ObjectMapper om = new ObjectMapper();

    public JavaPropsCallout (Map properties) { this.properties = properties; }

    private static String varName(String s) { return varprefix + s; }

    public ExecutionResult execute(MessageContext msgCtxt, ExecutionContext exeCtxt) {
        try {
            Properties sysProps = System.getProperties();
            msgCtxt.setVariable(varName("hello"), "world");
            Enumeration e = sysProps.propertyNames();

            String payload = null;
            String accept = msgCtxt.getVariable("request.header.accept");
            if (accept.startsWith("application/json")) {
                Map<String, String> map = new HashMap<String, String>();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    map.put(key, sysProps.getProperty(key));
                }

                // transform the map into a JSON payload
                try {
                    payload = om.writer()
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(map);
                    msgCtxt.setVariable("message.header.content-type", "application/json");
                }
                catch(java.io.IOException exc1) {
                    payload = "{\"exception\": \"" + exc1.toString() + "\"}";
                }
            }
            else if (accept.startsWith("text/xml")) {
                payload = "<root>not supported</root>";
            }
            else {
                // default to returning plain text
                StringBuilder sb = new StringBuilder(100);
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    sb.append(key + "=" + sysProps.getProperty(key)+"\n");
                }
                payload = sb.toString();
                msgCtxt.setVariable("message.header.content-type", "text/plain");
            }

            // Put the payload into the message.  This will go into the response
            // content if this callout runs in the response flow.  It will go into
            // the request content (perhaps not what you want) if this policy runs
            // on the request flow.
            msgCtxt.setVariable("message.content", payload+"\n");
        }
        catch (Exception e) {
            //e.printStackTrace(); // will go to stdout of message processor
            msgCtxt.setVariable(varName("error"), "Exception " + e.toString());
            msgCtxt.setVariable(varName("stacktrace"), ExceptionUtils.getStackTrace(e));
            return ExecutionResult.ABORT;
        }
        return ExecutionResult.SUCCESS;
    }
}
