/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state;

/**
 * 任务编排状态枚举（9状态）。
 *
 * @author g00940940
 * @since 2025-10-24
 */
public enum TaskState {
    START_VALIDATION,
    START_TRAINING_DIALING,
    START_MODEL_TRAIN,
    START_MODEL_REPLAY,
    START_GRAY_VALIDATION,
    START_FULL_REPLAY,
    START_WHITELIST,
    START_FULL_RELEASE,
    FINAL
}


