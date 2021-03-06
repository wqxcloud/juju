package com.juju.app.view.groupchat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.juju.app.R;
import com.juju.app.utils.DateUtil;

import java.util.Date;

/**
 *
 *
 * 消息列表中的时间气泡
 * [备注] 插入条件是前后两条消息发送的时间diff 超过某个范围
 *
 */
public class TimeRenderView extends LinearLayout {
    private TextView time_title;

    public TimeRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static TimeRenderView inflater(Context context,ViewGroup viewGroup){
        TimeRenderView timeRenderView = (TimeRenderView) LayoutInflater
                .from(context).inflate(R.layout.tt_message_title_time, viewGroup, false);
        return timeRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        time_title = (TextView) findViewById(R.id.time_title);
    }

    /**与数据绑定*/
    public void setTime(Long msgTime){
//        long timeStamp  = (long) msgTime;
        Date msgTimeDate = new Date(msgTime);
        time_title.setText(DateUtil
                .getTimeDiffDesc(msgTimeDate));
    }

}
