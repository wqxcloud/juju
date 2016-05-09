package com.juju.app.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juju.app.R;
import com.juju.app.activity.party.PartyCreateActivity;
import com.juju.app.annotation.CreateUI;
import com.juju.app.bean.UserInfoBean;
import com.juju.app.config.HttpConstants;
import com.juju.app.entity.Person;
import com.juju.app.entity.base.MessageEntity;
import com.juju.app.entity.chat.SessionEntity;
import com.juju.app.fragment.GroupChatFragment;
import com.juju.app.fragment.GroupPartyFragment;
import com.juju.app.fragment.MeFragment;
import com.juju.app.golobal.Constants;
import com.juju.app.golobal.GlobalVariable;
import com.juju.app.https.HttpCallBack;
import com.juju.app.https.HttpCallBack4OK;
import com.juju.app.https.JlmHttpClient;
import com.juju.app.service.im.manager.IMGroupManager;
import com.juju.app.service.im.manager.IMMessageManager;
import com.juju.app.service.im.manager.IMSessionManager;
import com.juju.app.ui.base.BaseActivity;
import com.juju.app.ui.base.BaseApplication;
import com.juju.app.ui.base.CreateUIHelper;

import com.juju.app.utils.ActivityUtil;
import com.juju.app.utils.JacksonUtil;
import com.juju.app.utils.Logger;
import com.juju.app.utils.StringUtils;
import com.juju.app.utils.ToastUtil;
import com.juju.app.view.dialog.titlemenu.ActionItem;
import com.juju.app.view.dialog.titlemenu.TitlePopup;
import com.juju.app.view.dialog.titlemenu.TitlePopup.OnItemOnClickListener;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.rey.material.app.Dialog;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ContentView(R.layout.activity_main)
@CreateUI
public class MainActivity extends BaseActivity implements CreateUIHelper, HttpCallBack4OK {

    private final String TAG = getClass().getSimpleName();

    private final int CREATE_GROUP = 0x01;

    private final int DELETE_GROUP = 0x02;


    private Logger logger = Logger.getLogger(MainActivity.class);


    @ViewInject(R.id.img_right)
    private ImageView img_right;

    @ViewInject(R.id.txt_title)
    private TextView txt_title;

    @ViewInject(R.id.layout_bar)
    private RelativeLayout layout_bar;

    @ViewInject(R.id.unread_msg_number)
    private TextView tx_unread_msg_number;


    private GroupChatFragment groupChatFragment;
    private GroupPartyFragment groupPartyFragment;
    private MeFragment meFragment;
    private TitlePopup titlePopup;


    private Fragment[] fragments;
    private ImageView[] imagebuttons;
    private TextView[] textviews;
    private int index;
    private int currentTabIndex;// 当前fragment的index


    private UserInfoBean userInfoBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void loadData() {
        if(GlobalVariable.isSkipLogin()){
            return;
        }
        userInfoBean = BaseApplication.getInstance().getUserInfoBean();

        Person person = new Person();
        person.setName("gm");
        person.setAge(30);

        person.save();
//        testSqlLite();
    }

    @Override
    public void initView() {
        long begin = System.currentTimeMillis();
        initTabView();
        initPopWindow();
        long end = System.currentTimeMillis();
        Log.d(TAG, "init MainActivity cost time:" + (end - begin) + "毫秒");
    }

    /**
     * 初始化TabView
     */
    private void initTabView() {
        groupChatFragment = new GroupChatFragment();
        groupPartyFragment = new GroupPartyFragment();
        meFragment = new MeFragment();
        fragments = new Fragment[] { groupChatFragment, groupPartyFragment,
                meFragment };
        imagebuttons = new ImageView[3];
        imagebuttons[0] = (ImageView) findViewById(R.id.ib_group_chat);
        imagebuttons[1] = (ImageView) findViewById(R.id.ib_group_party);
        imagebuttons[2] = (ImageView) findViewById(R.id.ib_profile);
        imagebuttons[0].setSelected(true);

        textviews = new TextView[3];
        textviews[0] = (TextView) findViewById(R.id.tv_group_chat);
        textviews[1] = (TextView) findViewById(R.id.tv_group_party);
        textviews[2] = (TextView) findViewById(R.id.tv_profile);
        textviews[0].setTextColor(getResources().getColor(R.color.blue));

        // 添加显示第一个fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, groupChatFragment)
                .add(R.id.fragment_container, groupPartyFragment)
                .add(R.id.fragment_container, meFragment)
                .hide(groupPartyFragment).hide(meFragment).show(groupChatFragment).commit();
    }

    public void onTabClicked(View view) {
        img_right.setVisibility(View.GONE);
        switch (view.getId()) {
            case R.id.re_group_chat:
                img_right.setVisibility(View.VISIBLE);
                index = 0;
                if (groupChatFragment != null) {
                    groupChatFragment.refresh();
                }
                txt_title.setText(R.string.group_chat);
                img_right.setImageResource(R.mipmap.icon_add);
                break;
            case R.id.re_group_party:
                index = 1;
                txt_title.setText(R.string.group_party);
                img_right.setVisibility(View.VISIBLE);
                img_right.setImageResource(R.mipmap.icon_add);
                break;
            case R.id.re_profile:
                index = 2;
                txt_title.setText(R.string.me);
                break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager()
                    .beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        imagebuttons[currentTabIndex].setSelected(false);
        // 把当前tab设为选中状态
        imagebuttons[index].setSelected(true);
        textviews[currentTabIndex].setTextColor(getResources().getColor(R.color.gray));
        if(index == 1){
            textviews[index].setTextColor(getResources().getColor(R.color.white));
        }else {
            textviews[index].setTextColor(getResources().getColor(R.color.blue));
        }
        currentTabIndex = index;
    }


    private OnItemOnClickListener onitemClick = new OnItemOnClickListener() {

        @Override
        public void onItemClick(ActionItem item, int position) {
            switch (position) {
                case 0:// 创建群
                    showCreateGroupChatDialog();
//                    startActivity(MainActivity.this, AddGroupChatActivity.class);
                    break;
                case 1:// 扫一扫加群
                    break;
                case 2:// 邀请码加群
                    break;
                default:
                    break;
            }
        }
    };

    private void initPopWindow() {
        // 实例化标题栏弹窗
        titlePopup = new TitlePopup(this, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titlePopup.setItemOnClickListener(onitemClick);
        // 给标题栏弹窗添加子类
        titlePopup.addAction(new ActionItem(this, R.string.menu_group,
                R.mipmap.icon_menu_group));
        titlePopup.addAction(new ActionItem(this, R.string.menu_qrcode,
                R.mipmap.icon_menu_qrcode));
        titlePopup.addAction(new ActionItem(this, R.string.menu_invitecode,
                R.mipmap.icon_menu_invitecode));

    }


    @OnClick(R.id.img_right)
    public void clickImgRight(View v) {
        switch(index) {
            case 0: //  群聊
                titlePopup.show(layout_bar);
                break;
            case 1: //  聚会
                //TODO 需要修改为从群聊中发起聚会
                String groupId = "570dbc6fe4b092891a647e32";
                BasicNameValuePair phoneValue = new BasicNameValuePair(Constants.GROURP_ID,groupId);
                ActivityUtil.startActivity(this,PartyCreateActivity.class,phoneValue);
                break;
        }
    }


//    private void sendMessage() {
//        int i = 0;
//        IMLoginManager.instance().sendMessage("ceshi@conference.juju", "在线吗？" + i);
//    }

    //加入聊天室
    private void joinChatRoom() {
//        try {
//            Thread.sleep(2000l);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        IMLoginManager.instance().joinChatRoom();
    }

//    public String getRunningServicesInfo(Context context) {
//        StringBuffer serviceInfo = new StringBuffer();
//        final ActivityManager activityManager = (ActivityManager) context
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(100);
//
//        Iterator<ActivityManager.RunningServiceInfo> l = services.iterator();
//        while (l.hasNext()) {
//            ActivityManager.RunningServiceInfo si = (ActivityManager.RunningServiceInfo) l.next();
//            serviceInfo.append("pid: ").append(si.pid);
//            serviceInfo.append("\nprocess: "+si.process);
//            serviceInfo.append("\nservice: ").append(si.service);
//            serviceInfo.append("\ncrashCount: ").append(si.crashCount);
//            serviceInfo.append("\nclientCount: ").append(si.clientCount);
//            serviceInfo.append("\nactiveSince: ").append(si.activeSince);
//            serviceInfo.append("\nlastActivityTime: ").append(si.activeSince);
//            serviceInfo.append(";");
//        }
//        return serviceInfo.toString();
//    }

    private void testSqlLite() {
        List<MessageEntity> entrys = IMMessageManager.instance().getPublicMessageDao().findAll();
        if(entrys != null && entrys.size() >0) {
            for(MessageEntity entry : entrys) {
                Log.d(TAG, "MessageEntity entry:" + JacksonUtil.turnObj2String(entry));
            }
        }
        List<SessionEntity> entrys2 = null;
        try {
            entrys2 = IMSessionManager.instance().getSessionDao().
                    findAll("select * from com_juju_app_entity_chat_SessionEntity");
        } catch (DbException e) {
            e.printStackTrace();
        }
        if(entrys2 != null && entrys2.size() >0) {
            for(SessionEntity entry : entrys2) {
                Log.d(TAG, "SessionEntity entry:" + JacksonUtil.turnObj2String(entry));
            }
        }
    }

    //显示未读消息总数
    public void setUnreadMessageCnt(int unreadNum) {

        logger.d("unread#setUreadNotify -> unreadNum:%d", unreadNum);
        if (0 == unreadNum) {
            tx_unread_msg_number.setVisibility(View.INVISIBLE);
            return;
        }

        String notify;
        if (unreadNum > 99) {
            notify = "99+";
        } else {
            notify = Integer.toString(unreadNum);
        }
        tx_unread_msg_number.setText(notify);
        tx_unread_msg_number.setVisibility(View.VISIBLE);
    }

    Dialog mDialog;
    private void showCreateGroupChatDialog() {
        final View view = MainActivity.this.getLayoutInflater().inflate(R.layout.activity_add_group_chat, null);
        if(mDialog == null) {
            mDialog = new Dialog(context, R.style.SimpleDialog);
            mDialog.layoutParams(400, 450);
            mDialog.title(R.string.chat_create)
                    .positiveAction(R.string.confirm)
                    .negativeAction(R.string.negative)
                    .contentView(view)
                    .maxWidth(400)
                    .maxHeight(600)
                    .positiveActionTextAppearance(R.style.other)
                    .negativeActionTextAppearance(R.style.other)
                    .cancelable(true);

            /**
             * 确定按钮监听事件
             */
            mDialog.positiveActionClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDialog != null) {
                        com.rey.material.widget.EditText et_name =
                                (com.rey.material.widget.EditText) view.findViewById(R.id.et_name);
                        com.rey.material.widget.EditText et_description =
                                (com.rey.material.widget.EditText) view.findViewById(R.id.et_description);
                        String name = et_name.getText().toString();
                        String description = et_description.getText().toString();
                        if (StringUtils.isBlank(name)) {
                            ToastUtil.TextIntToast(getApplicationContext(), R.string.chat_name_null, 0);
                            return;
                        }
                        if (name.length() > 20) {
                            ToastUtil.TextIntToast(getApplicationContext(), R.string.chat_name_over_length, 0);
                            return;
                        }
                        Map<String, Object> valueMap = new HashMap<String, Object>();
                        Map<String, Object> groupMap = new HashMap<String, Object>();

                        valueMap.put("userNo", userInfoBean.getJujuNo());
                        valueMap.put("token", userInfoBean.getToken());
                        groupMap.put("name", name);
                        if (StringUtils.isNotBlank(description)) {
                            groupMap.put("desc", description);
                        }
                        valueMap.put("group", groupMap);
                        JlmHttpClient<Map<String, Object>> client = new JlmHttpClient<Map<String, Object>>(
                                CREATE_GROUP, HttpConstants.getUserUrl() + "/addGroup",
                                MainActivity.this, valueMap, JSONObject.class);
                        try {
                            client.sendPost4OK();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        loadingCommon(R.string.chat_create_loading);
                    }
                }
            });

            /**
             * 取消按钮监听事件
             */
            mDialog.negativeActionClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDialog != null) {
                        mDialog.hide();
                    }
                }
            });
        }
        if(!mDialog.isShowing())
            mDialog.show();

    }

    @Override
    public void onSuccess(Object obj, int accessId) {
        if(accessId == CREATE_GROUP) {
            completeLoadingCommon();
            if(obj instanceof  JSONObject) {
                JSONObject result = (JSONObject)obj;
                try {
                    int status = result.getInt("status");
                    if(status == 0) {
                        String groupId = result.getString("groupId");
                        //消息服务器创建群组
                        boolean bool = IMGroupManager.instance().createChatRoom(groupId,
                                userInfoBean.getmMucServiceName(), userInfoBean.getmServiceName());
                        //创建成功
                        if(bool) {
                            //刷新群组，待实现
                            Log.d(TAG, "创建群聊成功，groupId："+groupId);
                        }
                        //创建失败
                        else {
                            //删除业务服务器群组消息
                            Map<String, Object> valueMap = new HashMap<String, Object>();

                            valueMap.put("userNo", userInfoBean.getJujuNo());
                            valueMap.put("token", userInfoBean.getToken());
                            valueMap.put("groupId", groupId);
                            JlmHttpClient<Map<String, Object>> client = new JlmHttpClient<Map<String, Object>>(
                                    DELETE_GROUP, HttpConstants.getUserUrl() + "/deleteGroup",
                                    MainActivity.this, valueMap, JSONObject.class);
                            try {
                                //不需要回复
                                client.sendPost4OK();
                            } catch (UnsupportedEncodingException e) {
                                logger.error(e);
                            } catch (JSONException e) {
                                logger.error(e);
                            }
                            showMsgDialog(R.string.chat_create_error);
                        }
                    } else {
                        showMsgDialog(R.string.chat_create_error);
                    }
                } catch (JSONException e) {
                    logger.error(e);
                }
                if(mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();
            }

        }
    }

    @Override
    public void onFailure(Exception e, int accessId) {
        if(accessId == CREATE_GROUP) {
            completeLoadingCommon();
            showMsgDialog(R.string.chat_create_error);
        }
    }


}
