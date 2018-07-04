package com.diplomska.testniPrimeri;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.HTTPVerbEnum;
import ca.uhn.fhir.model.dstu2.valueset.ObservationStatusEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
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
import java.util.List;

public class testniPrimeri {

    public static String HapiRESTfulServer = "http://localhost:8080/hapi/baseDstu2";
    public static String HapiAccessPoint = "http://localhost:7050/hapi.do";

    public static void main(String[] args) throws IOException, URISyntaxException {
        String given = "Jan";
        String family = "Starc";

        //addPatient(given, family);
        //getPatient("Novi", "Test");
        getPatientById("1");
    }

    public static void getPatientByGivenFamily(String given, String family) throws IOException, URISyntaxException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder url = new URIBuilder(HapiAccessPoint);
        url.setParameter("given", given);
        url.setParameter("family", family);
        HttpGet request = new HttpGet(String.valueOf(url));
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        System.out.println("----- RESPONSE -----\n" + responseString);

    }

    public static void getPatientById(String id) throws IOException, URISyntaxException {

        // We're connecting to a DSTU1 compliant server in this example
        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = HapiRESTfulServer;

        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        // Perform a search
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value("ZXBnTTumRMqzT5Oel36QsQ=="))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .execute();

        List<Patient> patientList = results.getAllPopulatedChildElementsOfType(Patient.class);
        Patient p = patientList.get(0);
        System.out.println("Test --> Given: " + p.getName().get(0).getGivenAsSingleString());
        String ident = p.getId().getValue();
        System.out.println("Identifier: " + ident);

        // Create an observation object
        Observation observation = new Observation();
        observation.setStatus(ObservationStatusEnum.FINAL);
        observation
                .getCode()
                .addCoding()
                .setSystem("http://loinc.org")
                .setCode("789-8")
                .setDisplay("Test 2");
        observation.setValue(
                new QuantityDt()
                        .setValue(4.12)
                        .setUnit("10 trillion/L")
                        .setSystem("http://unitsofmeasure.org")
                        .setCode("10*12/L"));



        //observation.setSubject(new ResourceReferenceDt(p.getId().getValue()));
        //IdDt idDt = new IdDt("hashValue");
        observation.setSubject(new ResourceReferenceDt("Patient/krneki"));

        Bundle bundle = new Bundle();
        bundle.setType(BundleTypeEnum.TRANSACTION);
        bundle.addEntry()
                .setResource(observation)
                .getRequest()
                .setUrl("Observation")
                .setMethod(HTTPVerbEnum.POST);

        System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));

        // Create a client and post the transaction to the server
        Bundle resp = client.transaction().withBundle(bundle).execute();

        // Log the response
        System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resp));

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
            HttpPost request = new HttpPost(HapiAccessPoint);
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
