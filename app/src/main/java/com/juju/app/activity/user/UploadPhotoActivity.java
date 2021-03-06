package com.juju.app.activity.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juju.app.R;
import com.juju.app.bean.UserInfoBean;
import com.juju.app.config.HttpConstants;
import com.juju.app.golobal.AppContext;
import com.juju.app.golobal.Constants;
import com.juju.app.https.HttpCallBack;
import com.juju.app.https.JlmHttpClient;
import com.juju.app.ui.base.BaseActivity;
import com.juju.app.utils.ImageLoaderUtil;
import com.juju.app.utils.ToastUtil;
import com.juju.app.view.CircleCopperImageView;
import com.juju.app.view.imagezoom.utils.DecodeUtils;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.rey.material.app.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

@ContentView(R.layout.activity_upload_photo)
public class UploadPhotoActivity extends BaseActivity implements HttpCallBack, View.OnLongClickListener {

    private static final String TAG = "UploadPhotoActivity";
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择

    private final String imageName = "juju_head.jpg";

    private int size;


    @ViewInject(R.id.txt_upload_hint)
    private TextView txtUploadHint;
    @ViewInject(R.id.upload_head)
    private CircleCopperImageView headImg;

    @ViewInject(R.id.head)
    private CircleImageView originHeadImg;

    @ViewInject(R.id.menu_layout)
    private RelativeLayout menuLayout;
    @ViewInject(R.id.cancel)
    private TextView txt_cancel;
    @ViewInject(R.id.confirm)
    private TextView txt_confirm;

    private String userNo;

    private Bitmap newPortriat;
    private Bitmap smallPortriat;

    private BottomSheetDialog msgDialog;
    private TextView txtCapture;
    private TextView txtPhoto;
    private TextView txtCancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParam();
        initView();
        setListeners();
    }

    private void initParam() {
        userNo = getIntent().getStringExtra(Constants.USER_NO);
    }


    private void initView() {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        size = (int) (Math.min(metrics.widthPixels, metrics.heightPixels) / 0.55);
        UserInfoBean userInfoBean = AppContext.getUserInfoBean();

        String targetNo = userNo==null?userInfoBean.getUserNo():userNo;

        ImageLoaderUtil.getImageLoaderInstance().displayImage(HttpConstants.getUserUrl() + "/getPortrait?userNo=" + userInfoBean.getUserNo() + "&token=" + userInfoBean.getToken() + "&targetNo=" + targetNo,originHeadImg,ImageLoaderUtil.DISPLAY_IMAGE_OPTIONS);

        if(userNo == null) {
            txtUploadHint.setVisibility(View.VISIBLE);
        }
    }


    private void setListeners() {
        if(userNo == null) {
            originHeadImg.setOnLongClickListener(this);
        }
    }


//    @Event(type = View.OnLongClickListener.class, value = R.id.head)
//    private void headLongClick(View view){
//        if(userNo == null){
//            showPicMenu();
//        }
//    }

    @Override
    public boolean onLongClick(View v) {
        switch(v.getId()){
            case R.id.head:
                showPicMenu();
                break;
        }
        return true;
    }

    private void showPicMenu() {

        msgDialog = new BottomSheetDialog(this);
        msgDialog.setCanceledOnTouchOutside(false);
        msgDialog.contentView(R.layout.layout_bottom_menu)
                .inDuration(300);
        txtCapture = (TextView)msgDialog.findViewById(R.id.btn_menu1);
        txtCapture.setText(R.string.take_camera_btn_text);
        txtCapture.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // 指定调用相机拍照后照片的储存路径
                File cacheDir = new File(Constants.BASE_PATH);
                if(!cacheDir.exists()){
                    cacheDir.mkdir();
                }
                File imageFile = new File(Constants.HEAD_IMAGE_CACHE);
                if (!imageFile.exists()) {
                    try {
                        imageFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
                msgDialog.dismiss();
            }
        });

        txtPhoto = (TextView)msgDialog.findViewById(R.id.btn_menu2);
        txtPhoto.setText(R.string.album);
        txtPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                msgDialog.dismiss();
            }
        });

        txtCancel = (TextView)msgDialog.findViewById(R.id.txt_cancel);
        txtCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                msgDialog.dismiss();
            }
        });

        msgDialog.show();

    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_REQUEST_TAKEPHOTO:
                    originHeadImg.setVisibility(View.GONE);

                    headImg.setVisibility(View.VISIBLE);
                    menuLayout.setVisibility(View.VISIBLE);
                    Bitmap bitmap = DecodeUtils.decode(this, Uri.parse(Constants.HEAD_IMAGE_CACHE), size, size);
                    headImg.setImageBitmap(bitmap);

                    break;

                case PHOTO_REQUEST_GALLERY:
                    originHeadImg.setVisibility(View.GONE);
                    headImg.setVisibility(View.VISIBLE);
                    menuLayout.setVisibility(View.VISIBLE);
                    if (data != null) {
                        Bitmap bitmap1 = DecodeUtils.decode(this, data.getData(), size, size);
                        headImg.setImageBitmap(bitmap1);
                    }
                    break;
            }
            super.onActivityResult(requestCode, resultCode, data);

        }else{
            originHeadImg.setVisibility(View.VISIBLE);
            headImg.setVisibility(View.GONE);
            menuLayout.setVisibility(View.GONE);
        }
    }

    @Event(R.id.confirm)
    private void uploadHead(View view){
        if(!headImg.isFillCircle()){
            ToastUtil.showShortToast(this,"图片需充满圆形区域！",1);
            return;
        }
        newPortriat = headImg.getCroppedImage();
        if(newPortriat.getWidth()>600){
            newPortriat = Bitmap.createScaledBitmap(newPortriat,600,600,true);
        }
        originHeadImg.setImageBitmap(newPortriat);
        smallPortriat = Bitmap.createScaledBitmap(newPortriat,120,120,true);
        Map<String, Object> valueMap = new HashMap<String, Object>();
        UserInfoBean userInfoBean = AppContext.getUserInfoBean();
        valueMap.put("userNo", userInfoBean.getUserNo());
        valueMap.put("token", userInfoBean.getToken());
        valueMap.put("portrait", newPortriat);
        valueMap.put("portraitSmall", smallPortriat);

        JlmHttpClient<Map<String, Object>> client = new JlmHttpClient<Map<String, Object>>(
                R.id.upload_head, HttpConstants.getUserUrl() + "/uploadPortrait", this, valueMap,
                JSONObject.class);
        try {
            loading(true, R.string.uploading);
            txt_confirm.setClickable(false);
            txt_cancel.setClickable(false);
            client.sendUpload();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Event(R.id.cancel)
    private void cancelUpload(View view){
        originHeadImg.setVisibility(View.VISIBLE);
        headImg.setVisibility(View.GONE);
        menuLayout.setVisibility(View.GONE);
    }


    /**
     * 按正方形裁切图片
     */
    public static Bitmap ImageCrop(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

        int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
        int retY = w > h ? 0 : (h - w) / 2;

        //下面这句是关键
        return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
    }


    @Override
    public void onSuccess(Object obj, int accessId, Object inputParameter) {
        switch (accessId) {
            case R.id.upload_head:
                if(obj != null) {
                    JSONObject jsonRoot = (JSONObject)obj;
                    try {
                        int status = jsonRoot.getInt("status");
                        if(status == 0) {
                            completeLoading();
                            Intent intent = getIntent();
                            intent.setData(Uri.parse(HttpConstants.getUserUrl() + "/getPortraitSmall?targetNo=" + AppContext.getUserInfoBean().getUserNo()));
                            this.setResult(RESULT_OK,intent);
                            headImg.setVisibility(View.GONE);
                            menuLayout.setVisibility(View.GONE);
                            originHeadImg.setVisibility(View.VISIBLE);

                            UserInfoBean userInfoBean = AppContext.getUserInfoBean();

                            String imgUrl = HttpConstants.getUserUrl() + "/getPortrait?userNo=" + userInfoBean.getUserNo() + "&token=" + userInfoBean.getToken() + "&targetNo=" + userInfoBean.getUserNo();
                            MemoryCacheUtils.removeFromCache(imgUrl,ImageLoaderUtil.getImageLoaderInstance().getMemoryCache());
                            DiskCacheUtils.removeFromCache(imgUrl,ImageLoaderUtil.getImageLoaderInstance().getDiskCache());

                        } else {
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "回调解析失败", e);
                        e.printStackTrace();
                    }
                }
                txt_confirm.setClickable(true);
                txt_cancel.setClickable(true);
                break;
        }
    }

    @Override
    public void onFailure(Throwable ex, boolean isOnCallback, int accessId, Object inputParameter) {
        switch (accessId) {
            case R.id.upload_head:
                completeLoading();
                Log.w(TAG,"upload portrait fail");
                ex.printStackTrace();
                ToastUtil.showShortToast(this, "上传失败", 1);
                UserInfoBean userInfoBean = AppContext.getUserInfoBean();
                ImageLoaderUtil.getImageLoaderInstance().displayImage(HttpConstants.getUserUrl() + "/getPortrait?userNo=" + userInfoBean.getUserNo() + "&token=" + userInfoBean.getToken() + "&targetNo=" + userInfoBean.getUserNo(), originHeadImg, ImageLoaderUtil.DISPLAY_IMAGE_OPTIONS);
                txt_confirm.setClickable(true);
                txt_cancel.setClickable(true);
                originHeadImg.setVisibility(View.VISIBLE);
                headImg.setVisibility(View.GONE);
                menuLayout.setVisibility(View.GONE);
                System.out.println("accessId:" + accessId + "\r\n isOnCallback:" + isOnCallback);
                Log.e(TAG, "onFailure", ex);
                break;
        }
    }

    @Override
    public void onCancelled(Callback.CancelledException cex) {

    }

    @Override
    public void onFinished() {

    }

}
