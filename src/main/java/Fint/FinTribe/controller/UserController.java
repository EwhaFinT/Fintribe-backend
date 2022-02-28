package Fint.FinTribe.controller;

import Fint.FinTribe.payload.request.LoginRequest;
import Fint.FinTribe.payload.request.MypageRequest;
import Fint.FinTribe.payload.request.SignupRequest;
import Fint.FinTribe.payload.response.LoginResponse;
import Fint.FinTribe.payload.response.MypageResponse;
import Fint.FinTribe.payload.response.SignupResponse;
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
}
