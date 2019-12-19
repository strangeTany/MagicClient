package com.example.myapplication;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

class UserCard extends SugarRecord {
    @Unique
    private String CardId;

    private String Name;
    private Integer Color;
    private Boolean Status;

    UserCard(String cardId, String name, Integer color, Boolean status) {
        CardId = cardId;
        Name = name;
        Color = color;
        Status = status;
    }

    String getCardId() {
        return CardId;
    }

    String getName() {
        return Name;
    }

    Integer getColor() {
        return Color;
    }

    Boolean getStatus() {
        return Status;
    }
}
