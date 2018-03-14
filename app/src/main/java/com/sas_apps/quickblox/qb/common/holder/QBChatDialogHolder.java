package com.sas_apps.quickblox.qb.common.holder;
/*
 * Created by Shashank Shinde.
 */

import com.quickblox.chat.model.QBChatDialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QBChatDialogHolder {
    public static QBChatDialogHolder instance;
    private HashMap<String, QBChatDialog> qbChatDialogHashMap;

    public static synchronized QBChatDialogHolder getInstance() {
        QBChatDialogHolder qbChatDialogHolder;
        synchronized (QBChatDialogHolder.class) {
            if (instance == null) {
                instance = new QBChatDialogHolder();
            }
        }
        qbChatDialogHolder = instance;
        return qbChatDialogHolder;
    }

    public QBChatDialogHolder() {
        this.qbChatDialogHashMap = new HashMap<>();
    }

    public void putDialogs(List<QBChatDialog> dialogs) {
        for (QBChatDialog qbChatDialog : dialogs) {
            putDialog(qbChatDialog);
        }
    }

    public void putDialog(QBChatDialog qbChatDialog) {
        this.qbChatDialogHashMap.put(qbChatDialog.getDialogId(), qbChatDialog);
    }

    public QBChatDialog getDialogById(String dialogId) {
        return (QBChatDialog) qbChatDialogHashMap.get(dialogId);
    }

    public List<QBChatDialog> getChatDialogsById(List <String> dialogIds){
        List<QBChatDialog> chatDialogs=new ArrayList<>();
        for (String id:dialogIds){
            QBChatDialog chatDialog=getDialogById(id);
            if(chatDialog!=null){
                chatDialogs.add(chatDialog);
            }
        }
        return chatDialogs;
    }

    public ArrayList<QBChatDialog> getAllChatDialogs(){
        ArrayList<QBChatDialog> dialogList=new ArrayList<>();
        for(String key:qbChatDialogHashMap.keySet()){
            dialogList.add(qbChatDialogHashMap.get(key));
        }
        return dialogList;
    }

    public void removeDialog(String dialogId) {
        qbChatDialogHashMap.remove(dialogId);
    }
}
