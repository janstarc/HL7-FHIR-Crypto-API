package com.diplomska.intercept;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.diplomska.encryptDecrypt.cryptoService;
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
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = "/getResource.do/Patient")
public class getResource extends HttpServlet {

    private cryptoService crypto = new cryptoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Get the parameter
        String family = request.getParameter("family");
        String given = request.getParameter("given");
        ServletContext context = getServletContext();

        // Encrypt the parameter to the hashed value
        try {
            crypto.init(context);
            given = crypto.encrypt(given);
            family = crypto.encrypt(family);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException |
                CertificateException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            System.out.println("Encryption Error");
            e.printStackTrace();
            return;
        }

        FhirContext ctx = FhirContext.forDstu2();
        IGenericClient client = ctx.newRestfulGenericClient("http://hapi.fhir.org/baseDstu2");

        // Search for the Patient - hashed value
        Bundle search = client
		      .search()
		      .forResource(Patient.class)
		      .where(Patient.FAMILY.matches().value(family))
              .and(Patient.GIVEN.matches().value(given))
		      .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
              .encodedJson()
		      .execute();

        // Convert bundle to List<Patient> - prep to edit the values
        List<Patient> resultArray = search.getAllPopulatedChildElementsOfType(Patient.class);

        for (Patient p : resultArray) {
            String fam = p.getName().get(0).getFamilyAsSingleString();
            String giv = p.getName().get(0).getGivenAsSingleString();

            try {
                // Decrypt the values
                String famDecrypt = crypto.decrypt(fam);
                String givDecrypt = crypto.decrypt(giv);

                // Handle the resource conversion and change the value of object p
                ArrayList<StringDt> famArray = new ArrayList<>();
                famArray.add(new StringDt(famDecrypt));
                p.getName().get(0).setFamily(famArray);
                ArrayList<StringDt> givArray = new ArrayList<>();
                givArray.add(new StringDt(givDecrypt));
                p.getName().get(0).setGiven(givArray);

            } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }

        // Write log
        System.out.println("Found " + search.getEntry().size() + " results.");
        String result = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(search);
        System.out.println("RESULT: " + result);

        // Send response
        PrintWriter out = response.getWriter();
        out.println(result);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        out.println("Use GET");
    }
}
