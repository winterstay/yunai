package com.yunai.yunai.exception;

public class ThrowUtils {
    //条件成立就抛出异常
    public static void throwIf(boolean condition, RuntimeException runtimeException){
        if(condition){
            throw runtimeException;
        }
    }

    public static void throwIf(boolean condition, ErrorCode errorCode){
        throwIf(condition, new BusinessException(errorCode));
    }

    public static void throwIf(boolean condition, ErrorCode errorCode, String message){
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
