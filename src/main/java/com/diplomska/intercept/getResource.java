package com.diplomska.intercept;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.HTTPVerbEnum;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
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
import java.io.*;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

@WebServlet(urlPatterns = "/getResource.do/Patient")
public class getResource extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String family = request.getParameter("family");
        System.out.println("Test: " + request.getParameter(family));


        FhirContext ctx = FhirContext.forDstu2();
        IGenericClient client = ctx.newRestfulGenericClient("http://hapi.fhir.org/baseDstu2");

        Bundle search = client
		      .search()
		      .forResource(Patient.class)
		      .where(Patient.FAMILY.matches().value(family))
		      .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .encodedJson()
		      .execute();

        System.out.println("Found " + search.getEntry().size() + " results.");
        String result = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(search);
        System.out.println("Result: " + result);

        // Response
        PrintWriter out = response.getWriter();
        out.println("Test success " + request.getParameter("family"));

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("---Request intercept START---");
        FhirContext ctx = FhirContext.forDstu2();
        //IBaseResource resource = ctx.newJsonParser().parseResource(new InputStreamReader(request.getInputStream()));
        //Patient p = (Patient) resource;

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost("http://hapi.fhir.org/baseDstu2");
        //StringEntity params = new StringEntity(new InputStreamReader(request.getInputStream()))
        BufferedReader krneki = request.getReader();
        String test = krneki.toString();
        System.out.println("Test" + test);





    }
}
