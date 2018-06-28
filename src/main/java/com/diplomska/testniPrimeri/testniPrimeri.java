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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class testniPrimeri {

    public static void main(String[] args) throws IOException, URISyntaxException {
        //addPatient();
        getPatient();
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

    public static void getPatient() throws IOException, URISyntaxException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder url = new URIBuilder("http://localhost:8080/getResource.do/Patient");
        url.setParameter("family", "Novak");
        HttpGet request = new HttpGet(String.valueOf(url));
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        System.out.println("----- RESPONSE -----\n" + responseString);
        /*
        Bundle search = client
		      .search()
		      .forResource(Patient.class)
		      .where(Patient.FAMILY.matches().value("Perc"))
		      .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
		      .execute();
         */
        /*
        BasicConfigurator.configure();
        FhirContext ctx = FhirContext.forDstu2();


        SearchParameterMap paramMap = new SearchParameterMap();
        Bundle b = new Bundle();
        b.setType(BundleTypeEnum.MESSAGE).getAllPopulatedChildElementsOfType(Patient.class);
        String requestBody = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(b);
        Bundle.EntrySearch b2 = new Bundle.EntrySearch();
        //b2.getAllPopulatedChildElementsOfType(Patient.class).

        //IGenericClient client = ctx.newRestfulGenericClient("http://hapi.fhir.org/baseDstu2");
        IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/getResource.do/");
        //IGenericClient cli = ctx.newJsonParser().;
        //GenericClient client = new GenericClient(ctx);
        Bundle search = client
                .search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value("Novak"))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .andLogRequestAndResponse(true)
                .execute();

        System.out.println("Found " + search.getEntry().size() + " results.");
        */
        //StringBuilder result = new StringBuilder();
        //URL url = new URL("http://localhost:8080/getResource.do/Patient");
        //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //conn.setRequestMethod("GET");
        //URIBuilder url = new URIBuilder("http://localhost:8080/getResource.do/Patient");
        //url.setParameter("family", "Novak");
        //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //conn.setRequestMethod("GET");
        //HttpGet get = new HttpGet(String.valueOf(url));


        //URIBuilder builder = new URIBuilder();
        //builder.setScheme("http").setHost("localhost:8080").setPath("/getResource.do/Patient")
        //        .setParameter("family", "Novak");

        //URI uri = builder.build();
        //String urlString = String.valueOf(httpget.getURI());
        //URL url = new URL(urlString);
        //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //conn.setRequestMethod("GET");
        //HttpResponse response = http

    }
}
