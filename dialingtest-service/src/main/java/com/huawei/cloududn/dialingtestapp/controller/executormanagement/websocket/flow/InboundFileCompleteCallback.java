/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.flow;

/**
 * 文件接收完成回调接口
 * 业务层实现此接口以接收文件完成通知
 *
 * @author g00940940
 * @since 2025-11-14
 */
public interface InboundFileCompleteCallback {

    /**
     * 文件接收完成
     *
     * @param state 文件接收状态
     */
    void onInboundFileComplete(InboundFileState state);
}

