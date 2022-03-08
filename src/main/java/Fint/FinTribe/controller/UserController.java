package Fint.FinTribe.controller;

import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import Fint.FinTribe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 1. 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        SignupResponse signupResponse = userService.registerUser(signupRequest);
        return new ResponseEntity<>(signupResponse, HttpStatus.OK);
    }

    // 2. 로그인
    @GetMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.loginUser(loginRequest);
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    // 3. 지갑 연결
    // ==== 어떻게 구현해야 할지 잘 모르겠다 ====
    @PostMapping("/wallet")
    public ResponseEntity<?> registerMyWallet() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 4. 마이페이지
    @PostMapping("/mypage")
    public ResponseEntity<?> myPage(@Valid @RequestBody MypageRequest mypageRequest) {
        MypageResponse mypageResponse = userService.myPage(mypageRequest);
        return new ResponseEntity<>(mypageResponse, HttpStatus.OK);
    }

    // 5. 아이디 찾기
    @PostMapping("/findId")
    public ResponseEntity<?> findId(@Valid @RequestBody FindIdRequest findIdRequest) {
        FindIdResponse findIdResponse = userService.findId(findIdRequest);
        return new ResponseEntity<>(findIdResponse, HttpStatus.OK);
    }

    // 6. 비밀번호 찾기
    @PostMapping("/findPw")
    public ResponseEntity<?> findPw(@Valid @RequestBody FindPwRequest findPwRequest) {
        FindPwResponse findPwResponse = userService.findPw(findPwRequest);
        return new ResponseEntity<>(findPwResponse, HttpStatus.OK);
    }
}