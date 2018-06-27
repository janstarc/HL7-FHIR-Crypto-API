package com.diplomska.login;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/login.do")
public class LoginServlet extends HttpServlet {

    private LoginService uvService = new LoginService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/view/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws  ServletException, IOException {

        String name = request.getParameter("name");
        String pass = request.getParameter("password");

        if(uvService.isUserValid(name, pass)){
            request.getSession().setAttribute("name", name);
            response.sendRedirect("/list-todo.do");

        } else {
            request.setAttribute("success", "Your un and pass don't match");
            request.getRequestDispatcher("/WEB-INF/view/login.jsp").forward(request, response);
        }



    }


}
