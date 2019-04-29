package com.example.user.library;

import android.support.v4.util.ArrayMap;

public class Constants {
    public static final String USER_ID = "user_id";
    public static ArrayMap<Integer, String> stasusDictionary = new ArrayMap<Integer, String>() {
        {
            put(0, "заявка отправлена");
            put(1, "заявка обрабатывается");
            put(2, "заявка отклонена");
            put(3, "заявка одобрена");
            put(4, "книга на руках");
            put(5, "проверка возврата");
            put(6, "заявка закрыта");
            put(7, "заявка расформирована");
        }
        ;
    };
}
