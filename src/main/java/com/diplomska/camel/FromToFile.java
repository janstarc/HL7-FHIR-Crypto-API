package com.diplomska.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class FromToFile {

    public static void main(String[] args) throws Exception {

        // create CamelContext
        CamelContext context = new DefaultCamelContext();

        // add our route to the CamelContext
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("file:src/data/inbox?noop=true").to("file:data/outbox");
            }
        });

        // start the route and let it do its work
        context.start();
        Thread.sleep(5000);

        // stop the CamelContext
        context.stop();
    }
}
