package com.sangfor.common;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CommonUtil {

    private static final Logger log = LoggerFactory.getLogger(CommonUtil.class);
    private static ConsoleCrypto consoleCrypto = null;

    private static String getRequestPath (String path) {
        return Config.consoleAddress + path;
    }


    /**
     * 获取控制台公钥信息
     * @return ConsoleCrypto
     */
    public static ConsoleCrypto getConsoleCrypto() {
        if (consoleCrypto == null) {
            String path = "/api/v1/admin/getConfig";

            String res = HttpUtil.get(getRequestPath(path));
            CommonResponse<ConsoleResponse> commonResponse = JSONUtil.toBean(res, new TypeReference<CommonResponse<ConsoleResponse>>() {
            }, false);
            consoleCrypto = commonResponse.getData().getCrypto();
        }
        return consoleCrypto;
    }

    public static String calSignature(String key, String urlString, Map<String, String> query, String body) {
        try {
            // 从url中解析path路径
            URL url = new URL(urlString);
            String path = url.getPath();

            // 按规则拼接query参数的kv_str("k=v")
            StringBuilder paramsBuilder = new StringBuilder();
            if (query != null) {
                // 使用TreeMap自动排序
                TreeMap<String, String> sortedQuery = new TreeMap<>(query);
                for (Map.Entry<String, String> entry : sortedQuery.entrySet()) {
                    paramsBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                // 移除最后一个"&"
                if (!paramsBuilder.isEmpty()) {
                    paramsBuilder.setLength(paramsBuilder.length() - 1);
                }
            }

            // 准备body参数，严格json字符串
            if (body != null) {
                paramsBuilder.append(body);
            }

            // 拼接签名字符串
            String strToSign = path;
            if (!paramsBuilder.isEmpty()) {
                strToSign += "?" + paramsBuilder.toString();
            }

            // 签名
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(strToSign.getBytes(StandardCharsets.UTF_8));

            // 将签名转换为十六进制字符串
            return bytesToHex(hash);
        } catch (Exception e) {
            log.error("Error calculating signature: {}", e.getMessage(), e);
            return null;
        }
    }

    public static Map<String, String> convertToStringMap(Map<String, Object> objectMap) {
        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            // 将Object转换为String，这里假设Object的toString方法能够合理地转换为String
            // 如果Object是null，那么在Map中对应的值将是null字符串
            stringMap.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "null");
        }
        return stringMap;
    }

    public static Map<String, String> prepareAuthHeaders(String urlString, Map<String, String> query, String body) {
        // 获取十位时间戳
        String timestamp = Long.toString(System.currentTimeMillis() / 1000);

        // 使用UUID生成随机数
        String nonce = UUID.randomUUID().toString();

        // 按照规则拼接签名密钥
        String key = String.format("appId=%s&appSecret=%s&timestamp=%s&nonce=%s", Config.apiId, Config.apiSecret, timestamp, nonce);
        String sign = calSignature(key, urlString, query, body);

        // 创建头信息的Map
        Map<String, String> headers = new HashMap<>();
        headers.put("x-ca-key", Config.apiId);
        headers.put("x-ca-timestamp", timestamp);
        headers.put("x-ca-nonce", nonce);
        headers.put("x-ca-sign", sign);

        return headers;
    }

    public static String encodeRSA(String content, String modulusHex, String exponent) {
        try {
            // 将十六进制的模数和指数转换为BigInteger
            BigInteger modulus = new BigInteger(modulusHex, 16);
            BigInteger publicExponent = new BigInteger(exponent);

            // 创建公钥规范
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);

            // 获取公钥对象
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = factory.generatePublic(spec);

            // 创建Cipher对象并初始化为加密模式
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            // 执行加密操作
            byte[] encryptedData = cipher.doFinal(content.getBytes());

            // 将加密后的数据转换为十六进制字符串
            return bytesToHex(encryptedData);
        } catch (Exception e) {
            log.error("Error encoding RSA: {}", e.getMessage(), e);
            return "";
        }
    }

    public static String encrypt(String password) {
        ConsoleCrypto consoleCrypto = getConsoleCrypto();

        String message = password + "_" + consoleCrypto.getAntiReplayRand();

        return encodeRSA(message, consoleCrypto.getPubKey(), consoleCrypto.getPubKeyExp());
    }

    // 辅助方法：将字节数组转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String openApiGetRequest(String path, Map<String, Object> params) {
        String requestUrl = getRequestPath(path);
        Map<String, String> paramsMap = convertToStringMap(params);
        Map<String, String> headersMap = prepareAuthHeaders(requestUrl, paramsMap, null);
        HttpRequest request = HttpUtil.createGet(requestUrl)
                .form(params)
                .addHeaders(headersMap);
        HttpResponse response = request.execute();
        return response.body();
    }

    public static String openApiPostRequest(String path, String body) {
        String requestUrl = getRequestPath(path);
        Map<String, String> headersMap = prepareAuthHeaders(requestUrl, null, body);
        HttpRequest request = HttpUtil.createPost(requestUrl)
                .body(body)
                .addHeaders(headersMap);
        HttpResponse response = request.execute();
        return response.body();
    }
}
