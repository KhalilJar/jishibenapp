package com.example.bookkeeping.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 这是一个 “通用 API 响应包装器”（也叫 Result、R、Response）。
 *
 * 它的使命只有一个：统一你后端返回给前端的 JSON 格式。
 * 不管接口成功还是失败，前端收到的 JSON 永远是这种形状：
 * {
 *   "code": 200,
 *   "message": "ok",
 *   "data": { ... }   // 这里的数据类型可以变
 * }
 *
 * @param <T>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private int code;       // 表示状态码
    private String message;  // 提示信息
    private T data;         // 响应数据（用泛型，可以是任何类型）

    /**
     * 返回了一个“三个属性都赋值了的 ApiResponse 实例”
     * 交给 Spring MVC，Spring MVC 会把它自动序列化成 JSON 字符串返回给前端。
     */
    // 快捷静态方法：成功
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "ok", data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "ok", null);
    }

    // 快捷静态方法： 失败
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

}
