package Fint.FinTribe.service;

import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import org.assertj.core.api.Assertions;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("회원 가입 성공 테스트")
    void registerUserSuccess() {
        SignupRequest signupRequest = new SignupRequest("identity", "password", "name", "010-1886-1886", "ewhafint@gmail.com");
        SignupResponse signupResponse = userService.registerUser(signupRequest);
        Assertions.assertThat(signupResponse.getSignupSuccess()).isEqualTo(1);
    }

    @Test
    @DisplayName("회원 가입 실패 테스트 - 이미 존재하는 아이디")
    void registerUserFail() {
        SignupRequest signupRequest = new SignupRequest("identity", "password", "name", "010-1886-1886", "ewhafint@gmail.com");
        userService.registerUser(signupRequest);
        SignupResponse signupResponse = userService.registerUser(signupRequest);
        Assertions.assertThat(signupResponse.getSignupSuccess()).isEqualTo(0);
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginUserSuccess() {
        LoginResponse loginResponse = userService.loginUser("identity", "password");
        Assertions.assertThat(loginResponse.getUserId()).isNotEqualTo(null);
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 존재하지 않는 회원")
    void loginUserFail() {
        LoginResponse loginResponse = userService.loginUser("identity", "password");
        Assertions.assertThat(loginResponse.getUserId()).isEqualTo(null);
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 비밀번호 불일치")
    void loginUserFail2() {
        LoginResponse loginResponse = userService.loginUser("identity", "password2");
        Assertions.assertThat(loginResponse.getUserId()).isEqualTo(null);
    }

    @Test
    @DisplayName("마이페이지 테스트")
    void myPage() {
        ObjectId userId = userService.loginUser("identity", "password").getUserId();
        MypageResponse mypageResponse = userService.myPage(userId);
        Assertions.assertThat(mypageResponse);
    }

    @Test
    @DisplayName("아이디 찾기 성공 테스트")
    void findIdSuccess() {
        FindIdResponse findIdResponse = userService.findId("name", "010-1886-1886");
        System.out.println(findIdResponse.getIdentity());
        Assertions.assertThat(findIdResponse.getIdentity()).isEqualTo("identity");
    }

    @Test
    @DisplayName("아이디 찾기 실패 테스트")
    void findIdFail() {
        FindIdResponse findIdResponse = userService.findId("name_", "010-1886-1886");
        Assertions.assertThat(findIdResponse.getIdentity()).isEqualTo(null);
    }

    @Test
    @DisplayName("비밀번호 찾기 성공 테스트")
    void findPwSuccess() {
        FindPwResponse findPwResponse = userService.findPw("identity", "serena35@ewhain.net");
        Assertions.assertThat(findPwResponse.isEmailSuccess()).isEqualTo(true);
    }

    @Test
    @DisplayName("비밀번호 찾기 실패 테스트")
    void findPwFail() {
        FindPwResponse findPwResponse = userService.findPw("identity2", "serena35@ewhain.net");
        Assertions.assertThat(findPwResponse.isEmailSuccess()).isEqualTo(false);
    }

    @Test
    @DisplayName("지갑 연결 성공 테스트")
    void registerWalletSuccess() {
        RegisterWalletRequest registerWalletRequest = new RegisterWalletRequest(new ObjectId("6231e6bc750b95639ca42496"), "0");
        RegisterWalletResponse registerWalletResponse = userService.registerWallet(registerWalletRequest);
        Assertions.assertThat(registerWalletResponse.getRegisterSuccess()).isEqualTo(1);
    }

    @Test
    @DisplayName("지갑 연결 실패 테스트")
    void registerWalletFail() {
        RegisterWalletRequest registerWalletRequest = new RegisterWalletRequest(new ObjectId(), "0");
        RegisterWalletResponse registerWalletResponse = userService.registerWallet(registerWalletRequest);
        Assertions.assertThat(registerWalletResponse.getRegisterSuccess()).isEqualTo(0);
    }
}