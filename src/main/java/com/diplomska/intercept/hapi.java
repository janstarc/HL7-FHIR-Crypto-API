package com.diplomska.intercept;

import com.diplomska.encryptDecrypt.cryptoService;
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

@WebServlet(urlPatterns = "/hapi.do")
public class hapi extends HttpServlet {

    private cryptoService crypto = new cryptoService();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


    }

    // Ko dobimo POST request - nalaganje resourca na bazo
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // Encrypt the resource
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost requestToCrypto = new HttpPost("http://localhost:8080/crypto.do");
        String requestBody = IOUtils.toString(new InputStreamReader(request.getInputStream()));
        requestToCrypto.setEntity(new StringEntity(requestBody));

        // Get the response, generate a usable form out of it
        HttpResponse encryptedResource = httpClient.execute(requestToCrypto);

        // Send the encrypted response to HAPI endpoint
        HttpPost encryptedToHapi = new HttpPost("http://hapi.fhir.org/baseDstu2");
        encryptedToHapi.setEntity(encryptedResource.getEntity());
        encryptedToHapi.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpResponse responseFromHapi = httpClient.execute(encryptedToHapi);
        String responseFromHapiString = EntityUtils.toString(responseFromHapi.getEntity());
        PrintWriter out = response.getWriter();
        out.println(responseFromHapiString);
    }
}