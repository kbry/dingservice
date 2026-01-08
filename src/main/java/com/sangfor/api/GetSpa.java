package com.sangfor.api;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.sangfor.common.CommonResponse;
import com.sangfor.common.CommonUtil;
import com.sangfor.common.GroupResponse;
import com.sangfor.vo.SpaVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aliyun.dingtalkoauth2_1_0.models.GetTokenResponse;
import com.aliyun.tea.TeaException;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiV2UserGetRequest;
import com.dingtalk.api.request.OapiV2UserGetuserinfoRequest;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.dingtalk.api.response.OapiV2UserGetuserinfoResponse;
import com.taobao.api.ApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GetSpa {
    private static final Logger log = LoggerFactory.getLogger(GetSpa.class);

    @Value("${dingtalk.clientId}")
    private String clientId;
    @Value("${dingtalk.clientSecret}")
    private String clientSecret;

    public GroupResponse queryGroupByPath(String groupPath) {
        String path = "/api/v1/localUserGroup/queryGroupByPath";
        Map<String, Object> params = new HashMap<>();
        params.put("path", groupPath);

        String response = CommonUtil.openApiGetRequest(path, params);
        System.out.println(response);

        CommonResponse<GroupResponse> commonResponse = JSONUtil.toBean(response, new TypeReference<CommonResponse<GroupResponse>>() {
        }, false);

        return commonResponse.getData();
    }

    @GetMapping("/getspa")
    public ResponseEntity<?> getSpa(@RequestParam String code, @RequestParam String corpId) {
        // 获取用户信息
        ResponseEntity<?> userInfoResponse = getUserInfo(code, corpId);
        
        // 检查是否成功获取用户信息
        if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
            // 如果失败，返回错误信息
            return ResponseEntity.status(userInfoResponse.getStatusCode())
                    .body(null);
        }
        
        // 检查响应体类型
        Object body = userInfoResponse.getBody();
        if (!(body instanceof OapiV2UserGetResponse)) {
            log.error("Unexpected response type: {}", body != null ? body.getClass().getName() : "null");
            return ResponseEntity.internalServerError().body(null);
        }
        
        OapiV2UserGetResponse rspnew = (OapiV2UserGetResponse) body;
        
        // 从用户信息中提取 name 和 mobile
        String name = null;
        String mobile = null;
        if (rspnew.getResult() != null) {
            name = rspnew.getResult().getName();
            mobile = rspnew.getResult().getMobile();
        }
        
        if (name == null || mobile == null) {
            log.error("Failed to extract name or mobile from user info");
            return ResponseEntity.badRequest().body(null);
        }
        
        log.info("name: {} mobile: {}", name, mobile);
        
        GroupResponse groupResponse = queryGroupByPath("/");
        if (StrUtil.isEmpty(groupResponse.getId())) {
            log.error("查询不到当前用户目录，请检查GroupPath");
            return ResponseEntity.badRequest().body(null);
        }
        log.info("Group ID: {}", groupResponse.getId());
        
        String path = "/api/v1/spa/sendSpaCode";
    
        SpaVo spaVo = new SpaVo();
        // spaVo.setName(name);
        List<String> sendMode = new ArrayList<>();
        sendMode.add("sms");
        spaVo.setSendMode(sendMode);
        spaVo.setUserDirectoryName("本地用户目录");
        spaVo.setPhone(mobile);
    

        String response = CommonUtil.openApiPostRequest(path, JSONUtil.toJsonStr(spaVo));
        CommonResponse<List<String>> commonResponse = JSONUtil.toBean(response, new TypeReference<CommonResponse<List<String>>>() {
        }, false);

        if (!StrUtil.equals(commonResponse.getMsg(), "200")) {
            log.error("SPA request failed: {}", commonResponse.getMsg());
            // return ResponseEntity.badRequest().body(null);
        }
        
        log.info("SPA request success: {}", commonResponse.getMsg());
        return  userInfoResponse;
    }

    public ResponseEntity<?> getUserInfo(@RequestParam String code, @RequestParam String corpId) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/getuserinfo");
        OapiV2UserGetuserinfoRequest req = new OapiV2UserGetuserinfoRequest();
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Code cannot be empty");
        }
        if (corpId == null || corpId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("CorpId cannot be empty");
        }
        req.setCode(code);
        String accessToken = null;
        try {
             accessToken = getAccessToken(clientId, clientSecret,corpId);
            if (accessToken == null) {
                return ResponseEntity.internalServerError().body("Failed to get access token");
            }
            log.info("Access token: {}", accessToken);
            OapiV2UserGetuserinfoResponse rsp = client.execute(req, accessToken);
            if (!rsp.isSuccess()) {
                log.error("Failed to get user info: {}", rsp.getErrmsg());
                return ResponseEntity.internalServerError().body(rsp.getErrmsg());
            }

            log.info("Successfully got user info: {}", rsp.getBody());
            
            // 从响应中提取 userid
            String userid = null;
            if (rsp.getResult() != null) {
                userid = rsp.getResult().getUserid();
            }
            
            if (userid == null || userid.trim().isEmpty()) {
                log.error("Failed to extract userid from response");
                return ResponseEntity.internalServerError().body("Failed to extract userid from response");
            }
            
            log.info("Extracted userid: {}", userid);

            DingTalkClient clientnew = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/get");
            OapiV2UserGetRequest reqnew = new OapiV2UserGetRequest();
            reqnew.setUserid(userid);
            reqnew.setLanguage("zh_CN");
            OapiV2UserGetResponse rspnew = clientnew.execute(reqnew, accessToken);
            
            if (!rspnew.isSuccess()) {
                log.error("Failed to get user details: {}", rspnew.getErrmsg());
                return ResponseEntity.internalServerError().body(rspnew.getErrmsg());
            }
            
            log.info("Successfully got user info: {}", rspnew.getBody());
            
            return ResponseEntity.ok(rspnew);
        } catch (ApiException e) {
            log.error("Error getting user info: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to get user info: " + e.getMessage());
        }
    }

    private String getAccessToken(String clientId, String clientSecret, String corpId) {

        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.protocol = "https";
        config.regionId = "central";

        try {
            com.aliyun.dingtalkoauth2_1_0.Client client = new com.aliyun.dingtalkoauth2_1_0.Client(config);
            com.aliyun.dingtalkoauth2_1_0.models.GetTokenRequest getTokenRequest = new com.aliyun.dingtalkoauth2_1_0.models.GetTokenRequest()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setGrantType("client_credentials");
             GetTokenResponse response = client.getToken(corpId, getTokenRequest);
             return response.getBody().accessToken;
        } catch (TeaException err) {
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                log.error("Error getting access token: {}", err.getMessage());
            }

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                log.error("Error getting access token: {}", err.getMessage());
            }
        }
        return null;
    }

}
