package com.sangfor.common;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {
    
    @Value("${sangfor.console.address}")
    private String consoleAddressValue;
    
    @Value("${sangfor.api.id}")
    private String apiIdValue;
    
    @Value("${sangfor.api.secret}")
    private String apiSecretValue;
    
    // 保留静态字段以兼容现有代码
    public static String consoleAddress;
    public static String apiId;
    public static String apiSecret;
    
    @PostConstruct
    public void init() {
        // 将注入的值赋给静态字段
        consoleAddress = consoleAddressValue;
        apiId = apiIdValue;
        apiSecret = apiSecretValue;
    }
    
    // 提供实例方法以便在Spring管理的Bean中使用
    public String getConsoleAddress() {
        return consoleAddressValue;
    }
    
    public String getApiId() {
        return apiIdValue;
    }
    
    public String getApiSecret() {
        return apiSecretValue;
    }
}
