package com.juju.app.adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.juju.app.R;
import com.juju.app.config.HttpConstants;
import com.juju.app.entity.Party;
import com.juju.app.helper.IMUIHelper;
import com.juju.app.ui.base.BaseActivity;
import com.juju.app.utils.ImageLoaderUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class PartyListAdapter extends BaseAdapter {
    private LayoutInflater inflater;

    private boolean isSearchMode = false;
    private String searchKey;

    private boolean isStockMode = false;


    private List<Party> partyList = new ArrayList<Party>();
    private List<Party> matchPartyList = new ArrayList<Party>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Callback mCallback;
    private Comparator<Party> comparator = new Comparator<Party>() {
        @Override
        public int compare(Party p1, Party p2) {
            if(p1.getFollowFlag()==p2.getFollowFlag()){
                Long.valueOf(p2.getLocalId()).compareTo(p1.getLocalId());
            }
            if(p1.getFollowFlag()==1){
                return -1;
            }else{
                return 1;
            }
        }
    };

    public void setPartyList(List<Party> partyList) {
        if (partyList != null) {
            this.partyList = partyList;
            this.matchPartyList = partyList;
        }

    }

    public void reOrderParty() {
        Collections.sort(partyList,comparator);
        if(isSearchMode()){
            Collections.sort(matchPartyList,comparator);
        }
    }


    public interface Callback {
        public void follow(Party party);
        public void showParty(Party party);
    }

    public PartyListAdapter(LayoutInflater inflater,Callback callback) {
        this.inflater = inflater;
        this.mCallback = callback;
    }

    @Override
    public int getCount() {
        return matchPartyList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return matchPartyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Party party = matchPartyList.get(position);
        if (view == null) {
            view = inflater.inflate(R.layout.party_item, parent, false);
        }

        ImageView partyImage = (ImageView) view.findViewById(R.id.party_preview);
        partyImage.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);



        if(party.getCoverUrl().startsWith("http:")){
            ImageLoaderUtil.getImageLoaderInstance().displayImage(party.getCoverUrl(), partyImage, ImageLoaderUtil.DISPLAY_IMAGE_OPTIONS);
        }else{
            int resId = ((BaseActivity) inflater.getContext()).getResValue(party.getCoverUrl().toLowerCase(), "drawable");
            ImageLoaderUtil.getImageLoaderInstance().displayImage("drawable://" + resId, partyImage, ImageLoaderUtil.DISPLAY_IMAGE_OPTIONS);
        }

        TextView txtUnRead = (TextView)view.findViewById(R.id.un_read);
        CircleImageView imgCreatorHead = (CircleImageView) view.findViewById(R.id.creatorImage);
        TextView txtCreatorName = (TextView) view.findViewById(R.id.creator_name);
        TextView txtPartyName = (TextView) view.findViewById(R.id.party_name);
        TextView txtTime = (TextView) view.findViewById(R.id.time);
        TextView txtPartyDesc = (TextView) view.findViewById(R.id.partyDesc);
        TextView txtStatus = (TextView) view.findViewById(R.id.txt_status);
        TextView txtViewFollow = (TextView) view.findViewById(R.id.flag_follow);
        ImageView imgFlag = (ImageView) view.findViewById(R.id.img_flag);

        if(party.isNew()){
            txtUnRead.setVisibility(View.VISIBLE);
        }else{
            txtUnRead.setVisibility(View.GONE);
        }


        partyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.showParty(party);
            }
        });

        partyImage.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                mCallback.follow(party);
                return true;
            }
        });

        ImageLoaderUtil.getImageLoaderInstance().displayImage(HttpConstants.getUserUrl() + "/getPortraitSmall?targetNo="
                + party.getUserNo(), imgCreatorHead, ImageLoaderUtil.DISPLAY_IMAGE_OPTIONS);

        txtCreatorName.setText(party.getCreator().getNickName());

        if (isSearchMode) {
            // 高亮显示
            if(party.isDescMatch()) {
                IMUIHelper.setTextHilighted(txtPartyDesc, party.getDesc(),
                        party.getSearchElement());
                txtPartyName.setText(party.getName());
            }else{
                IMUIHelper.setTextHilighted(txtPartyName, party.getName(),
                        party.getSearchElement());
                txtPartyDesc.setText(party.getDesc());
            }
        } else {
            txtPartyName.setText(party.getName());
            txtPartyDesc.setText(party.getDesc());
        }

        if (party.getTime() != null) {
            txtTime.setText(dateFormat.format(party.getTime()));
        } else {
            txtTime.setText("暂无任何方案");
        }

        switch (party.getFollowFlag()) {
            case 0:
                txtViewFollow.setVisibility(View.GONE);
                break;
            case 1:
                txtViewFollow.setVisibility(View.VISIBLE);
                break;
        }

        switch (party.getStatus()) {
            case -1:
                imgFlag.setImageResource(R.mipmap.description);
                txtStatus.setText(R.string.drafts);
                break;
            case 0:
                imgFlag.setImageResource(R.mipmap.flag_red);
                txtStatus.setText(R.string.calling);
                break;
            case 1:
                imgFlag.setImageResource(R.mipmap.flag_green);
                txtStatus.setText(R.string.running);
                break;
            case 2:
                imgFlag.setImageResource(R.mipmap.flag_gray);
                txtStatus.setText(R.string.finished);
                break;
        }
        return view;
    }

    public void recover() {
        isSearchMode = false;
        matchPartyList = partyList;
        notifyDataSetChanged();
    }

    public void onSearch(String key) {
        isSearchMode = true;
        searchKey = key;
        List<Party> searchList = new ArrayList<Party>();
        for (Party party : partyList) {
            if (IMUIHelper.handlePartySearch(searchKey, party)) {
                searchList.add(party);
            }
        }
        matchPartyList = searchList;
        notifyDataSetChanged();
    }

    public List<Party> getMatchPartyList() {
        return matchPartyList;
    }

    public boolean isSearchMode() {
        return isSearchMode;
    }

    public void setStockMode(boolean stockMode) {
        isStockMode = stockMode;
    }
}
