package com.todo.encryptDecrypt;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/encryptDecrypt.do")
public class EncryptDecryptServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Use POST!");
        request.getRequestDispatcher("/WEB-INF/view/encryptDecrypt.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("encryptDecrypt servlet");

        String input = null;
        while((input = request.getReader().readLine()) != null){
            System.out.println("ED: " + input);
        }
    }
}
