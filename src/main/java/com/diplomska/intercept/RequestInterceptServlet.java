package com.diplomska.intercept;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.HTTPVerbEnum;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.diplomska.encryptDecrypt.cryptoService;
import org.hl7.fhir.instance.model.api.IBaseResource;
//import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

@WebServlet(urlPatterns = "/addResource.do")
public class RequestInterceptServlet extends HttpServlet {

    private cryptoService crypto = new cryptoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.getRequestDispatcher("/WEB-INF/view/addResource.jsp").forward(request, response);
    }

    // Later, accessing subfields
        // https://stackoverflow.com/questions/10198013/how-do-i-access-a-jsonobject-subfield

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        System.out.println("---Request intercept START---");
        FhirContext ctx = FhirContext.forDstu2();
        IBaseResource resource = ctx.newJsonParser().parseResource(new InputStreamReader(request.getInputStream()));
        Patient p = (Patient) resource;

        String familyName = null;
        String givenName = null;

        try {
            familyName = p.getName().get(0).getFamilyAsSingleString();
            givenName = p.getName().get(0).getGivenAsSingleString();
            System.out.println("Family: " + familyName + " | Given: " + givenName);
        } catch (Exception e){
            System.out.println("JSON Parser Error - Invalid resource");
            e.printStackTrace();
            return;
        }

        ServletContext context = getServletContext();
        try {
            crypto.init(context);
            familyName = crypto.encrypt(familyName);
            givenName = crypto.encrypt(givenName);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException |
                 CertificateException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            System.out.println("Encryption Error");
            e.printStackTrace();
            return;
        }

        System.out.println("Family: " + familyName + " Given: " + givenName);
        ArrayList<StringDt> family = new ArrayList<>();
        family.add(new StringDt(familyName));
        ArrayList<StringDt> given = new ArrayList<>();
        given.add(new StringDt(givenName));

        try{
            p.getName().get(0).setFamily(family);
            p.getName().get(0).setGiven(given);
        } catch (Exception e){
            e.printStackTrace();
        }

        String changedResource = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(p);
        System.out.println(changedResource);
        System.out.println("---Request intercept END---");

        // Send the request to HAPI endpoint
        // Create a bundle that will be used as a transaction
        Bundle bundle = new Bundle();
        bundle.setType(BundleTypeEnum.TRANSACTION);

        // Add the patient as an entry. This entry is a POST with an
        // If-None-Exist header (conditional create) meaning that it
        // will only be created if there isn't already a Patient with
        // the identifier 12345
        bundle.addEntry()
                .setFullUrl(p.getId().getValue())
                .setResource(p)
                .getRequest()
                .setUrl("Patient")
                .setMethod(HTTPVerbEnum.POST);

        // Create a client and post the transaction to the server
        String serverBase = "http://hapi.fhir.org/baseDstu2";
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);
        Bundle resp = client.transaction().withBundle(bundle).execute();

        // Log the response
        System.out.println("------------RESPONSE------------");
        String responseString = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resp);
        System.out.println(responseString);
        //response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.println(responseString);
    }
}