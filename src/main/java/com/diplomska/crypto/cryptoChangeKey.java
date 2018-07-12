package com.diplomska.crypto;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.HTTPVerbEnum;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import org.apache.http.HttpStatus;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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

        // Encrypts patient id with the right key
        try {
            crypto.init(getServletContext());
            _idEnc = crypto.encrypt(_id);

            if(_idEnc == null){
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
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
        Bundle toUpload = new Bundle();

        // Get extensions, find encryptedReference extension, update hash
        for (Observation o : observationsList){
            List<ExtensionDt> extList = o.getUndeclaredExtensions();
            extList = encryptResourceWithNewKey(extList, _id, keyAlias);
            if(extList == null){
                response.sendError(HttpStatus.SC_NOT_FOUND);
                return;
            }
            System.out.println("Ext output2 (size): " + extList.size());

            System.out.println("Observation URL: " + HapiRESTfulServer + "/Observation/" + o.getId().getIdPart());
            // Add each resource separately
            toUpload.addEntry().setResource(o).getRequest().setUrl(HapiRESTfulServer + "/Observation/" + o.getId().getIdPart()).setMethod(HTTPVerbEnum.PUT);
        }

        // Get extensions, find encryptedReference extension, update hash
        for (Condition c : conditionsList){
            List<ExtensionDt> extList = c.getUndeclaredExtensions();
            extList = encryptResourceWithNewKey(extList, _id, keyAlias);
            if(extList == null){
                response.sendError(HttpStatus.SC_NOT_FOUND);
                return;
            }
            System.out.println("Ext output (size): " + extList.size());

            System.out.println("Condition URL: " + HapiRESTfulServer + "/Condition/" + c.getId().getIdPart());
            // Add each resource separately
            toUpload.addEntry().setResource(c).getRequest().setUrl(HapiRESTfulServer + "/Condition/" + c.getId().getIdPart()).setMethod(HTTPVerbEnum.PUT);
        }

        searchCondition.setType(BundleTypeEnum.MESSAGE);
        searchObservation.setType(BundleTypeEnum.MESSAGE);

        toUpload.setType(BundleTypeEnum.TRANSACTION);

        String bundleOut = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(toUpload);

        PrintWriter out = response.getWriter();
        out.println(bundleOut);
    }

    // Checks if encryptedReference extension exists, re-encrypts Patient reference with new key
    private List<ExtensionDt> encryptResourceWithNewKey(List<ExtensionDt> extList, String _id, String keyAlias){
        if(extList.size() > 0){
            for(ExtensionDt ext : extList){
                if(ext.getElementSpecificId().equals("encryptedReference")){
                    //System.out.println("Plain: " + _id + "Prev Hash: " + ext.getValue() + " Test: " + _idEnc);
                    try {
                        String newHash = crypto.encryptWithNewKey(_id, keyAlias);
                        if(newHash == null) return null;
                        System.out.println("New hash: " + newHash);
                        ext.setValue(new StringDt("Patient/" + newHash));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        } else {
            return null;
        }
        return extList;
    }
}
