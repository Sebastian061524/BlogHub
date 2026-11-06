package com.sebastian.bloghub.common;

import lombok.Data;

/**
 * 统一的 JSON 响应体，由 Controller 和异常处理器共同使用。
 * 泛型 T 表示成功响应中的业务数据类型，失败时 data 为 null。
 * status 是业务结果标记（1 成功、0 失败），与 HTTP 的 200、400、401 等状态码分属两层。
 * {@code @Data} 由 Lombok 在编译时生成访问器等方法，供业务代码取值和 JSON 序列化使用。
 */
@Data
public class Result<T> {
    // 响应体中的业务状态；真正的 HTTP 状态由 ResponseEntity 或 MVC 默认行为决定。
    private int status;
    private String message;
    private T data;

    /**
     * 把调用方提供的业务状态、展示消息和数据组合为响应体；构造器本身不设置 HTTP 状态。
     */
    public Result(int status, String message, T data){
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * 将服务层返回的数据或 Controller 组装的 VO 包装为成功结果，T 由调用位置推断。
     */
    public static <T> Result<T> success(String message, T data){
        return new Result<>(1, message, data);
    }

    /**
     * 将可对外展示的错误消息包装为失败结果；Void 表示没有业务数据。
     */
    public static Result<Void> error(String message){
        return new Result<>(0, message, null);
    }


}

