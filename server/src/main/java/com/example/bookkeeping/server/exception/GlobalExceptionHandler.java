package com.example.bookkeeping.server.exception;

import com.example.bookkeeping.server.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * 意思：拦截所有 Controller 抛出的异常，把处理结果转成 JSON 写回响应
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 处理未登录 / Token 无效异常 → 返回 401
     *
     * @ExceptionHandler 指定这个方法处理哪种异常
     * @ResponseStatus 指定返回的 HTTP 状态码
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)  // 枚举，代表401
    public ApiResponse<Void> handleUnauthrized(UnauthorizedException e) {
        return ApiResponse.error(401, e.getMessage());
    }

    /**
     * 处理业务异常 → 返回 400
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    /**
     * 兜底方法，抓漏网之鱼  ->  return 500
     * 避免直接把 异常堆栈暴露给前端
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> hadleException(Exception e) {
      log.error("出错了：" + e); //打印完整堆栈给自己看，  放 e  而不是 e.getMessage()
      return ApiResponse.error(500, "是预料之外的错误呢");  //返回给前端 一条报错信息和 状态码就行
    }

}

