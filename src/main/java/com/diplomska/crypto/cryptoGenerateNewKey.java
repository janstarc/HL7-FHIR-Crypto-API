package com.diplomska.crypto;

import com.diplomska.crypto.cryptoService;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/crypto.do/GenerateNewKey"})
public class cryptoGenerateNewKey extends HttpServlet {

    private cryptoService crypto = new cryptoService();

    // Get requesti - iskanje pacientov
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext context = getServletContext();
        String keyAlias = request.getParameter("keyAlias");
        try{
            crypto.init(context);
            String out = crypto.addNewKeyToKeyStore(keyAlias);
            System.out.println("KeyAlias: " + out);
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
