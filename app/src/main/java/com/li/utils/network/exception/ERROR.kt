package com.li.utils.network.exception

/**
 * 网络错误类，对应 接口 HTTP 的状态码
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/02
 */
enum class ERROR(val code: Int, val msg: String) {

    /**
     * 由于被认为是客户端错误（例如，错误的请求语法、无效的请求消息帧或欺骗性的请求路由），服务器无法或不会处理请求。
     * */
    BAD_REQUEST(400, "客户端错误"),


    /**
     * 当前请求需要用户验证
     */
    UNAUTHORIZED(401, "当前请求需要用户验证"),


    /**
     * 客户端没有访问内容的权限
     * */
    FORBIDDEN(403, "客户端没有访问内容的权限"),


    /**
     * 无法找到指定位置的资源
     */
    NOT_FOUND(404, "无法找到指定位置的资源"),


    /**
     * 服务器遇到了意料不到的情况，不能完成客户的请求
     */
    SERVER_ERROR(500, "服务器错误"),

    /**
     * 服务器不支持请求的功能，无法完成请求
     * */
    NOT_IMPLEMENTED(501, "服务器不支持请求的功能，无法完成请求"),


    /**
     * 由于超载或系统维护，服务器暂时的无法处理客户端的请求
     * */
    SERVICE_UNAVAILABLE(502, "服务器暂时无法处理客户端的请求"),

    /**
     * 未知错误
     */
    UNKNOWN(1000, "未知错误"),

    /**
     * 解析错误
     */
    PARSE_ERROR(1001, "解析错误"),

    /**
     * 网络错误
     */
    NETWORK_ERROR(1002, "网络异常，请尝试刷新"),

    /**
     * 协议出错
     */
    HTTP_ERROR(1003, "404 Not Found"),

    /**
     * 证书出错
     */
    SSL_ERROR(1004, "证书出错"),

    /**
     * 连接超时
     */
    TIMEOUT_ERROR(1005, "连接超时"),

    /**
     * 未知 Host
     */
    UNKNOWN_HOST(1006, "未知Host");
}