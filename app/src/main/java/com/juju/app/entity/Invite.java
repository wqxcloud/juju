package com.juju.app.entity;

import com.juju.app.entity.base.BaseEntity;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.Date;


@Table(name = "invite")
public class Invite extends BaseEntity {

    @Column(name = "user_no")
    private String userNo;

    @Column(name = "nick_name")
    private String nickName;

    @Column(name = "time")
    private Date time;

    //  0：未通过   1：通过    -1：等待验证
    @Column(name = "status")
    private int status;


    //  0：邀请   1：被邀请
    @Column(name = "flag")
    private int flag;


    @Column(name = "group_id")
    private String groupId;

    @Column(name = "group_name")
    private String groupName;

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
