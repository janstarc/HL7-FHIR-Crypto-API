package com.diplomska.todo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/list-todo.do")
public class ListTodoServlet extends HttpServlet {

    private TodoService todoService = new TodoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("todos", todoService.retrieveTodos());
        request.getRequestDispatcher("/WEB-INF/view/list-todos.jsp").forward(request, response);
    }
}
