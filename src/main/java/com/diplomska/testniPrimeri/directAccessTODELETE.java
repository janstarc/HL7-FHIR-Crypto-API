package com.diplomska.testniPrimeri;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.valueset.*;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;

import java.util.List;

import static com.diplomska.constants.address.*;
import static com.diplomska.testniPrimeri.testniPrimeri.*;

public class directAccessTODELETE {

    public static void main(String[] args){

        addCondition();
        //findResource();
    }

    public static void findResource(){

        // Create FHIR context
        FhirContext ctx = FhirContext.forDstu2();
        IGenericClient client = ctx.newRestfulGenericClient(HapiRESTfulServer);

        // Search with encoded parameters
        Bundle search = client
                .search()
                .forResource(BaseResource.class)
                .where(new StringClientParam("_content").matches().value("Patient/6m6MxjXnHoDCWUyCJqEP1Q=="))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .encodedJson()
                .execute();

        // Convert bundle to List<Observation>
        List<BaseResource> resultArray = search.getAllPopulatedChildElementsOfType(BaseResource.class);
        System.out.println("Result Array size: " + resultArray.size());

        /*
        // Loop through the Observation list, decrypt hashed parameters
        for (Observation o : resultArray) {

            // Decrypt the values
            try {
                // Find the right extension, decrypt the value
                List<ExtensionDt> extList = o.getUndeclaredExtensions();
                if(extList.size() > 0){
                    for(ExtensionDt ext : extList){
                        if(ext.getElementSpecificId().equals("encryptedReference")){
                            ext.setValue(new StringDt("Patient/" + _id));
                        }
                    }
                }

                // Write log
                System.out.println("Found " + search.getEntry().size() + " results.");
                System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(search));

            } catch (Exception e){
                e.printStackTrace();
            }
        }
        */
    }

    public static void addCondition(){
        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = HapiPublicTestServer;
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);
        //String ident = p.getId().getValue();

        Condition condition = new Condition();
        condition
                .getCode()
                .addCoding()
                .setSystem("http://loinc.org")
                .setCode("789-8")
                .setDisplay("Test 2");

        condition
                .setClinicalStatus(ConditionClinicalStatusCodesEnum.ACTIVE)
                .setVerificationStatus(ConditionVerificationStatusEnum.CONFIRMED);

        ExtensionDt ext = new ExtensionDt();
        ext.setElementSpecificId("encryptedReference");
        ext.setValue(new StringDt("Patient/5YYX8eiaCLsUg3eSNrxn5g=="));
        condition.addUndeclaredExtension(ext);

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
                        .setValue(randomNum())
                        .setUnit("test" + randomNum() + "testEnota")
                        .setSystem("http://unitsofmeasure.org")
                        .setCode("10*12/L"));

        Bundle bundle = new Bundle();
        bundle.setType(BundleTypeEnum.TRANSACTION);
        bundle.addEntry()
                .setResource(condition)
                .getRequest()
                .setUrl("Observation")
                .setMethod(HTTPVerbEnum.POST);
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

    // TODO Direct access to HAPI server! DELETE!
    public static void addResourceToPatient(Patient p) {

        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = HapiRESTfulServer;
        //String serverBase = "http://hapi.fhir.org/baseDstu2";
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

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
                        .setValue(randomNum())
                        .setUnit("test" + randomNum() + "testEnota")
                        .setSystem("http://unitsofmeasure.org")
                        .setCode("10*12/L"));


        //observation.setSubject(new ResourceReferenceDt("Patient/1124")); // TO DELETE!
        String _id = String.valueOf(p.getId().getIdPartAsLong());
        System.out.println("ID: " + _id);

        String encryptedRef = "testCeToleDela";
        //ResourceReferenceDt resourceReferenceDt = new ResourceReferenceDt(_id);
        //observation.setSubject(new ResourceReferenceDt("Patient/" + encryptedRef));

        ExtensionDt ext = new ExtensionDt();
        ext.setElementSpecificId("encryptedReference");
        ext.setValue(new StringDt("Patient/GVJiNefvk3wDfaDqS5xh0Q=="));
        observation.addUndeclaredExtension(ext);

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
}
