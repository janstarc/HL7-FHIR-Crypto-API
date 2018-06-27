package com.diplomska.todo;

import java.util.ArrayList;
import java.util.List;

public class TodoService {

    // Static list is created --> It is not connected with any object
    // This is a SERVICE, not an OBJECT class
    private static List<Todo> todos = new ArrayList<Todo>();

    // Gets called only once --> When the TodoService class is called for the first time
        // Create some starting objects
    static {
        todos.add(new Todo("First todo", "Study"));
        todos.add(new Todo("Second todo", "Study" ));
        todos.add(new Todo("Third todo", "Study"));
    }

    public List<Todo> retrieveTodos(){
        return todos;
    }

    public void addTodo(Todo todo){
        todos.add(todo);
    }

    public void deleteTodo(Todo todo){
        todos.remove(todo);
    }


}
