package com.diplomska.camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import javax.jms.ConnectionFactory;

// Run command: mvn compile exec:java -Dexec.mainClass=contentEnricher.ContentEnricher
public class ContentEnricher {


    public static void main(String[] args) throws Exception {

        // Kreiranje Camel Contexta
        CamelContext context = new DefaultCamelContext();
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
        context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

        // Dodajanjanje novega routa
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("file:src/data/inbox?noop=true").to("jms:incomingFiles");      // Iz datoteke v jms queue

                // Content based router
                from("jms:incomingFiles")
                .choice()
                    .when(header("CamelFileName").endsWith(".txt"))
                        .to("jms:beanQueue")
                    .when(header("CamelFileName").endsWith(".tx"))
                        .to("jms:dslQueue")
                    .otherwise()
                        .to("jms:processorQueue");

                // Content enrichment with bean --> Klice se MyTranformer() class
                from("jms:beanQueue")
                    .bean(new MyTransformer(), "TransformContent")
                    .to("jms:processedFiles");

                // Content enrichment in Java DSL
                from("jms:dslQueue")
                    .setBody(body().append("\nDodana vrstica - DSL Queue"))
                    .to("jms:processedFiles");


                // Content enrichment using processor
                from("jms:processorQueue")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            org.apache.camel.Message in = exchange.getIn();
                            in.setBody(in.getBody(String.class) + "New line added in processorQueue");
                        }
                    })
                .to("jms:processedFiles");

                // Zadnji queue, kjer se generirajo output datoteke
                from("jms:processedFiles")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            System.out.println("Content: " + exchange.getIn().getHeader("CamelFileName") + " - Pred pisanjem v file");
                        }
                    })
                .to("file:src/data/outbox");
            }
        });

        // start the route and let it do its work
        context.start();
        Thread.sleep(10000);

        // stop the CamelContext
        context.stop();
    }
}
