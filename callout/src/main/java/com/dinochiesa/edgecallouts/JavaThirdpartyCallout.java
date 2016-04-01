package com.dinochiesa.edgecallouts;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.IOIntensive;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;

// jackson v1.9.7 is included in Edge
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonNode;
// commons-lang v2.6 is included in Edge
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;

// Google's guava JAR provides these. It is included in Edge.
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.base.Predicate;
import com.google.common.base.Function;


@IOIntensive
public class JavaThirdpartyCallout implements Execution {
    private final static String varprefix = "java_";
    private Map properties; // read-only
    private final static ObjectMapper om = new ObjectMapper();

    public JavaThirdpartyCallout (Map properties) { this.properties = properties; }

    private static String varName(String s) { return varprefix + s; }

    public ExecutionResult execute(MessageContext msgCtxt,
                                   ExecutionContext exeCtxt)
    {
        try {
            msgCtxt.setVariable(varName("hello"), "world");
            Properties sysProps = System.getProperties();
            String cp = sysProps.getProperty("java.class.path");

            List<String> elements = Arrays.asList(cp.split(":"));

            Collection<String> selected = Collections2.filter
                (elements, new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        return input.indexOf("/thirdparty/") > 0;
                    }
                });

            Function<String,String> fn = new Function<String,String>(){
                @Override
                public String apply(String input) {
                    return input.replaceAll("/.+/thirdparty/([A-Za-z1-9\\.]+)", "$1");
                }
            };

            Collection<String> result = Collections2.transform(selected, fn);
            String[] filteredItems = result.toArray(new String[0]);
            Arrays.sort(filteredItems);

            String payload = null;
            String accept = msgCtxt.getVariable("request.header.accept");
            if (accept != null && accept.startsWith("application/json")) {
                Map<String, String[]> map = new HashMap<String, String[]>();
                map.put("thirdpartylibs", filteredItems);
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
            else if (accept != null && accept.startsWith("text/xml")) {
                payload = "<root>not supported</root>";
            }
            else {
                // default to returning plain text
                msgCtxt.setVariable("message.header.content-type", "text/plain");
                payload = StringUtils.join(filteredItems, "\n");
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
