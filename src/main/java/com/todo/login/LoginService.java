package com.todo.login;

public class LoginService {

    public boolean isUserValid(String un, String pass){
        if(un.equals("Jan") && pass.equals("geslo")) return true;
        else return false;
    }
}
