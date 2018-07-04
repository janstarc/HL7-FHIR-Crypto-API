package com.diplomska.testniPrimeri;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.net.URISyntaxException;

public class testniPrimeri {

    public static void main(String[] args) throws IOException, URISyntaxException {
        addPatient("Novi", "Test");
        //getPatient("Alenka", "Starc");
    }

    public static void getPatient(String given, String family) throws IOException, URISyntaxException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder url = new URIBuilder("http://localhost:7050/hapi.do");
        url.setParameter("given", given);
        url.setParameter("family", family);
        HttpGet request = new HttpGet(String.valueOf(url));
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        System.out.println("----- RESPONSE -----\n" + responseString);

    }

    public static void addPatient(String given, String family){
        Patient patient = new Patient();
        // ..populate the patient object..
        patient.addIdentifier().setSystem("urn:system").setValue("17061996");
        patient.addName().addFamily(family).addGiven(given);

        // Log the request
        FhirContext ctx = FhirContext.forDstu2();
        String requestBody = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        System.out.println(requestBody);

        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost("http://localhost:7050/hapi.do");
            StringEntity params = new StringEntity(requestBody);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            System.out.println("----- RESPONSE -----\n" + responseString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
