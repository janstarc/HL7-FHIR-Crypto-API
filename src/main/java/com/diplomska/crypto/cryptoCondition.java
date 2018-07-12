package com.diplomska.crypto;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.StringDt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

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
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = "/crypto.do/Condition")
public class cryptoCondition extends HttpServlet {

    private cryptoService crypto = new cryptoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String encrypt = request.getParameter("encrypt");

        // Encryption of IDs
        if(encrypt.equals("true")){

            // Get the plaintext ID from GET request
            String _id = request.getParameter("_id");
            ServletContext context = getServletContext();

            // Encrypt the ID
            try {
                crypto.init(context);
                _id =  crypto.encrypt(_id);
            } catch (CertificateException | NoSuchAlgorithmException | NoSuchPaddingException | UnrecoverableEntryException | KeyStoreException | SQLException e) {
                e.printStackTrace();
            }

            // Put encrypted value to JSON object
            JsonObject jObj = new JsonObject();
            jObj.addProperty("_id", _id);
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

            // Send it back to hapi.do
            PrintWriter out = response.getWriter();
            out.println(gson.toJson(jObj));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        FhirContext ctx = FhirContext.forDstu2();

        // Create observationList out of request
        Bundle req = (Bundle) ctx.newJsonParser().parseResource(new InputStreamReader(request.getInputStream()));
        List<Condition> conditionList = req.getAllPopulatedChildElementsOfType(Condition.class);

        // Create ServletContext and init ED Service
        ServletContext context = getServletContext();
        try {
            crypto.init(context);
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | UnrecoverableEntryException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        for(Condition c : conditionList){

            // Get list of extensions for every Observation object, find the encryptedReference extension
            List<ExtensionDt> extList = c.getUndeclaredExtensions();
            if(extList.size() > 0){
                for(ExtensionDt ext : extList){
                    if(ext.getElementSpecificId().equals("encryptedReference")){        // Find encrypted reference extension

                        String id = ext.getValue().toString();
                        String idPart = id.substring(id.lastIndexOf("/") + 1);      // Get idPart (5 instead of Patient/5)

                        // Encrypt the reference
                        try{
                            idPart = crypto.encrypt(idPart);
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                        // Set encrypted ID
                        ext.setValue(new StringDt("Patient/" + idPart));
                    }
                }
            }
        }

        // Encode bundle to json and send it to the HAPI server
        String bundleString = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(req);
        PrintWriter out = response.getWriter();
        out.println(bundleString);
    }
}