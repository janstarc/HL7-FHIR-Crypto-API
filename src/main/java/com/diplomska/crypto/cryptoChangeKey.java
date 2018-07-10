package com.diplomska.crypto;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.List;

import static com.diplomska.constants.address.HapiRESTfulServer;

@WebServlet(urlPatterns = "/crypto.do/ChangeKey")
public class cryptoChangeKey extends HttpServlet {

    private cryptoService crypto = new cryptoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String _id = request.getParameter("_id");
        String keyAlias = request.getParameter("keyAlias");
        String _idEnc = null;


        try {
            crypto.init(getServletContext());
            _idEnc = crypto.encrypt(_id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create FHIR context
        FhirContext ctx = FhirContext.forDstu2();
        IGenericClient client = ctx.newRestfulGenericClient(HapiRESTfulServer);

        // Search with encoded parameters
        Bundle searchObservation = client
                .search()
                .forResource(Observation.class)
                .where(new StringClientParam("_content").matches().value("Patient/" + _idEnc))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .encodedJson()
                .execute();

        Bundle searchCondition = client
                .search()
                .forResource(Condition.class)
                .where(new StringClientParam("_content").matches().value("Patient/" + _idEnc))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .encodedJson()
                .execute();

        // Convert bundle to List<Observation>
        List<Observation> observationsList = searchObservation.getAllPopulatedChildElementsOfType(Observation.class);
        List<Condition> conditionsList = searchCondition.getAllPopulatedChildElementsOfType(Condition.class);
        System.out.println("ObservationSize: " + observationsList.size() + " | ConditionsSize: " + conditionsList.size());

        for (Observation o : observationsList){
            List<ExtensionDt> extList = o.getUndeclaredExtensions();
            if(extList.size() > 0){
                for(ExtensionDt ext : extList){
                    if(ext.getElementSpecificId().equals("encryptedReference")){
                        System.out.println("Plain: " + _id + "Prev Hash: " + ext.getValue() + " Test: " + _idEnc);
                        try {
                            String newHash = crypto.encryptWithNewKey(_id, keyAlias);
                            System.out.println("New hash: " + newHash);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        for (Condition c : conditionsList){
            List<ExtensionDt> extList = c.getUndeclaredExtensions();
            if(extList.size() > 0){
                for(ExtensionDt ext : extList){
                    if(ext.getElementSpecificId().equals("encryptedReference")){
                        System.out.println(ext.getValue());
                        // TODO Reencrypt here
                    }
                }
            }
        }

        //PrintWriter out = response.getWriter();
        //out.println(ct)
    }
}
