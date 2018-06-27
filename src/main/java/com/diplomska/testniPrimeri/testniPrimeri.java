package com.diplomska.testniPrimeri;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Goal;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.HTTPVerbEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class testniPrimeri {

    public static void main(String[] args){
        addPatient();
    }

    public static void addPatient(){
        Patient patient = new Patient();
        // ..populate the patient object..
        patient.addIdentifier().setSystem("urn:system").setValue("17061996");
        patient.addName().addFamily("Perc").addGiven("Betka");

        // Invoke the server create method (and send pretty-printed JSON
        // encoding to the server
        // instead of the default which is non-pretty printed XML)

		/*
		MethodOutcome outcome = client.create()
		   .resource(patient)
		   .prettyPrint()
		   .encodedJson()
		   .execute();
		*/

        // Log the request
        FhirContext ctx = FhirContext.forDstu2();
        String requestBody = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        System.out.println(requestBody);

        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost("http://localhost:8080/addResource.do");
            StringEntity params = new StringEntity(requestBody);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            System.out.println("----- RESPONSE -----\n" + responseString);
            //Goal.Outcome o = ctx.newBundleFactory()responseString;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // The MethodOutcome object will contain information about the
        // response from the server, including the ID of the created
        // resource, the OperationOutcome response, etc. (assuming that
        // any of these things were provided by the server! They may not
        // always be)
        //IdDt id = (IdDt) outcome.getId();
        //System.out.println("Got ID: " + id.getValue());
    }

    public static void getPatient(){

        FhirContext ctx = FhirContext.forDstu2();
        //String requestBody = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString();
    }
}
