package com.juju.app.fragment.party;

import android.util.Log;
import android.widget.ListView;

import com.juju.app.R;
import com.juju.app.activity.party.PartyActivity;
import com.juju.app.activity.party.PlayVideoActivity;
import com.juju.app.adapters.VideoProgramListAadpter;
import com.juju.app.annotation.CreateFragmentUI;
import com.juju.app.bean.json.GetVideoUrlsResBean;
import com.juju.app.config.HttpConstants;
import com.juju.app.entity.VideoProgram;
import com.juju.app.https.HttpCallBack;
import com.juju.app.https.JlmHttpClient;
import com.juju.app.ui.base.BaseFragment;
import com.juju.app.ui.base.CreateUIHelper;
import com.juju.app.utils.ActivityUtil;

import org.json.JSONException;
import org.xutils.common.Callback;
import org.xutils.view.annotation.ContentView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 项目名称：juju
 * 类描述：群聊—Fragment
 * 创建人：gm
 * 日期：2016/2/18 15:09
 * 版本：V1.0.0
 */
@ContentView(R.layout.fragment_live)
@CreateFragmentUI(viewId = R.layout.fragment_live)
public class LiveFragment extends BaseFragment implements CreateUIHelper,HttpCallBack, VideoProgramListAadpter.Callback {


    private PartyActivity parentActivity;

    private ListView listView;
    private List<VideoProgram> videoProgramList;




    @Override
    public void setOnListener() {
    }

    /**
     * 刷新页面
     */
    public void refresh() {

    }

    @Override
    protected void findViews() {
        super.findViews();
        listView = (ListView) findViewById(R.id.listLiveView);
        parentActivity = (PartyActivity) getActivity();
    }

    @Override
    public void loadData() {
        initTestData();
    }

    @Override
    public void initView() {

    }

    /**
     * 测试数据
     */
    private void initTestData() {


        Map<String, Object> valueMap = new HashMap<String, Object>();
        JlmHttpClient<Map<String, Object>> client = new JlmHttpClient<Map<String, Object>>(23,HttpConstants.getUserUrl() + "/getVideoUrls", this, valueMap, GetVideoUrlsResBean.class);

        //增加注释
        try {
            client.sendGet();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void onSuccess(ResponseInfo<String> responseInfo, int accessId, Object... obj) {
//
//    }
//
//    @Override
//    public void onFailure(HttpException error, String msg, int accessId) {
//        switch (accessId){
//            case 23:
//
//                break;
//        }
//    }

    @Override
    public void onSuccess(Object obj, int accessId, Object inputParameter) {
        switch (accessId) {
            case 23:
                if(obj != null) {
                    GetVideoUrlsResBean videoUrlsResBeann = (GetVideoUrlsResBean)obj;
                    videoProgramList = new ArrayList<VideoProgram>();
                    VideoProgram v1 = new VideoProgram();
                    v1.setStatus(0);
                    v1.setVideoUrl("rtmp://219.143.237.232:1935/juju/12345");

                    VideoProgram v2 = new VideoProgram();
                    v2.setStatus(1);
                    if(videoUrlsResBeann.getVideoUrl2()!=null && !videoUrlsResBeann.getVideoUrl2().equals("null")){
                        v2.setVideoUrl("http://219.143.237.232:8080/hls/12345.m3u8");
                    }else{
                        v2.setVideoUrl("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
                    }


                    VideoProgram v3 = new VideoProgram();
                    v3.setStatus(1);
                    v3.setVideoUrl("rtmp://219.143.237.232:1935/juju/123456");
                    videoProgramList.add(v1);
                    videoProgramList.add(v2);
                    videoProgramList.add(v3);

                    VideoProgramListAadpter adapter = new VideoProgramListAadpter(null, videoProgramList);
                    adapter.setCallback(this);
                    listView.setAdapter(adapter);
                }
                break;
        }
    }

    @Override
    public void onFailure(Throwable ex, boolean isOnCallback, int accessId, Object inputParameter) {
        System.out.println("accessId:" + accessId + "\r\n isOnCallback:" + isOnCallback );
        Log.e("LiveFragment", "onFailure", ex);
    }

    @Override
    public void onCancelled(Callback.CancelledException cex) {

    }

    @Override
    public void onFinished() {

    }

    @Override
    public void playVideo(VideoProgram videoProgram) {
        //  TODO 获取视频请求的URL参数，传入播放界面
//        BasicNameValuePair nvPair = new BasicNameValuePair("videoUrl", videoProgram.getVideoUrl()+"?requestId="+ UUID.randomUUID().toString());
        ActivityUtil.startActivity4UPAndNew(getActivity(), PlayVideoActivity.class, "videoUrl",
                videoProgram.getVideoUrl()+"?requestId="+ UUID.randomUUID().toString());
    }
}
