package com.example.user.library;

import android.support.v4.util.ArrayMap;

public class Constants {
    final static  int PAGE_COUNT = 3;
   // public static String tabTitles[] = new String[] { "Новые", "Одобрены", "Возврат","У читателя" };
    public static String tabTitles[] = new String[] { "Новые", "Возврат","У читателя" };
    public static final String ADMIN_ID = "admin_id";
    public static final String USER_ID = "user_id";
    public static ArrayMap<Integer, String> stasusDictionary = new ArrayMap<Integer, String>() {
        {
            put(0, "заявка отправлена");
            put(1, "заявка отменена");
            put(2, "заявка обрабатывается");
            put(3, "заявка отклонена");
            put(4, "заявка одобрена");
            put(5, "книга на руках");
            put(6, "проверка возврата");
            put(7, "заявка закрыта");
            put(8, "заявка расформирована");
        }
        ;
    };


}
