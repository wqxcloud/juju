package com.juju.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.juju.app.R;
import com.juju.app.activity.party.PlanDetailActivity;
import com.juju.app.config.HttpConstants;
import com.juju.app.entity.PlanVote;
import com.juju.app.utils.ImageLoaderUtil;
import com.juju.app.utils.ViewHolderUtil;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 项目名称：juju
 * 类描述：群聊列表数据源
 * 创建人：gm
 * 日期：2016/2/21 17:09
 * 版本：V1.0.0
 */
public class PlanVoteListAdapter extends BaseAdapter {

    private PlanDetailActivity context;

    private List<PlanVote> planVoteList;

    private int changeIndex;

    public PlanVoteListAdapter(PlanDetailActivity context, List<PlanVote> planVoteList) {
        this.context = context;
        if(planVoteList == null){
            this.planVoteList = new ArrayList<PlanVote>();
        }else {
            this.planVoteList = planVoteList;
        }
    }

    public void setPlanVoteList(List<PlanVote> planVoteList) {
        if(planVoteList == null){
            this.planVoteList = new ArrayList<PlanVote>();
        }else {
            this.planVoteList = planVoteList;
        }
    }

    @Override
    public int getCount() {
        return planVoteList.size();
    }

    @Override
    public Object getItem(int position) {
        return planVoteList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.user_icon_item, parent, false);
        }
        CircleImageView img_head = ViewHolderUtil.get(convertView, R.id.img_head);
        TextView txt_nickName = ViewHolderUtil.get(convertView, R.id.txt_nick_name);

        final PlanVote planVote = planVoteList.get(position);
        ImageLoaderUtil.getImageLoaderInstance().displayImage(HttpConstants.getUserUrl() + "/getPortraitSmall?targetNo="
                + planVote.getAttenderNo(), img_head, ImageLoaderUtil.DISPLAY_IMAGE_OPTIONS);
        txt_nickName.setText(context.getIMContactManager().findContact(planVote.getAttenderNo()).getNickName());

        return convertView;

    }


}
