package com.juju.app.helper;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.juju.app.R;
import com.juju.app.adapter.SingleCheckAdapter;
import com.juju.app.bean.CityBean;
import com.juju.app.entity.Party;
import com.juju.app.entity.User;
import com.juju.app.entity.chat.GroupEntity;
import com.juju.app.entity.chat.SearchElement;
import com.juju.app.event.LoginEvent;
import com.juju.app.event.SmackSocketEvent;
import com.juju.app.golobal.DBConstant;
import com.juju.app.utils.Logger;
import com.juju.app.utils.pinyin.PinYinUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class IMUIHelper {



    // 文字高亮显示
    public static void setTextStartHilighted(TextView textView, String text,int endIndex) {
        textView.setText(text);
        if (textView == null
                || TextUtils.isEmpty(text)
                || endIndex < 0) {
            return;
        }

        int startIndex = 0;
        if (startIndex < 0 || endIndex > text.length()) {
            return;
        }
        // 开始高亮处理
        int color =  Color.rgb(255, 255, 255);
        textView.setText(text, BufferType.SPANNABLE);
        Spannable span = (Spannable) textView.getText();
        span.setSpan(new ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    // 文字高亮显示
    public static void setTextHilighted(TextView textView, String text,SearchElement searchElement) {
        textView.setText(text);
        if (textView == null
                || TextUtils.isEmpty(text)
                || searchElement ==null) {
            return;
        }

        int startIndex = searchElement.startIndex;
        int endIndex = searchElement.endIndex;
        if (startIndex < 0 || endIndex > text.length()) {
            return;
        }
        // 开始高亮处理
        int color =  Color.rgb(0, 130, 227);
        textView.setText(text, BufferType.SPANNABLE);
        Spannable span = (Spannable) textView.getText();
        span.setSpan(new ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static boolean handleItemSearch(String key, SingleCheckAdapter.ItemBean itemBean) {
        if (TextUtils.isEmpty(key) || itemBean == null) {
            return false;
        }
        itemBean.getSearchElement().reset();
        return handleTokenFirstCharsSearch(key, itemBean.getPinyinElement(), itemBean.getSearchElement())
                || handleTokenPinyinFullSearch(key, itemBean.getPinyinElement(), itemBean.getSearchElement())
                || handleNameSearch(itemBean.getMainName(), key, itemBean.getSearchElement());

    }


    public static boolean handleContactSearch(String key, User contact) {
        if (TextUtils.isEmpty(key) || contact == null) {
            return false;
        }

        contact.getSearchElement().reset();

        return handleTokenFirstCharsSearch(key, contact.getPinyinElement(), contact.getSearchElement())
                || handleTokenPinyinFullSearch(key, contact.getPinyinElement(), contact.getSearchElement())
                || handleNameSearch(contact.getNickName(), key, contact.getSearchElement());

    }

    public static boolean handleCitySearch(String key, CityBean cityBean) {
        if (TextUtils.isEmpty(key) || cityBean == null) {
            return false;
        }
        cityBean.getSearchElement().reset();
        if(handleNameSearch(cityBean.getCity(), key, cityBean.getSearchElement())){
            return true;
        }
        return false;
    }

    public static boolean handleTokenFirstCharsSearch(String key, PinYinUtil.PinYinElement pinYinElement, SearchElement searchElement) {
        return handleNameSearch(pinYinElement.tokenFirstChars, key.toUpperCase(), searchElement);
    }

    public static boolean handleNameSearch(String name, String key,
                                           SearchElement searchElement) {
        int index = name.indexOf(key);
        if (index == -1) {
            return false;
        }

        searchElement.startIndex = index;
        searchElement.endIndex = index + key.length();

        return true;
    }

    public static boolean handleTokenPinyinFullSearch(String key, PinYinUtil.PinYinElement pinYinElement, SearchElement searchElement) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }

        String searchKey = key.toUpperCase();

        //onLoginOut the old search result
        searchElement.reset();

        int tokenCnt = pinYinElement.tokenPinyinList.size();
        int startIndex = -1;
        int endIndex = -1;

        for (int i = 0; i < tokenCnt; ++i) {
            String tokenPinyin = pinYinElement.tokenPinyinList.get(i);

            int tokenPinyinSize = tokenPinyin.length();
            int searchKeySize = searchKey.length();

            int keyCnt = Math.min(searchKeySize, tokenPinyinSize);
            String keyPart = searchKey.substring(0, keyCnt);

            if (tokenPinyin.startsWith(keyPart)) {

                if (startIndex == -1) {
                    startIndex = i;
                }

                endIndex = i + 1;
            } else {
                continue;
            }

            if (searchKeySize <= tokenPinyinSize) {
                searchKey = "";
                break;
            }

            searchKey = searchKey.substring(keyCnt, searchKeySize);
        }

        if (!searchKey.isEmpty()) {
            return false;
        }

        if (startIndex >= 0 && endIndex > 0) {
            searchElement.startIndex = startIndex;
            searchElement.endIndex = endIndex;

            return true;
        }

        return false;
    }

    public static boolean handleGroupSearch(String key, GroupEntity group) {
        if (TextUtils.isEmpty(key) || group == null) {
            return false;
        }
        group.getSearchElement().reset();

        return handleTokenFirstCharsSearch(key, group.getPinyinElement(), group.getSearchElement())
                || handleTokenPinyinFullSearch(key, group.getPinyinElement(), group.getSearchElement())
                || handleNameSearch(group.getMainName(), key, group.getSearchElement());
    }

    public static boolean handlePartySearch(String key, Party party) {
        if (TextUtils.isEmpty(key) || party == null) {
            return false;
        }
        party.getSearchElement().reset();

        if(handleNameSearch(party.getName(), key, party.getSearchElement())){
            party.setDescMatch(false);
            return true;
        }

        if(handleNameSearch(party.getDesc(), key, party.getSearchElement())){
            party.setDescMatch(true);
            return true;
        }
        return false;
    }

    public static void setViewTouchHightlighted(final View view) {
        if (view == null) {
            return;
        }

        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    view.setBackgroundColor(Color.rgb(1, 175, 244));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.setBackgroundColor(Color.rgb(255, 255, 255));
                }
                return false;
            }
        });
    }

    public static void setEntityImageViewAvatarNoDefaultPortrait(ImageView imageView,
                                                                 String avatarUrl, int sessionType, int roundPixel) {
        setEntityImageViewAvatarImpl(imageView, avatarUrl, sessionType, false, roundPixel);
    }


    public static void setEntityImageViewAvatarImpl(ImageView imageView,
                                                    String avatarUrl, int sessionType, boolean showDefaultPortrait, int roundPixel) {
        if (avatarUrl == null) {
            avatarUrl = "";
        }

        String fullAvatar = getRealAvatarUrl(avatarUrl);
        int defaultResId = -1;

        if (showDefaultPortrait) {
            defaultResId = getDefaultAvatarResId(sessionType);
        }

        displayImage(imageView, fullAvatar, defaultResId, roundPixel);
    }

    /**
     * 如果图片路径是以  http开头,直接返回
     * 如果不是， 需要集合自己的图像路径生成规律
     * @param avatarUrl
     * @return
     */
    public static String getRealAvatarUrl(String avatarUrl) {
        if (avatarUrl.toLowerCase().contains("http")) {
            return avatarUrl;
        } else if (avatarUrl.trim().isEmpty()) {
            return "";
        } else {
            return avatarUrl;
        }
    }

    // 这个还是蛮有用的,方便以后的替换
    public static int getDefaultAvatarResId(int sessionType) {
        if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {
            return R.mipmap.tt_default_user_portrait_corner;
        } else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
            return R.mipmap.group_default;
        } else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
            return R.mipmap.group_default;
        }

        return R.mipmap.tt_default_user_portrait_corner;
    }

    public static void displayImage(ImageView imageView,
                                    String resourceUri, int defaultResId, int roundPixel) {

        Logger logger = Logger.getLogger(IMUIHelper.class);

        logger.d("displayimage#displayImage resourceUri:%s, defeaultResourceId:%d", resourceUri, defaultResId);

        if (resourceUri == null) {
            resourceUri = "";
        }

        boolean showDefaultImage = !(defaultResId <= 0);

        if (TextUtils.isEmpty(resourceUri) && !showDefaultImage) {
            logger.e("displayimage#, unable to display image");
            return;
        }


        DisplayImageOptions options;
        if (showDefaultImage) {
            options = new DisplayImageOptions.Builder().
                    showImageOnLoading(defaultResId).
                    showImageForEmptyUri(defaultResId).
                    showImageOnFail(defaultResId).
                    cacheInMemory(true).
                    cacheOnDisk(true).
                    considerExifParams(true).
                    displayer(new RoundedBitmapDisplayer(roundPixel)).
                    imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
                    bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
                    build();
        } else {
            options = new DisplayImageOptions.Builder().
                    cacheInMemory(true).
                    cacheOnDisk(true).
//			considerExifParams(true).
//			displayer(new RoundedBitmapDisplayer(roundPixel)).
//			imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
//			bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
        build();
        }

        ImageLoader.getInstance().displayImage(resourceUri, imageView, options, null);
    }

    public static void displayImageNoOptions(ImageView imageView,
                                             String resourceUri, int defaultResId, int roundPixel) {

        Logger logger = Logger.getLogger(IMUIHelper.class);

        logger.d("displayimage#displayImage resourceUri:%s, defeaultResourceId:%d", resourceUri, defaultResId);

        if (resourceUri == null) {
            resourceUri = "";
        }

        boolean showDefaultImage = !(defaultResId <= 0);

        if (TextUtils.isEmpty(resourceUri) && !showDefaultImage) {
            logger.e("displayimage#, unable to display image");
            return;
        }

        DisplayImageOptions options;
        if (showDefaultImage) {
            options = new DisplayImageOptions.Builder().
                    showImageOnLoading(defaultResId).
                    showImageForEmptyUri(defaultResId).
                    showImageOnFail(defaultResId).
                    cacheInMemory(true).
                    cacheOnDisk(true).
                    considerExifParams(true).
                    displayer(new RoundedBitmapDisplayer(roundPixel)).
                    imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
                    bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
                    build();
        } else {
            options = new DisplayImageOptions.Builder().
//                    cacheInMemory(true).
//                    cacheOnDisk(true).
        imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
                    bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
                    build();
        }
        ImageLoader.getInstance().displayImage(resourceUri, imageView, options, null);
    }

    // 根据event 展示提醒文案
    public static int getLoginErrorTip(LoginEvent event) {
        switch (event) {
            case LOGIN_AUTH_FAILED:
                return R.string.login_error_general_failed;
            case LOGIN_INNER_FAILED:
                return R.string.login_error_unexpected;
            default :
                return  R.string.login_error_unexpected;
        }
    }
    public static int getSocketErrorTip(SmackSocketEvent event) {
        switch (event) {
            case CONNECT_MSG_SERVER_FAILED :
                return R.string.connect_msg_server_failed;
            default :
                return  R.string.login_error_unexpected;
        }
    }
}
