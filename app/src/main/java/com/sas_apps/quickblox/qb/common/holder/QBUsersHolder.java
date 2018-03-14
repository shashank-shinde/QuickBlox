package com.sas_apps.quickblox.qb.common.holder;
/*
 * Created by Shashank Shinde.
 */

import android.util.SparseArray;
import com.quickblox.users.model.QBUser;
import java.util.ArrayList;
import java.util.List;

public class QBUsersHolder {
    public static QBUsersHolder instance;
    public SparseArray<QBUser> qbUserSparseArray;

    public static synchronized QBUsersHolder getInstance() {
        if ((instance == null)) {
            instance = new QBUsersHolder();
        }
        return instance;
    }

    public QBUsersHolder() {
        qbUserSparseArray = new SparseArray<>();
    }

    public void putUsers(List<QBUser> users) {
        for (QBUser user : users) {
            putUser(user);
        }
    }

    public void putUser(QBUser user) {
        qbUserSparseArray.put(user.getId(), user);
    }

    public QBUser getUserById(int id) {
        return qbUserSparseArray.get(id);
    }

    public List<QBUser> getUserById(List<Integer> ids) {
        List<QBUser> qbUser = new ArrayList<>();
        for (Integer id : ids) {
            QBUser user = getUserById(id);
            if (user != null) {
                qbUser.add(user);
            }
        }
        return qbUser;
    }


}
