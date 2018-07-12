package com.diplomska.hapi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
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
import java.sql.SQLException;
import java.util.Set;

import static com.diplomska.constants.address.HapiRESTfulServer;
import static com.diplomska.crypto.cryptoDB.updateKeyAlias;

@WebServlet(urlPatterns = {"/hapi.do/Patient"})
public class hapiPatient extends HttpServlet {

    // Get requesti - iskanje pacientov
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Search with encoded parameters
        FhirContext ctx = FhirContext.forDstu2();
        IGenericClient client = ctx.newRestfulGenericClient(HapiRESTfulServer);
        Set<String> parameterNames = request.getParameterMap().keySet();
        Bundle search = null;

        if(parameterNames.contains("_id")){

            // Get the parameter
            String _id = request.getParameter("_id");

            // Search for the Patient - hashed value
            search = client
                    .search()
                    .byUrl(HapiRESTfulServer + "/Patient?_id=" + _id)
                    .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                    .encodedJson()
                    .execute();

        } else if (parameterNames.contains("family") && parameterNames.contains("given")) {

            // Get the parameter
            String family = request.getParameter("family");
            String given = request.getParameter("given");

            // Search for the Patient - hashed value
            search = client
                    .search()
                    .forResource(Patient.class)
                    .where(Patient.FAMILY.matches().value(family))
                    .and(Patient.GIVEN.matches().value(given))
                    .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                    .encodedJson()
                    .execute();
        }

        // Send response
        PrintWriter out = response.getWriter();
        out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(search));
    }

    // Ko dobimo POST request - nalaganje resourca na bazo
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost requestToHapi = new HttpPost(HapiRESTfulServer);
        String requestBody = IOUtils.toString(new InputStreamReader(request.getInputStream()));
        requestToHapi.setEntity(new StringEntity(requestBody));
        requestToHapi.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpResponse responseFromHapi = httpClient.execute(requestToHapi);
        int statusCode = responseFromHapi.getStatusLine().getStatusCode();

        if(statusCode != 200){
            System.out.println("Status code: " + statusCode);
            response.sendError(statusCode);
            return;
        }

        FhirContext ctx = FhirContext.forDstu2();

        String responseFromHapiString = EntityUtils.toString(responseFromHapi.getEntity());
        Bundle resp = (Bundle) ctx.newJsonParser().parseResource(responseFromHapiString);
        Bundle.Entry entry = resp.getEntryFirstRep();
        String[] entryString = entry.getResponse().getLocation().split("/");
        String idPart = entryString[1];
        System.out.println("-----> User ID: " + idPart);
        boolean err = false;

        try {
            updateKeyAlias(idPart, "key1");
        } catch (SQLException e) {
            e.printStackTrace();
            err = true;
        }

        PrintWriter out = response.getWriter();
        out.println(responseFromHapiString);
        if(err) out.println("DB Error - Keys not added!");
    }
}