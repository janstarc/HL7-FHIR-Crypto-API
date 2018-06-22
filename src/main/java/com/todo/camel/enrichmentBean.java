package com.todo.camel;

public class enrichmentBean {

    public String enrich(String body){
        return body + "\nEnriched in enrichment bean";
    }
}
