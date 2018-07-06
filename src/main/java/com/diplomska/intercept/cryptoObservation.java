package com.diplomska.intercept;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.HTTPVerbEnum;
import ca.uhn.fhir.model.primitive.StringDt;
import com.diplomska.encryptDecrypt.cryptoService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
import java.util.List;

@WebServlet(urlPatterns = "/crypto.do/Observation")
public class cryptoObservation extends HttpServlet {

    private cryptoService crypto = new cryptoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String encrypt = request.getParameter("encrypt");

        if(encrypt.equals("true")){
            System.out.println("Here 1");
            String _id = request.getParameter("_id");

            ServletContext context = getServletContext();

            try {
                crypto.init(context);
                // TEST
                System.out.println("1 = " + crypto.encrypt("1") + " Dec= " + crypto.decrypt(crypto.encrypt("1")));
                System.out.println("14954 = " + crypto.encrypt("14954") + " Dec= " + crypto.decrypt(crypto.encrypt("14954")));
                System.out.println("14962 = " + crypto.encrypt("14962") + " Dec= " + crypto.decrypt(crypto.encrypt("14962")));
                // TEST
                _id =  crypto.encrypt(_id);
            } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | CertificateException | NoSuchAlgorithmException | NoSuchPaddingException | UnrecoverableEntryException | KeyStoreException e) {
                e.printStackTrace();
            }

            JsonObject jObj = new JsonObject();
            jObj.addProperty("_id", _id);
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

            PrintWriter out = response.getWriter();
            out.println(gson.toJson(jObj));

        } else if (encrypt.equals("false")) {

            System.out.println("Here 2");
            String _id = request.getParameter("_id");
            ServletContext context = getServletContext();

            try {
                crypto.init(context);
                _id =  crypto.decrypt(_id);
            } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | CertificateException | NoSuchAlgorithmException | NoSuchPaddingException | UnrecoverableEntryException | KeyStoreException e) {
                e.printStackTrace();
            }

            JsonObject jObj = new JsonObject();
            jObj.addProperty("_id", _id);
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

            PrintWriter out = response.getWriter();
            out.println(gson.toJson(jObj));
        }
    }

    // Ko dobimo POST request - nalaganje resource na bazo
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        System.out.println("-------- CRYPTO START --------");
        FhirContext ctx = FhirContext.forDstu2();

        // Convert request to resource and cast resource to Patient
        Bundle req = (Bundle) ctx.newJsonParser().parseResource(new InputStreamReader(request.getInputStream()));
        List<Observation> observationList = req.getAllPopulatedChildElementsOfType(Observation.class);

        String aaaa = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(req);
        System.out.println("\n RESOURCE BEFORE ENCRYPTION: " + aaaa + "\n ----- END -----");
        // Create ServletContext and init ED Servuce
        ServletContext context = getServletContext();
        try {
            crypto.init(context);
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | UnrecoverableEntryException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        System.out.println("observationList.len = " + observationList.size());
        // Encrypt all resources
        for(Observation o : observationList){
            /**
             *  TODO: FIX HERE!!!
             *      --> Ker je referenca na drugem strezniku, vrne HAPI celo referenco. Narobe se parsa, referenca je null
             */
            String _id = String.valueOf(o.getId().getIdPartAsLong());
            System.out.println("Value of ID (crypto): " + _id + " As string: " + o.getId().getIdPart() + " As long: " + o.getId().getIdPartAsLong());
            try {
                _id = crypto.encrypt(_id);
            } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
            o.setSubject(new ResourceReferenceDt("Patient/" + _id));
        }

        // Create a bundle that will be used in a transaction
        //Bundle bundle = new Bundle();
        //req.setType(BundleTypeEnum.TRANSACTION);

        // Add the patient as an entry
        /*bundle.addEntry()
                .setResource(req)
                .getRequest()
                .setUrl("Observation")
                .setMethod(HTTPVerbEnum.POST);*/

        // Encode bundle to json and send it to the HAPI server
        String bundleString = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(req);
        System.out.println(bundleString);
        PrintWriter out = response.getWriter();
        out.println(bundleString);
    }
}