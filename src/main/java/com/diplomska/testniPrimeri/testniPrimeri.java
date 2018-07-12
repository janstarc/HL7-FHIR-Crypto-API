package com.diplomska.testniPrimeri;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.*;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.diplomska.crypto.cryptoService;
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
import java.util.Random;

import static com.diplomska.constants.address.*;

public class testniPrimeri {

    private static cryptoService crypto = new cryptoService();

    public static void main(String[] args) throws IOException, URISyntaxException {
        String given = "Testni";
        String family = "Pacient4";

        /** Test 1.1 */
        //addPatient(given, family);

        /** Test 1.2 */
        /*
        try{
            Patient p = getPatientById("11579");
            System.out.println("Test --> _id: " + p.getId().getIdPartAsLong());
            addObservationToPatient(p);
        } catch (Exception e){
            e.printStackTrace();
        }
        */

        /** Test 1.3 */
        /*
        try{
            Patient p = getPatientById("11579");
            System.out.println("Test --> _id: " + p.getId().getIdPartAsLong());
            addConditionToPatient(p);
        } catch (Exception e){
            e.printStackTrace();
        }
        */

        /** Test 2.1.1 */
        //Patient p = getPatientById("11579");

        /** Test 2.1.2 */
        //getPatientByGivenFamily(given, family);

        /** Test 2.2 */
        //getAllObservationsForPatient("11579");

        /** Test 2.3 */
        getAllConditionsForPatient("11579");


    }

    public static void getAllConditionsForPatient(String _id) throws URISyntaxException, IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder url = new URIBuilder(HapiAccessPointCondition);
        url.setParameter("patient", _id);
        HttpGet request = new HttpGet(String.valueOf(url));
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        System.out.println("---RESPONSE---\n" + responseString);
    }

    // GET all observations for Patient ID
    public static void getAllObservationsForPatient(String _id) throws URISyntaxException, IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder url = new URIBuilder(HapiAccessPointObservation);
        url.setParameter("patient", _id);
        HttpGet request = new HttpGet(String.valueOf(url));
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        System.out.println("---- RESPONSE -----\n" + responseString);
    }

    // POST observation
    public static void addObservationToPatient(Patient p){

        // Get the patient ID
        String _id = String.valueOf(p.getId().getIdPartAsLong());

        // Create an observation object
        Observation observation = new Observation();
        observation.setStatus(ObservationStatusEnum.FINAL);
        observation
                .getCode()
                .addCoding()
                .setSystem("http://loinc.org")
                .setCode("789-8")
                .setDisplay("Observation1, TP3");
        observation.setValue(
                new QuantityDt()
                        .setValue(randomNum())
                        .setUnit("test" + randomNum() + "testEnota")
                        .setSystem("http://unitsofmeasure.org")
                        .setCode("10*12/L"));

        //observation.setSubject(new ResourceReferenceDt(p.getId()));
        ExtensionDt ext = new ExtensionDt();
        ext.setElementSpecificId("encryptedReference");
        ext.setValue(new StringDt("Patient/" + _id));
        observation.addUndeclaredExtension(ext);


        // Create a Bundle, containing the Observation object
        Bundle bundle = new Bundle();
        bundle.setType(BundleTypeEnum.TRANSACTION);
        bundle.addEntry()
                .setResource(observation)
                .getRequest()
                    .setUrl("Observation")
                    .setMethod(HTTPVerbEnum.POST);

        FhirContext ctx = FhirContext.forDstu2();
        String requestBody = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        System.out.println("Req body: " + requestBody);

        // Send a POST request to the server
        HttpClient httpClient = HttpClientBuilder.create().build();
        try{
            HttpPost request = new HttpPost(HapiAccessPointObservation);
            StringEntity params = new StringEntity(requestBody);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            System.out.println("---- RESPONSE ----\n" + responseString);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void addConditionToPatient(Patient p){

        // Get the patient ID
        String _id = String.valueOf(p.getId().getIdPartAsLong());

        // Create an observation object
        Condition condition = new Condition();
        condition.setClinicalStatus(ConditionClinicalStatusCodesEnum.ACTIVE);
        condition.setCategory(ConditionCategoryCodesEnum.COMPLAINT);
        condition.setNotes("Test Condition");

        //observation.setSubject(new ResourceReferenceDt(p.getId()));
        ExtensionDt ext = new ExtensionDt();
        ext.setElementSpecificId("encryptedReference");
        ext.setValue(new StringDt("Patient/" + _id));
        condition.addUndeclaredExtension(ext);


        // Create a Bundle, containing the Observation object
        Bundle bundle = new Bundle();
        bundle.setType(BundleTypeEnum.TRANSACTION);
        bundle.addEntry()
                .setResource(condition)
                .getRequest()
                .setUrl("Condition")
                .setMethod(HTTPVerbEnum.POST);

        FhirContext ctx = FhirContext.forDstu2();
        String requestBody = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        System.out.println("Req body: " + requestBody);

        // Send a POST request to the server
        HttpClient httpClient = HttpClientBuilder.create().build();
        try{
            HttpPost request = new HttpPost(HapiAccessPointCondition);
            StringEntity params = new StringEntity(requestBody);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            System.out.println("----- RESPONSE -----\n" + responseString);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // GET Patient by Given and Family name
    public static void getPatientByGivenFamily(String given, String family) throws IOException, URISyntaxException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder url = new URIBuilder(HapiAccessPointPatient);
        url.setParameter("given", given);
        url.setParameter("family", family);
        HttpGet request = new HttpGet(String.valueOf(url));
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        System.out.println("----- RESPONSE -----\n" + responseString);

    }

    // GET Patient by ID
    public static Patient getPatientById(String id) throws IOException, URISyntaxException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder url = new URIBuilder(HapiAccessPointPatient);
        url.setParameter("_id", id);
        System.out.println("Value of URL = " + String.valueOf(url));
        HttpGet request = new HttpGet(String.valueOf(url));
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        System.out.println("----- RESPONSE -----\n" + responseString);

        FhirContext ctx = FhirContext.forDstu2();
        System.out.println("------------TEST------------");
        System.out.println(responseString);
        System.out.println("------------TEST------------");
        Bundle result = (Bundle) ctx.newJsonParser().parseResource(responseString);
        List<Patient> patientList = result.getAllPopulatedChildElementsOfType(Patient.class);
        Patient p;
        if(patientList.size() == 1){
            p = patientList.get(0);
            return p;
        } else {
            System.out.println("No result");
        }

        return null;
    }

    // POST new Patient, encrypt First and Last
    public static void addPatient(String given, String family){
        Patient patient = new Patient();

        // ..populate the patient object..
        patient.addIdentifier().setSystem("urn:system").setValue(String.valueOf(randomNum()));
        patient.addName().addFamily(family).addGiven(given);

        // Log the request
        FhirContext ctx = FhirContext.forDstu2();
        //String requestBody = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        //System.out.println(requestBody);

        // Create a Bundle, containing the Observation object
        Bundle bundle = new Bundle();
        bundle.setType(BundleTypeEnum.TRANSACTION);
        bundle.addEntry()
                .setResource(patient)
                .getRequest()
                .setUrl("Patient")
                .setMethod(HTTPVerbEnum.POST);

        String requestBody = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);

        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(HapiAccessPointPatient);
            StringEntity params = new StringEntity(requestBody);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            System.out.println("----- RESPONSE -----\n" + responseString);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static int randomNum(){
        Random r = new Random();
        int Low = 10;
        int High = 100;
        int Result = r.nextInt(High-Low) + Low;
        return Result;
    }
}
