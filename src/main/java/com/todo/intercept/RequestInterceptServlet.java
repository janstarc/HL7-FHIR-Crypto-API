package com.todo.intercept;

import com.google.gson.*;
import com.todo.encryptDecrypt.cryptoService;
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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

@WebServlet(urlPatterns = "/request-intercept.do")
public class RequestInterceptServlet extends HttpServlet {

    private cryptoService crypto = new cryptoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/view/request-intercept.jsp").forward(request, response);
        //response.sendRedirect("/encryptdecrypt.do");
    }

    // Later, accessing subfields
        // https://stackoverflow.com/questions/10198013/how-do-i-access-a-jsonobject-subfield

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        System.out.println("---Request intercept START---");

        // Parse json object ot json form
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement jElement = jp.parse(new InputStreamReader(request.getInputStream()));
        String jsonString = jElement.toString();
        JsonObject jObject = gson.fromJson(jsonString, JsonObject.class);

        System.out.println("Json Element = " + jsonString);
        String id = jObject.getAsJsonPrimitive("id").toString();
        String decryptedId = null;
        ServletContext context = getServletContext();
        try {
            crypto.init(context);
            id = crypto.encrypt(id);
            decryptedId = crypto.decrypt(id);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException |
                 CertificateException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
        }

        jObject.addProperty("id", id);
        jObject.addProperty("decId", decryptedId);
        System.out.println("JObject = " + jObject.toString());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // create HTML response
        PrintWriter writer = response.getWriter();
        writer.append(jObject.toString());



        System.out.println("---Request intercept END---");
    }

}
