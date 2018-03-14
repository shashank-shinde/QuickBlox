package com.sas_apps.quickblox.qb.common.holder;
/*
 * Created by Shashank Shinde.
 */

import android.os.Bundle;

public class QBUnreadCountHolder {
    private static QBUnreadCountHolder instance;
    private Bundle bundle;

    public static synchronized QBUnreadCountHolder getInstance() {
        QBUnreadCountHolder qbUnreadCountHolder;
        synchronized (QBUnreadCountHolder.class) {
            if (instance == null) {
                instance = new QBUnreadCountHolder();
            }
            qbUnreadCountHolder = instance;
        }
        return qbUnreadCountHolder;
    }

    private QBUnreadCountHolder() {
        bundle = new Bundle();
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public int getUnreadCountByDialogId(String id) {
        return this.bundle.getInt(id);
    }
}
