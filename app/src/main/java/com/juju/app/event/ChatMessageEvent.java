package com.juju.app.event;

import com.juju.app.golobal.IMBaseDefine;

import org.jivesoftware.smack.packet.Message;

/**
 * 项目名称：juju
 * 类描述：聊天消息事件
 * 创建人：gm
 * 日期：2016/6/27 11:16
 * 版本：V1.0.0
 */
public class ChatMessageEvent {

    public IMBaseDefine.MsgType msgType;
    public Message message;

}
