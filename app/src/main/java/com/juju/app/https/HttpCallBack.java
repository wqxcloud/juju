package com.juju.app.https;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;

/**
 * 项目名称：juju
 * 类描述：Http回调接口
 * 创建人：gm
 * 日期：2016/2/17 16:46
 * 版本：V1.0.0
 */
public interface HttpCallBack {

    public void onSuccess(ResponseInfo<String> responseInfo, int accessId, Object... obj);

    public void onFailure(HttpException error, String msg, int accessId);


}
