package edu.npu.controller;

import edu.npu.dto.CheckSmsCodeDto;
import edu.npu.dto.FaceVerificationDto;
import edu.npu.dto.UserLoginDto;
import edu.npu.dto.UserRegisterDto;
import edu.npu.entity.LoginAccount;
import edu.npu.service.FaceService;
import edu.npu.service.LoginAccountService;
import edu.npu.service.OcrService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author : [wangminan]
 * @description : [用于处理基础注册、用户名密码登录、退出的接口]
 */
@RestController
@Slf4j
public class BasicAuthController {

    @Resource
    private LoginAccountService loginAccountService;

    @Resource
    private OcrService ocrService;

    @Resource
    private FaceService faceService;

    /**
     * 用户注册接口
     * @param userRegisterDto 用户注册信息
     * @return R
     */
    @PostMapping("/register/user")
    public R register(@RequestBody @Validated UserRegisterDto userRegisterDto){
        return loginAccountService.registerUser(userRegisterDto);
    }

    /**
     * 接收formData格式的请求 校验身份证
     *
     * @return R
     */
    @PostMapping("/register/ocr/idCard")
    public R ocrIdCard(@RequestPart(value = "card") MultipartFile file){
        return ocrService.ocrIdCard(file);
    }

    /**
     * 用户名密码登录接口
     * @param userLoginDto 用户登录信息
     * @return R
     */
    @PostMapping("/login/password")
    public R login(@RequestBody @Validated UserLoginDto userLoginDto){
        return loginAccountService.login(userLoginDto);
    }

    @PostMapping("/login/phone")
    public R loginByPhone(@RequestBody @Validated CheckSmsCodeDto checkSmsCodeDto){
        return loginAccountService.loginByPhone(checkSmsCodeDto);
    }

    /**
     * 用户退出登录接口
     * @param loginAccount 用户登录信息
     * @return R
     */
    @PostMapping("/loginuser/logout") // 直接请求logout会被spring security的页面302
    public R logout(@AuthenticationPrincipal LoginAccount loginAccount){
        return loginAccountService.logout(loginAccount);
    }

    @PostMapping("/faceVerification")
    public R faceVerification(@RequestBody @Validated FaceVerificationDto verificationDto){
        return faceService.personIdVerification(verificationDto);
    }
}
