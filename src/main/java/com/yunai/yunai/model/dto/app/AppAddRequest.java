package com.yunai.yunai.model.dto.app;

import java.io.Serializable;

import lombok.Data;

@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用初始化 prompt
     */
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}
