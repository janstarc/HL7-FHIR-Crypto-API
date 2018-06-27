package com.diplomska.intercept;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

@WebServlet(urlPatterns = "/getResource.do")
public class getResource extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("---Get resource START---");
        FhirContext ctx = FhirContext.forDstu2();
        IBaseResource resource = ctx.newJsonParser().parseResource(new InputStreamReader(request.getInputStream()));
        Bundle b = (Bundle) resource;


        System.out.println(new InputStreamReader(request.getInputStream()));

    }
}
