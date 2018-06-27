package com.diplomska.camel;

public class MyTransformer {

    public String TransformContent(String body){

        String converted = body.toUpperCase();
        System.out.println("Converted: " + converted);
        converted += "\n---TransformContent bean---";

        return converted;
    }
}
