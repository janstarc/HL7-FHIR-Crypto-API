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

        //BufferedReader krneki = request.getReader();
        //String test = krneki.readLine();
        System.out.println("Test: " + request.getParameter("family"));

        PrintWriter out = response.getWriter();
        out.println("Test success " + request.getParameter("family"));
        /*
        out.println("{\n" +
                "    \"resourceType\": \"Bundle\",\n" +
                "    \"id\": \"cc083b96-67fc-444b-83a4-6a007865fdcd\",\n" +
                "    \"meta\": {\n" +
                "        \"lastUpdated\": \"2018-06-27T23:35:26.803+00:00\"\n" +
                "    },\n" +
                "    \"type\": \"searchset\",\n" +
                "    \"total\": 0,\n" +
                "    \"link\": [\n" +
                "        {\n" +
                "            \"relation\": \"self\",\n" +
                "            \"url\": \"http://hapi.fhir.org/baseDstu2/Patient?family=Perc\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");

         */
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
