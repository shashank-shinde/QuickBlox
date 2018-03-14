package com.sas_apps.quickblox.qb.common.holder;

/*
 * Created by Shashank Shinde.
 */

import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QBChatMsgHolder {

    private static QBChatMsgHolder instance;
    private HashMap<String ,ArrayList<QBChatMessage>> qbChatMessageArray;

    public static synchronized QBChatMsgHolder getInstance(){
        QBChatMsgHolder qbChatMsgHolder;
        synchronized (QBChatMsgHolder.class){
            if(instance==null){
                instance=new QBChatMsgHolder();
            }
            qbChatMsgHolder=instance;
        }
        return qbChatMsgHolder;
    }

    private QBChatMsgHolder(){
        this.qbChatMessageArray=new HashMap<>();
    }

    public void putMessages(String dialogId,ArrayList<QBChatMessage> qbChatMessages){
        this.qbChatMessageArray.put(dialogId,qbChatMessages);
    }

    public void putMessage(String dialogId,QBChatMessage qbChatMessage){
        List<QBChatMessage> listResult=this.qbChatMessageArray.get(dialogId);
        listResult.add(qbChatMessage);
        ArrayList<QBChatMessage> listAdded=new ArrayList<>(listResult.size());
        listAdded.addAll(listResult);
        putMessages(dialogId,listAdded);
    }
    public ArrayList<QBChatMessage> getChatMessageByDialogId(String dialogId){
        return (ArrayList<QBChatMessage>) this.qbChatMessageArray.get(dialogId);
    }

}
