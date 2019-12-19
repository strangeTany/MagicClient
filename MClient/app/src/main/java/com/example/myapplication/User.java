package com.example.myapplication;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

class User extends SugarRecord {
    @Unique
    private String Token;

    private String UserName;

    User(String token, String userName, String email, Integer wins, Integer losses) {
        this.Token = token;
        this.UserName = userName;
    }

    String getToken() {
        return Token;
    }

    String getUserName() {
        return UserName;
    }
}
