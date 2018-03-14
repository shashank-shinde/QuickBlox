package com.sas_apps.quickblox.qb.common;
/*
 * Created by Shashank Shinde.
 */

import com.quickblox.users.model.QBUser;
import com.sas_apps.quickblox.qb.common.holder.QBUsersHolder;

import java.util.List;

public class Common {

    public static String createChatDialogName(List<Integer> qbUsers){

        List<QBUser> qbUsers1= QBUsersHolder.getInstance().getUserById(qbUsers);
        StringBuilder name=new StringBuilder();
        for (QBUser user:qbUsers1){
            name.append(user.getFullName()).append(" ");
        }
        if (name.length()>30){
            name=name.replace(30,name.length()-1,"...");
        }
        return name.toString();
    }
}


