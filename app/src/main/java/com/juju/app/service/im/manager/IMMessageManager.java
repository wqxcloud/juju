package com.juju.app.service.im.manager;

import android.text.TextUtils;
import android.util.Log;

import com.juju.app.biz.DaoSupport;
import com.juju.app.biz.MessageDao;
import com.juju.app.biz.impl.MessageDaoImpl;
import com.juju.app.entity.base.MessageEntity;
import com.juju.app.entity.chat.PeerEntity;
import com.juju.app.entity.chat.SessionEntity;
import com.juju.app.event.MessageEvent;
import com.juju.app.event.RefreshHistoryMsgEvent;
import com.juju.app.golobal.Constants;
import com.juju.app.golobal.DBConstant;
import com.juju.app.golobal.MessageConstant;
import com.juju.app.helper.chat.SequenceNumberMaker;
import com.juju.app.service.im.callback.XMPPServiceCallbackImpl;
import com.juju.app.service.im.iq.RedisResIQ;
import com.juju.app.service.im.service.XMPPServiceImpl;
import com.juju.app.utils.Logger;
import com.juju.app.utils.StringUtils;

import org.apache.commons.lang.math.NumberUtils;
import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.SmackException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 项目名称：juju
 * 类描述：消息管理服务
 * 创建人：gm
 * 日期：2016/3/22 09:45
 * 版本：V1.0.0
 */
public class IMMessageManager extends IMManager {

    private Logger logger = Logger.getLogger(IMMessageManager.class);

    private final String TAG = getClass().getSimpleName();

    private volatile static IMMessageManager inst;

    private IMSessionManager sessionManager = IMSessionManager.instance();

    private DaoSupport messageDao;

    private MessageDaoImpl publicMessageDao;


    //双重判断+volatile（禁止JMM重排序）保证线程安全
    public static IMMessageManager instance() {
        if(inst == null) {
            synchronized (IMMessageManager.class) {
                if (inst == null) {
                    inst = new IMMessageManager();
                }
            }
        }
        return inst;
    }

    public MessageDaoImpl getPublicMessageDao() {
        return publicMessageDao;
    }

    public DaoSupport getMessageDao() {
        return messageDao;
    }

    @Override
    public void doOnStart() {
        messageDao = new MessageDaoImpl(ctx);
        publicMessageDao = (MessageDaoImpl) messageDao;
    }

    public void onLoginSuccess() {
        if (!EventBus.getDefault().isRegistered(inst)) {
            EventBus.getDefault().register(inst);
        }
    }

    /**
     * 上下文环境的更新
     * 1. 环境变量的clear
     * 2. eventBus的清空
     */
    @Override
    public void reset() {

    }


    public void sendText(final MessageEntity msgEntity) {
        if (isAuthenticated()) {
            String uuid = UUID.randomUUID().toString();
            msgEntity.setStatus(MessageConstant.MSG_SENDING);
            msgEntity.setId(uuid);
            //插入本地数据库
            final MessageEntity dbMessage = msgEntity.clone();
            messageDao.saveOrUpdate(dbMessage);
            try {
                socketService.sendMessage("ceshi@conference.juju",
                        msgEntity.getContent(), uuid, new XMPPServiceCallbackImpl() {
                            @Override
                            public void onSuccess(Object t) {
                                //回复时间
                                if (t instanceof XMPPServiceImpl.ReplayMessageTime) {
                                    XMPPServiceImpl.ReplayMessageTime messageTime =
                                            (XMPPServiceImpl.ReplayMessageTime) t;
                                    String id = messageTime.getId();
                                    String time = messageTime.getTime();
                                    if (dbMessage != null) {
                                        dbMessage.setStatus(MessageConstant.MSG_SUCCESS);
                                        //通知消息
                                        msgEntity.setStatus(MessageConstant.MSG_SUCCESS);
                                        if (NumberUtils.isNumber(time)) {
                                            int msgId = SequenceNumberMaker.getInstance()
                                                    .makelocalUniqueMsgId(Long.parseLong(time));
                                            //更新msgId
                                            dbMessage.setMsgId(msgId);
                                            dbMessage.setCreated(Long.parseLong(time));
                                            dbMessage.setUpdated(Long.parseLong(time));
                                        }
                                        //更新会话
                                        sessionManager.updateSession(dbMessage);
                                        triggerEvent(new MessageEvent(MessageEvent.Event
                                                .ACK_SEND_MESSAGE_OK, msgEntity));
                                        messageDao.saveOrUpdate(dbMessage);
                                    }
                                }
                            }

                            @Override
                            public void onFailed() {
                                //消息发送失败
                                dbMessage.setStatus(MessageConstant.MSG_FAILURE);
                                messageDao.saveOrUpdate(dbMessage);
                                msgEntity.setStatus(MessageConstant.MSG_FAILURE);
                                triggerEvent(new MessageEvent(MessageEvent.Event.ACK_SEND_MESSAGE_FAILURE, msgEntity));
                            }

                            @Override
                            public void onTimeout() {
                                //消息发送超时
                                dbMessage.setStatus(MessageConstant.MSG_FAILURE);
                                messageDao.saveOrUpdate(dbMessage);
                                msgEntity.setStatus(MessageConstant.MSG_FAILURE);
                                triggerEvent(new MessageEvent(MessageEvent.Event.ACK_SEND_MESSAGE_FAILURE, msgEntity));
                            }
                        });
            } catch (SmackException.NotConnectedException e) {
                Log.e(TAG, "sendText:", e);
                triggerEvent(new MessageEvent(MessageEvent.Event.ACK_SEND_MESSAGE_FAILURE, msgEntity));
            }
        } else {
            //TODO ChatActivity提示未验证通过
        }
    }

    // 拉取历史消息 {from ChatActivity}
    public List<MessageEntity> loadHistoryMsg(int pullTimes, String sessionKey,
                                              PeerEntity peerEntity) {
        int lastMsgId = 99999999;
        long lastCreateTime = 1761055138000l;
        int count = Constants.MSG_CNT_PER_PAGE;
        SessionEntity sessionEntity = IMSessionManager.instance().findSession(sessionKey);
        if (sessionEntity != null) {
            // 以前已经聊过天，删除之后，sessionEntity不存在
            logger.i("#loadHistoryMsg# sessionEntity is null");
            lastMsgId = sessionEntity.getLatestMsgId();
            // 这个地方设定有问题，先使用最大的时间,session的update设定存在问题
//            lastCreateTime = sessionEntity.getUpdated();
        }

        if(lastMsgId <1 || TextUtils.isEmpty(sessionKey)){
            return Collections.emptyList();
        }

        if (count > lastMsgId) {
            count = lastMsgId;
        }

        List<MessageEntity> msgList = doLoadHistoryMsg(
                pullTimes, peerEntity.getPeerId(),
                peerEntity.getType(),
                sessionKey, lastMsgId, lastCreateTime, count);

        return msgList;
    }

    // 根据次数有点粗暴
    public List<MessageEntity> loadHistoryMsg(MessageEntity entity, int pullTimes) {
        logger.d("IMMessageActivity#LoadHistoryMsg");
        // 在滑动的过程中请求，msgId请求下一条的
        int reqLastMsgId = entity.getMsgId() - 1;
//        int loginId = IMLoginManager.instance().getLoginId();
        long reqLastCreateTime = entity.getCreated();
        String chatKey = entity.getSessionKey();
        int cnt = Constants.MSG_CNT_PER_PAGE;
        List<MessageEntity> msgList = doLoadHistoryMsg(pullTimes,
                "0", entity.getSessionType(), chatKey, reqLastMsgId, reqLastCreateTime, cnt);
        return msgList;
    }

    /**
     * 从DB中请求信息
     * 1. 从最近会话点击进入，拉取消息
     * 2. 在消息页面下拉刷新
     *
     * @param pullTimes
     * @param peerId
     * @param peerType
     * @param sessionKey
     * @param lastMsgId
     * @param lastCreateTime
     * @param count
     * @return
     */
    private List<MessageEntity> doLoadHistoryMsg(int pullTimes, final String peerId,
                                                 final int peerType, final String sessionKey,
                                                 int lastMsgId, long lastCreateTime, int count) {
        if(lastMsgId <1 || TextUtils.isEmpty(sessionKey)){
            return Collections.emptyList();
        }
        if (count > lastMsgId) {
            count = lastMsgId;
        }
        // 降序结果输出desc
        List<MessageEntity> listMsg = publicMessageDao.findHistoryMsgs(sessionKey, lastMsgId, lastCreateTime, count);
        // asyn task refresh
        int resSize = listMsg.size();
        logger.d("LoadHistoryMsg return size is %d", resSize);
        if (resSize == 0 || pullTimes == 1 || pullTimes % 3 == 0) {
            //刷新历史记录
            RefreshHistoryMsgEvent historyMsgEvent = new RefreshHistoryMsgEvent();
            historyMsgEvent.pullTimes = pullTimes;
            historyMsgEvent.count = count;
            historyMsgEvent.lastMsgId = lastMsgId;
            historyMsgEvent.listMsg = listMsg;
            historyMsgEvent.peerId = peerId;
            historyMsgEvent.peerType = peerType;
            historyMsgEvent.sessionKey = sessionKey;
            triggerEvent(historyMsgEvent);
        }
        return listMsg;
    }

    /**
     * 事件的处理会在一个后台线程中执行，对应的函数名是onEventBackgroundThread，
     * 虽然名字是BackgroundThread，事件处理是在后台线程，
     * 但事件处理时间还是不应该太长
     * 因为如果发送事件的线程是后台线程，会直接执行事件，
     * 如果当前线程是UI线程，事件会被加到一个队列中，由一个线程依次处理这些事件，
     * 如果某个事件处理时间太长，会阻塞后面的事件的派发或处理
     */
    public void onEventBackgroundThread(RefreshHistoryMsgEvent historyMsgEvent) {
        doRefreshLocalMsg(historyMsgEvent);
    }

    /**
     * asyn task
     * 因为是多端同步，本地信息并不一定完成，拉取时提前异步检测
     */
    private void doRefreshLocalMsg(RefreshHistoryMsgEvent hisEvent) {
        /**check DB数据的一致性*/
        int lastSuccessMsgId = hisEvent.lastMsgId;
        List<MessageEntity> listMsg = hisEvent.listMsg;

        int resSize = listMsg.size();
        if (hisEvent.pullTimes > 1) {
            for (int index = resSize - 1; index >= 0; index--) {
                MessageEntity entity = listMsg.get(index);
                if (!SequenceNumberMaker.getInstance().isFailure(entity.getMsgId())) {
                    lastSuccessMsgId = entity.getMsgId();
                    break;
                }
            }
        } else {
            /**是第一次拉取*/
            if (SequenceNumberMaker.getInstance().isFailure(lastSuccessMsgId))
            /**正序第一个*/
                for (MessageEntity entity : listMsg) {
                    if (!SequenceNumberMaker.getInstance().isFailure(entity.getMsgId())) {
                        lastSuccessMsgId = entity.getMsgId();
                        break;
                    }
                }
        }

        final int refreshCnt = hisEvent.count * 3;
        String peerId = hisEvent.peerId;
        int peerType = hisEvent.peerType;
        String sessionKey = hisEvent.sessionKey;
        boolean localFailure = SequenceNumberMaker.getInstance().isFailure(lastSuccessMsgId);
        if (localFailure) {
            logger.e("LoadHistoryMsg# all msg is failure!");
            if (hisEvent.pullTimes == 1) {
                //走网络
//                reqHistoryMsgNet(peerId, peerType, lastSuccessMsgId, refreshCnt);
            }
        } else {
            /**正常*/
            refreshDBMsg(peerId, peerType, sessionKey, lastSuccessMsgId, refreshCnt);
        }
    }

    /**
     * 历史消息直接从DB中获取。
     * 所以要保证DB数据没有问题
     */
    public void refreshDBMsg(String peerId, int peedType, String chatKey, int lastMsgId, int refreshCnt) {
        if (lastMsgId < 1) {
            return;
        }
        int beginMsgId = lastMsgId - refreshCnt;
        if (beginMsgId < 1) {
            beginMsgId = 1;
        }

        // 返回的结果是升序
//        List<Integer> msgIdList =  dbInterface.refreshHistoryMsgId(chatKey, beginMsgId, lastMsgId);
//        if(msgIdList.size() == (lastMsgId-beginMsgId+1)){
//            logger.d("refreshDBMsg#do need refresh Message!,cause sizeOfList is right");
//            return;
//        }
        // 查找缺失的msgid
        List<Integer> needReqList = new ArrayList<>();
//        for(int startIndex = beginMsgId, endIndex = lastMsgId; startIndex <= endIndex; startIndex++){
//            if(!msgIdList.contains(startIndex)){
//                needReqList.add(startIndex);
//            }
//        }
        // 请求缺失的消息
        if (needReqList.size() > 0) {
            reqMsgById(peerId, peedType, needReqList);
        }


        //需要和服务器进行比对同步
        /**事件驱动通知*/
        MessageEvent event = new MessageEvent();
        event.setEvent(MessageEvent.Event.HISTORY_MSG_OBTAIN);
        triggerEvent(event);

    }

    private void reqMsgById(String peerId, int sessionType, List<Integer> msgIds) {
//        int userId = IMLoginManager.instance().getLoginId();
//        IMBaseDefine.SessionType  sType = Java2ProtoBuf.getProtoSessionType(sessionType);
//        IMMessage.IMGetMsgByIdReq  imGetMsgByIdReq = IMMessage.IMGetMsgByIdReq.newBuilder()
//                .setSessionId(peerId)
//                .setUserId(userId)
//                .setSessionType(sType)
//                .addAllMsgIdList(msgIds)
//                .build();
//        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
//        int cid = IMBaseDefine.MessageCmdID.CID_MSG_GET_BY_MSG_ID_REQ_VALUE;
//        imSocketManager.sendRequest(imGetMsgByIdReq,sid,cid);
    }

    public List<MessageEntity> findAll4Order(String orders) {
        List<MessageEntity> list = new ArrayList<MessageEntity>();
        list = messageDao.findAll4Order(orders);
        return list;
    }

    public MessageEntity getMessageEntity(RedisResIQ redisResIQ) throws JSONException {
        MessageEntity messageEntity = null;
        if(StringUtils.isNotBlank(redisResIQ.getContent())) {
            messageEntity = new MessageEntity();
            JSONObject jsonBody = new JSONObject(redisResIQ.getContent());
            String to = jsonBody.getString("to");
            String from = jsonBody.getString("from");
            String thread = jsonBody.getString("thread");
            String body = jsonBody.getString("body");

            messageEntity.setCreated(Long.parseLong(thread));
            messageEntity.setUpdated(Long.parseLong(thread));
            int msgId = SequenceNumberMaker.getInstance()
                    .makelocalUniqueMsgId(Long.valueOf(thread));


            String[] fromArr = from.split("@");
            if(fromArr.length >=2) {
                messageEntity.setFromId(fromArr[0]);
            } else {
                messageEntity.setFromId(from);
            }
            messageEntity.setMsgId(msgId);
            messageEntity.setToId(to);
            messageEntity.setMsgType(DBConstant.MSG_TYPE_GROUP_TEXT);
            messageEntity.setStatus(MessageConstant.MSG_SUCCESS);
            messageEntity.setContent(body);
            messageEntity.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
            messageEntity.buildSessionKey(false);
        }
        return messageEntity;
    }

//    long nowTime = System.currentTimeMillis();
//    textMessage.setFromId(fromUser.getPeerId());
//    textMessage.setToId(peerEntity.getPeerId());
//    textMessage.setUpdated(nowTime);
//    textMessage.setCreated(nowTime);
//    textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
//    textMessage.setGIfEmo(true);
//    int peerType = peerEntity.getType();
//    int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_TEXT
//            : DBConstant.MSG_TYPE_SINGLE_TEXT;
//    textMessage.setMsgType(msgType);
//    textMessage.setStatus(MessageConstant.MSG_SENDING);
//    // 内容的设定
//    textMessage.setContent(content);
//    textMessage.buildSessionKey(true);


    /**
     * 供IMUnreadMsgManager调用
     * @param messageEntity
     */
    public void saveMessage(MessageEntity  messageEntity) {
        if(messageEntity != null) {
            messageDao.replaceInto(messageEntity);
        }
    }

}
