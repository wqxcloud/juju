package com.juju.app.biz.impl;

import android.content.Context;
import android.util.Log;

import com.juju.app.biz.DaoSupport;
import com.juju.app.biz.MessageDao;
import com.juju.app.entity.base.MessageEntity;
import com.juju.app.entity.chat.AudioMessage;
import com.juju.app.entity.chat.ImageMessage;
import com.juju.app.entity.chat.SessionEntity;
import com.juju.app.entity.chat.SmallMediaMessage;
import com.juju.app.entity.chat.TextMessage;
import com.juju.app.golobal.DBConstant;
import com.juju.app.golobal.MessageConstant;
import com.juju.app.utils.StringUtils;


import org.xutils.db.Selector;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 项目名称：juju
 * 类描述：文本聊天记录DAO业务类
 * 创建人：gm
 * 日期：2016/4/14 14:31
 * 版本：V1.0.0
 */
public class MessageDaoImpl extends DaoSupport<MessageEntity, Long> implements MessageDao {

    private final String TAG = getClass().getSimpleName();


    public MessageDaoImpl(Context context) {
        super(context);
    }

    @Override
    public void replaceInto(MessageEntity entity) {
        if(entity instanceof AudioMessage) {
            MessageEntity messageEntity = entity.clone();
            super.replaceInto(messageEntity);
        } else if (entity instanceof ImageMessage) {
            MessageEntity messageEntity = entity.clone();
            super.replaceInto(messageEntity);
        } else {
            super.replaceInto(entity);
        }
    }
//
//    @Override
//    public void save(MessageEntity entity) {
//        super.save(entity);
//    }
//
//    @Override
//    public void update(MessageEntity entity) {
//        super.update(entity);
//    }
//
//    @Override
//    public void saveOrUpdate(MessageEntity entity) {
//        super.saveOrUpdate(entity);
//    }

    //    @Override
//    protected void execAfterTableCreated() throws DbException{
//        super.execAfterTableCreated();
//        //创建索引
//        db.execNonQuery("CREATE INDEX index_message_created ON message(created)");
//        db.execNonQuery("CREATE UNIQUE INDEX index_message_session_key_msg_id " +
//                "on message(session_key, msg_id)");
//    }

    /**
     *
     * 查询所有文本聊天记录 （重写）
     *
     * @return
     */
    public List<MessageEntity> findAll() {
        List<MessageEntity> messageList = null;
        try {
            messageList = db.selector(MessageEntity.class)
                    .orderBy("created desc, msg_id", true).findAll();
        } catch (DbException e) {
            Log.e(TAG, "MessageDaoImpl#findAll error:", e);
        }
        return messageList;
    }


    /**
     *******************************************扩展接口***************************************
     */

    /**
     * 查询历史消息
     *
     * @param sessionKey
     * @param lastMsgId
     * @param lastCreateTime
     * @param count
     * @return
     */
    public List<MessageEntity> findHistoryMsgs(String sessionKey, int lastMsgId,
                                               long lastCreateTime, int count) {
        /**解决消息重复的问题*/
        int preMsgId = lastMsgId +1;
        List<MessageEntity> messageList = null;
        try {
            messageList = db.selector(MessageEntity.class)
                    .where("created", "<=", lastCreateTime)
                    .and("session_key", "=", sessionKey)
                    .and((WhereBuilder.b("msg_id", ">", 90000000).or("msg_id", " <= ", lastMsgId)))
                    .and("msg_id", "!=", preMsgId)
                    .orderBy("created", true)
                    .orderBy("msg_id", true)
                    .limit(count).findAll();
        } catch (DbException e) {
            Log.e(TAG, "MessageDaoImpl#findHistoryMsgs error:", e);
        }
        if(messageList != null && messageList.size() >0) {
            return formatMessage(messageList);
        } else {
            messageList = new ArrayList<MessageEntity>();
        }
        return messageList;
    }



    private MessageEntity formatMessage(MessageEntity msg){
        MessageEntity messageEntity = null;
        int displayType = msg.getDisplayType();
        switch (displayType){
//            case DBConstant.SHOW_MIX_TEXT:
//                try {
//                    messageEntity =  MixMessage.parseFromDB(msg);
//                } catch (JSONException e) {
//                    logger.e(e.toString());
//                }
//                break;
//            case DBConstant.SHOW_AUDIO_TYPE:
//                messageEntity = AudioMessage.parseFromDB(msg);
//                break;
//            case DBConstant.SHOW_IMAGE_TYPE:
//                messageEntity = ImageMessage.parseFromDB(msg);
//                break;
            case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                messageEntity = TextMessage.parseFromDB(msg);
                break;
        }
        return messageEntity;
    }

    public List<MessageEntity> formatMessage(List<MessageEntity> msgList){
        if(msgList.size() <= 0){
            return Collections.emptyList();
        }
        ArrayList<MessageEntity> newList = new ArrayList<>();
        for(MessageEntity info:msgList){
            int displayType = info.getDisplayType();
            switch (displayType){
//                case DBConstant.SHOW_MIX_TEXT:
//                    try {
//                        newList.add(MixMessage.parseFromDB(info));
//                    } catch (JSONException e) {
//                        logger.e(e.toString());
//                    }
//                    break;
                case DBConstant.SHOW_AUDIO_TYPE:
                    if(StringUtils.isNotBlank(info.getContent())) {
                        newList.add(AudioMessage.parseFromDB(info));
                    }
                    break;
                case DBConstant.SHOW_IMAGE_TYPE:
                    newList.add(ImageMessage.parseFromDB(info));
                    break;
                case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                    newList.add(TextMessage.parseFromDB(info));
                    break;
                case DBConstant.SHOW_NOTIFY_TYPE:
                    newList.add(TextMessage.parseFromDB(info));
                    break;
                case DBConstant.SHOW_SMALL_MEDIA_TYPE:
                    newList.add(SmallMediaMessage.parseFromDB(info));
                    break;
            }
        }


        return newList;
    }

    @Override
    public long getSessionLastTime(String sessionKey) {
        long timeLine = 0l;
        String successType = String.valueOf(MessageConstant.MSG_SUCCESS);
        Selector selector = null;
        try {
            selector = db.selector(MessageEntity.class);
            selector.where("status", "=", successType)
                    .and("session_key", "=", sessionKey)
                    .orderBy("created", true).offset(0).limit(1);
            List<MessageEntity> list = findAll(selector);
            if(list != null) {
                for(MessageEntity entity : list) {
                    timeLine = entity.getCreated();
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        return timeLine;
    }

//    @Override
//    public void saveOrUpdate(MessageEntity entity) {
//        try {
//            if(entity instanceof TextMessage) {
//                MessageEntity cloneMessage = entity.clone();
//                db.saveOrUpdate(cloneMessage);
//            } else {
//                db.saveOrUpdate(entity);
//            }
//        } catch (DbException e) {
//            Log.e(TAG, "execute saveOrUpdate error:"+clazz.getSimpleName(), e);
//        }
//    }

}
