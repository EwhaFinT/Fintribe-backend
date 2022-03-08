package Fint.FinTribe.service;

import Fint.FinTribe.config.SecurityConfig;
import Fint.FinTribe.domain.user.User;
import Fint.FinTribe.domain.user.UserRespository;
import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

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
        Object userId = saveUser(signupRequest.getIdentity(), signupRequest.getPassword(), signupRequest.getName(), signupRequest.getPhone(), signupRequest.getEmail());
        if(userId != null) return new SignupResponse(1);
        return new SignupResponse(0);
    }

    // 2. 로그인
    public LoginResponse loginUser(LoginRequest loginRequest) {
        Optional<User> user = findByIdentity(loginRequest.getIdentity());
        if(user.isEmpty()) return new LoginResponse(null, "해당 아이디가 존재하지 않습니다."); // 해당 아이디가 존재하지 않음
        if(!securityConfig.passwordEncoder().matches(loginRequest.getPassword(), user.get().getPw())) return new LoginResponse(null, "비밀번호가 일치하지 않습니다."); // 비밀번호가 일치하지 않음
        return new LoginResponse(user.get().getUserId(), "로그인에 성공했습니다.");
    }

    // 3. 지갑 연결


    // 4. 마이페이지
    public MypageResponse myPage(MypageRequest mypageRequest) {
        Optional<User> user = userRespository.findById(mypageRequest.getUserId());
        if(user.isEmpty()) return new MypageResponse(null, null);
        return new MypageResponse(user.get().getWallet(), user.get().getArtId());
    }

    // 5. 아이디 찾기
    public FindIdResponse findId(FindIdRequest findIdRequest) {
        Optional<User> user = userRespository.findByNameAndPhone(findIdRequest.getName(), findIdRequest.getPhone());
        if(user.isEmpty()) return new FindIdResponse(null); // 해당 정보와 일치하는 사용자 존재하지 않음
        return new FindIdResponse(user.get().getIdentity());
    }

    // 6. 비밀번호 찾기
    public FindPwResponse findPw(FindPwRequest findPwRequest) {
        Optional<User> user = userRespository.findByIdentityAndEmail(findPwRequest.getIdentity(), findPwRequest.getEmail());
        if(user.isEmpty()) return new FindPwResponse(false); // 해당 정보와 일치하는 사용자 존재하지 않음
        String tempPassword = makeTempPassword();
        updatePassword(tempPassword, user.get());
        String emailText = user.get().getName() + "님의 임시 비밀번호는 [" + tempPassword + "] 입니다.";
        SimpleMailMessage message = makeEmailForm(findPwRequest.getEmail(), "[FinTribe: 비밀번호 찾기]", emailText);
        javaMailSender.send(message);
        return new FindPwResponse(true);
    }

    private Optional<User> findByUserId(ObjectId userId) { return userRespository.findById(userId); }

    private Optional<User> findByIdentity(String identity) {
        return userRespository.findByIdentity(identity);
    }

    private Object saveUser(String identity, String password, String name, String phone, String email) { // 회원 저장
        String encodedPassword = securityConfig.passwordEncoder().encode(password); // 비밀번호 해싱
        User user = User.builder()
                .userId(new ObjectId())
                .artId(null)
                .identity(identity).pw(encodedPassword).name(name)
                .wallet(null).phone(phone).email(email).build();
        return userRespository.save(user); // 회원 저장
    }

    // 메일 형식 만들기
    private SimpleMailMessage makeEmailForm(String to, String title, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("serena35@ewhain.net"); // ==== 메일 주소 수정 필요 ====
        message.setSubject(title);
        message.setText(text);
        return message;
    }

    // 임시 비밀번호 만들기
    private String makeTempPassword() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    // 비밀번호 업데이트
    private Object updatePassword(String tempPassword, User user) {
        user.setPw(tempPassword);
        return userRespository.save(user);
    }

    public void deleteAll() { // (단위 테스트용)
        userRespository.deleteAll();
    }
}