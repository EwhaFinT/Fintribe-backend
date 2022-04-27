package Fint.FinTribe.controller;

import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import Fint.FinTribe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
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
    public ResponseEntity<?> loginUser(@Valid @RequestParam("identity") String identity, @Valid @RequestParam("password") String password) {
        LoginResponse loginResponse = userService.loginUser(identity, password);
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    // 3. 지갑 연결
    @PostMapping("/wallet")
    public ResponseEntity<?> registerMyWallet(@Valid @RequestBody RegisterWalletRequest registerWalletRequest) {
        RegisterWalletResponse registerWalletResponse = userService.registerWallet(registerWalletRequest);
        return new ResponseEntity<>(registerWalletResponse, HttpStatus.OK);
    }

    // 4. 마이페이지
    @GetMapping("/mypage")
    public ResponseEntity<?> myPage(@Valid @RequestParam("userId") String userId) {
        MypageResponse mypageResponse = userService.myPage(new ObjectId(userId));
        return new ResponseEntity<>(mypageResponse, HttpStatus.OK);
    }

    // 5. 아이디 찾기
    @GetMapping("/find-id")
    public ResponseEntity<?> findId(@Valid @RequestParam("name") String name, @Valid @RequestParam("phone") String phone) {
        FindIdResponse findIdResponse = userService.findId(name, phone);
        return new ResponseEntity<>(findIdResponse, HttpStatus.OK);
    }

    // 6. 비밀번호 찾기
    @GetMapping("/find-pw")
    public ResponseEntity<?> findPw(@Valid @RequestParam("identity") String identity, @Valid @RequestParam("email") String email) {
        FindPwResponse findPwResponse = userService.findPw(identity, email);
        return new ResponseEntity<>(findPwResponse, HttpStatus.OK);
    }

    // 7. 아이디 중복 조회
    @GetMapping("/check-id")
    public ResponseEntity<?> idCheck(@Valid @RequestParam("identity") String identity) {
        IdCheckResponse idCheckResponse = userService.idCheck(identity);
        return new ResponseEntity<>(idCheckResponse, HttpStatus.OK);
    }
}