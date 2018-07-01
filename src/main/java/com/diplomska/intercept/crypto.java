package com.diplomska.intercept;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.HTTPVerbEnum;
import ca.uhn.fhir.model.primitive.StringDt;
import com.diplomska.encryptDecrypt.cryptoService;
import org.hl7.fhir.instance.model.api.IBaseResource;

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

@WebServlet(urlPatterns = "/crypto.do")
public class crypto extends HttpServlet {

    private cryptoService crypto = new cryptoService();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


    }

    // Ko dobimo POST request - nalaganje resource na bazo
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        System.out.println("-------- CRYPTO START --------");
        FhirContext ctx = FhirContext.forDstu2();

        // Convert request to resource and cast resource to Patient
        IBaseResource resource = ctx.newJsonParser().parseResource(new InputStreamReader(request.getInputStream()));
        Patient p = (Patient) resource;

        // Get first and last name from the resource
        String familyName;
        String givenName;
        try {
            familyName = p.getName().get(0).getFamilyAsSingleString();
            givenName = p.getName().get(0).getGivenAsSingleString();
        } catch (Exception e){
            System.out.println("JSON Parser Error - Invalid resource");
            e.printStackTrace();
            return;
        }

        // Encrypt first and last name
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

        // Handle data types
        ArrayList<StringDt> family = new ArrayList<>();
        family.add(new StringDt(familyName));
        ArrayList<StringDt> given = new ArrayList<>();
        given.add(new StringDt(givenName));

        // Set encrypted values to resource
        try{
            p.getName().get(0).setFamily(family);
            p.getName().get(0).setGiven(given);
        } catch (Exception e){
            e.printStackTrace();
        }

        // Encode the resource to json, prepare it to be sent
        String encryptedResource = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(p);
        //System.out.println(encryptedResource);

        //PrintWriter out = response.getWriter();
        //out.println(encryptedResource);

        /**
         *  NEW
         */
        // Create a bundle that will be used as a transaction
        Bundle bundle = new Bundle();
        bundle.setType(BundleTypeEnum.TRANSACTION);

        // Add the patient as an entry
        bundle.addEntry()
                .setFullUrl(p.getId().getValue())
                .setResource(p)
                .getRequest()
                .setUrl("Patient")
                .setMethod(HTTPVerbEnum.POST);

        String bundleString = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        System.out.println(bundleString);
        PrintWriter out = response.getWriter();
        out.println(bundleString);
        //System.out.println("-------- CRYPTO END   --------");

    }
}