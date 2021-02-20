package com.fixbug.controller;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * @Classname NRpcContriller
 * @Date 2020/10/23 14:26
 * @Created by Fbl
 * @Description TODO
 */

public class NRpcController implements RpcController {
    private boolean isFailed;
    private  String failedInfo;

    @Override
    public void reset() {
        isFailed = false;
        failedInfo = "";
    }

    @Override
    public boolean failed() {
        return isFailed;
    }

    @Override
    public String errorText() {
        return failedInfo;
    }

    @Override
    public void startCancel() {

    }

    @Override
    public void setFailed(String s) {
        failedInfo = s;
        isFailed = true;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void notifyOnCancel(RpcCallback<Object> rpcCallback) {

    }
}
