package com.diplomska.todo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/add-todo.do")
public class AddTodoServlet extends HttpServlet {

    private TodoService todoService = new TodoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("WEB-INF/view/add-todo.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String newTodo = request.getParameter("newTodo");
        String category = request.getParameter("category");
        todoService.addTodo(new Todo(newTodo, category));
        response.sendRedirect("/list-todo.do");
    }
}
