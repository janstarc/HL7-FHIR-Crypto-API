package com.diplomska.intercept;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.ListResource;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import com.diplomska.encryptDecrypt.cryptoService;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;

@WebServlet(urlPatterns = "/getResource.do/Patient")
public class getResource extends HttpServlet {

    private cryptoService crypto = new cryptoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String family = request.getParameter("family");
        System.out.println("Test: " + request.getParameter(family));

        ServletContext context = getServletContext();
        try {
            crypto.init(context);
            family = crypto.encrypt(family);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException |
                CertificateException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            System.out.println("Encryption Error");
            e.printStackTrace();
            return;
        }

        System.out.println("Crypto test: " + family);

        FhirContext ctx = FhirContext.forDstu2();
        IGenericClient client = ctx.newRestfulGenericClient("http://hapi.fhir.org/baseDstu2");

        Bundle search = client
		      .search()
		      .forResource(Patient.class)
		      .where(Patient.FAMILY.matches().value(family))
		      .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
              .encodedJson()
		      .execute();



        List<IBaseResource> retVal = new ArrayList<IBaseResource>();
        List<Patient> resultArray = search.getAllPopulatedChildElementsOfType(Patient.class);
        for (Patient p : resultArray) {
            String fam = p.getName().get(0).getFamilyAsSingleString();
            String giv = p.getName().get(0).getGivenAsSingleString();
            try {
                String famDecrypt = crypto.decrypt(fam);
                String givDecrypt = crypto.decrypt(giv);
                System.out.println("Family = " + famDecrypt + " Given = " + givDecrypt);

                ArrayList<StringDt> famArray = new ArrayList<>();
                famArray.add(new StringDt(famDecrypt));
                ArrayList<StringDt> givArray = new ArrayList<>();
                givArray.add(new StringDt(givDecrypt));
                retVal.add(p);

            } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }


        Bundle bun = new Bundle();
        Patient p = null;
        IResource pRes = p;
        Bundle.Entry bunEntry = new Bundle.Entry();
        bunEntry.setResource(pRes);
        bun.addEntry(bunEntry);
        //Bundle.Entry bunEntry = (Bundle.Entry) pRes;
        //bun.addEntry(bunEntry);


        //System.out.println(ctx.newJsonParser().setPrettyPrint(true).encode);


        //System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString((IBaseResource) resultArray));

        //BundleUtil.toListOfResources(f)

        System.out.println("Found " + search.getEntry().size() + " results.");
        String result = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(search);
        System.out.println("Result: " + result);

        // Response
        PrintWriter out = response.getWriter();
        //out.println("Test success " + request.getParameter("family"));
        out.println("Result: " + result);

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
