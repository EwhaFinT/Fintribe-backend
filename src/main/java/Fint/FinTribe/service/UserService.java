package Fint.FinTribe.service;

import Fint.FinTribe.config.SecurityConfig;
import Fint.FinTribe.domain.user.User;
import Fint.FinTribe.domain.user.UserRespository;
import Fint.FinTribe.payload.request.LoginRequest;
import Fint.FinTribe.payload.request.MypageRequest;
import Fint.FinTribe.payload.request.SignupRequest;
import Fint.FinTribe.payload.response.LoginResponse;
import Fint.FinTribe.payload.response.MypageResponse;
import Fint.FinTribe.payload.response.SignupResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRespository userRespository;
    private final SecurityConfig securityConfig;

    // 1. 회원 가입
    public SignupResponse registerUser(SignupRequest signupRequest) {
        // 아이디 중복 검사
        if(findByIdentity(signupRequest.getIdentity()).isPresent()) { return new SignupResponse(0); }
        // 회원 저장
        Object userId = saveUser(signupRequest.getIdentity(), signupRequest.getPassword(), signupRequest.getPhone(), signupRequest.getEmail());
        if(userId != null) return new SignupResponse(1);
        return new SignupResponse(0);
    }

    // 2. 로그인
    public LoginResponse loginUser(LoginRequest loginRequest) {
        Optional<User> user = findByIdentity(loginRequest.getIdentity());
        if(user.isEmpty()) return new LoginResponse(0, "해당 아이디가 존재하지 않습니다."); // 해당 아이디가 존재하지 않음
        if(!securityConfig.passwordEncoder().matches(loginRequest.getPassword(), user.get().getPw())) return new LoginResponse(1, "비밀번호가 일치하지 않습니다."); // 비밀번호가 일치하지 않음
        return new LoginResponse(user.get().getUserId(), "로그인에 성공했습니다.");
    }

    // 3. 지갑 연결


    // 4. 마이페이지
    public MypageResponse myPage(MypageRequest mypageRequest) {
        Optional<User> user = userRespository.findById(mypageRequest.getUserId());
        if(user.isEmpty()) return new MypageResponse(null, null);
        return new MypageResponse(user.get().getWallet(), user.get().getArtId());
    }

    private Optional<User> findByIdentity(String identity) {
        return userRespository.findByIdentity(identity);
    }

    private Object saveUser(String identity, String password, String phone, String email) { // 회원 저장
        String encodedPassword = securityConfig.passwordEncoder().encode(password); // 비밀번호 해싱
        User user = User.builder()
                .userId(new ObjectId())
                .artId(null)
                .identity(identity).pw(encodedPassword)
                .wallet(null).phone(phone).email(email).build();
        return userRespository.save(user); // 회원 저장
    }

    public void deleteAll() { // (단위 테스트용)
        userRespository.deleteAll();
    }
}