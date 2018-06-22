package com.todo.intercept;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.deploy.net.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import sun.net.www.http.HttpClient;

import javax.mail.internet.ContentType;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

@WebServlet(urlPatterns = "/request-intercept.do")
public class RequestInterceptServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/view/request-intercept.jsp").forward(request, response);
        //response.sendRedirect("/encryptdecrypt.do");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Here 2");
        request.getContentType();


        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();

        JsonElement je = jp.parse(new InputStreamReader(request.getInputStream()));
        String prettyJsonString = gson.toJson(je);
        //System.out.println("Content = " + prettyJsonString);


        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(prettyJsonString);
        response.getWriter().flush();

        //System.out.println("End of doPost in RequestInterceptor");

        response.sendRedirect("/encryptdecrypt.do");
    }

}
