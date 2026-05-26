package com.yunai.yunai.model.dto.chatHistory;

import java.io.Serializable;

import com.yunai.yunai.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型：user / ai
     */
    private String messageType;

    private static final long serialVersionUID = 1L;
}
