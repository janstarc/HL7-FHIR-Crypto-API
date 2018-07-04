package com.diplomska.intercept;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.diplomska.encryptDecrypt.cryptoService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/hapi.do"})
public class hapi extends HttpServlet {

    // Get requesti - iskanje pacientov
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Get the parameter
        String family = request.getParameter("family");
        String given = request.getParameter("given");

        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder uri;
        try {
            // Send request to crypto --> Encrypt search parameters
            uri = new URIBuilder("http://localhost:7050/crypto.do");
            uri.setParameter("encrypt", "true");
            uri.setParameter("given", given);
            uri.setParameter("family", family);
            HttpGet requestToCrypto = new HttpGet(String.valueOf(uri));
            HttpResponse encryptedGet = httpClient.execute(requestToCrypto);

            // Crypto returns JSON object with encrypted search parameters
            String encryptedJson = EntityUtils.toString(encryptedGet.getEntity());
            JsonObject jObj = new Gson().fromJson(encryptedJson, JsonObject.class);
            String familyEnc = jObj.get("family").getAsString();
            String givenEnc = jObj.get("given").getAsString();
            System.out.println("Given enc: " + givenEnc + " Family enc: " + familyEnc);

            // Search with encoded parameters
            FhirContext ctx = FhirContext.forDstu2();
            IGenericClient client = ctx.newRestfulGenericClient("http://hapi.fhir.org/baseDstu2");

            // Search for the Patient - hashed value
            Bundle search = client
                    .search()
                    .forResource(Patient.class)
                    .where(Patient.FAMILY.matches().value(familyEnc))
                    .and(Patient.GIVEN.matches().value(givenEnc))
                    .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                    .encodedJson()
                    .execute();

            // Convert bundle to List<Patient>
            List<Patient> resultArray = search.getAllPopulatedChildElementsOfType(Patient.class);
            System.out.println("Result Array size: " + resultArray.size());

            // Loop through the patient list, decrypt hashed parameters
            for (Patient p : resultArray) {
                String fam = p.getName().get(0).getFamilyAsSingleString();
                String giv = p.getName().get(0).getGivenAsSingleString();
                System.out.println("Here?");

                try {
                    // Decrypt the values
                    uri = new URIBuilder("http://localhost:7050/crypto.do");
                    uri.setParameter("encrypt", "false");
                    uri.setParameter("given", fam);
                    uri.setParameter("family", giv);
                    HttpGet requestToCrypto2 = new HttpGet(String.valueOf(uri));
                    HttpResponse encryptedGet2 = httpClient.execute(requestToCrypto2);

                    // Crypto returns JSON object with encrypted search parameters
                    String encryptedJson2 = EntityUtils.toString(encryptedGet2.getEntity());
                    JsonObject jObj2 = new Gson().fromJson(encryptedJson2, JsonObject.class);
                    String famDecrypt = jObj2.get("given").getAsString();
                    String givDecrypt = jObj2.get("family").getAsString();

                    // Handle the resource conversion and change the value of object p
                    ArrayList<StringDt> famArray = new ArrayList<>();
                    famArray.add(new StringDt(famDecrypt));
                    p.getName().get(0).setFamily(famArray);
                    ArrayList<StringDt> givArray = new ArrayList<>();
                    givArray.add(new StringDt(givDecrypt));
                    p.getName().get(0).setGiven(givArray);

                    // Write log
                    System.out.println("Found " + search.getEntry().size() + " results.");
                    String result = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(search);
                    //System.out.println("RESULT: " + result);

                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            // Send response
            PrintWriter out = response.getWriter();
            out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(search));

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // Ko dobimo POST request - nalaganje resourca na bazo
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // Encrypt the resource
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost requestToCrypto = new HttpPost("http://localhost:8080/crypto.do");
        String requestBody = IOUtils.toString(new InputStreamReader(request.getInputStream()));
        requestToCrypto.setEntity(new StringEntity(requestBody));

        // Get the response, generate a usable form out of it
        HttpResponse encryptedResource = httpClient.execute(requestToCrypto);

        // Send the encrypted response to HAPI endpoint
        HttpPost encryptedToHapi = new HttpPost("http://hapi.fhir.org/baseDstu2");
        encryptedToHapi.setEntity(encryptedResource.getEntity());
        encryptedToHapi.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpResponse responseFromHapi = httpClient.execute(encryptedToHapi);
        String responseFromHapiString = EntityUtils.toString(responseFromHapi.getEntity());
        PrintWriter out = response.getWriter();
        out.println(responseFromHapiString);
    }
}