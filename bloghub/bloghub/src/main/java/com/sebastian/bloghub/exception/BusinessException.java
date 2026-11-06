package com.sebastian.bloghub.exception;

/**
 * 承载可向客户端展示的业务失败原因与 HTTP 状态，供业务层、校验器和拦截器主动抛出。
 * 继承 RuntimeException 便于统一异常处理，也使事务中的该异常默认触发回滚。
 * 典型状态：400 输入不合法、401 未登录或凭证无效、403 已登录但无权限、404 资源不存在。
 */
public class BusinessException extends RuntimeException{

    private final int httpStatus;

    /**
     * 提供给全局异常处理器选择 HTTP 响应状态；该数值不同于 Result 内的 0/1 状态。
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * 调用方传入 HTTP 状态和可公开的业务消息；消息交给父类保存，状态单独保留。
     */
    public BusinessException(int httpStatus, String message){
        super(message);
        this.httpStatus = httpStatus;
    }

}
