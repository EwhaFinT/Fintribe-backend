package Fint.FinTribe.service;

import Fint.FinTribe.config.SecurityConfig;
import Fint.FinTribe.domain.user.User;
import Fint.FinTribe.domain.user.UserRespository;
import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.groundx.caver_ext_kas.CaverExtKAS;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.ApiException;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.api.wallet.model.Account;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final String chainId = "1001";
    private final String accessKeyId = "KASK489KAHY54740WDAAL1PU";
    private final String secretAccessKey = "KcCPXC2EiGze7svsh0v1w7tlnb9e-q23QoUW4yWs";
    private final UserRespository userRespository;
    private final SecurityConfig securityConfig;
    private final JavaMailSender javaMailSender;

    // 1. 회원 가입
    public SignupResponse registerUser(SignupRequest signupRequest) {
        // 아이디 중복 검사
        if(findByIdentity(signupRequest.getIdentity()).isPresent()) { return new SignupResponse(0); }
        // 회원 저장
        Object userId = saveUser(signupRequest.getIdentity(), signupRequest.getPassword(), signupRequest.getName(), signupRequest.getPhone(), signupRequest.getEmail());
        Optional<User> user = userRespository.findByIdentity(signupRequest.getIdentity());
        updateWallet(user.get(), makeWallet());
        if(userId != null) return new SignupResponse(1);
        return new SignupResponse(0);
    }

    // 2. 로그인
    public LoginResponse loginUser(String identity, String password) {
        Optional<User> user = findByIdentity(identity);
        if(user.isEmpty()) return new LoginResponse(null, "해당 아이디가 존재하지 않습니다."); // 해당 아이디가 존재하지 않음
        if(!securityConfig.passwordEncoder().matches(password, user.get().getPw())) return new LoginResponse(null, "비밀번호가 일치하지 않습니다."); // 비밀번호가 일치하지 않음
        return new LoginResponse(user.get().getUserId().toString(), "로그인에 성공했습니다.");
    }

    // 3. 마이페이지
    public MypageResponse myPage(ObjectId userId) {
        Optional<User> user = userRespository.findById(userId);
        if(user.isEmpty()) return new MypageResponse(null, null);
        return new MypageResponse(user.get().getWallet(), user.get().getArtId());
    }

    // 4. 아이디 찾기
    public FindIdResponse findId(String name, String phone) {
        Optional<User> user = userRespository.findByNameAndPhone(name, phone);
        if(user.isEmpty()) return new FindIdResponse(null); // 해당 정보와 일치하는 사용자 존재하지 않음
        return new FindIdResponse(user.get().getIdentity());
    }

    // 5. 비밀번호 찾기
    public FindPwResponse findPw(String identity, String email) {
        Optional<User> user = userRespository.findByIdentityAndEmail(identity, email);
        if(user.isEmpty()) return new FindPwResponse(false); // 해당 정보와 일치하는 사용자 존재하지 않음
        String tempPassword = makeTempPassword();
        updatePassword(tempPassword, user.get());
        String emailText = user.get().getName() + "님의 임시 비밀번호는 [" + tempPassword + "] 입니다.";
        SimpleMailMessage message = makeEmailForm(email, "[FinTribe: 비밀번호 찾기]", emailText);
        javaMailSender.send(message);
        return new FindPwResponse(true);
    }

    // 6. 아이디 중복 검사
    public IdCheckResponse idCheck(String identity) {
        if(findByIdentity(identity).isPresent()) return new IdCheckResponse(0);
        return new IdCheckResponse(1);
    }

    public Optional<User> findByUserId(ObjectId userId) { return userRespository.findById(userId); }

    private Optional<User> findByIdentity(String identity) {
        return userRespository.findByIdentity(identity);
    }

    public List<ObjectId> getArtId(ObjectId userId){
        Optional<User> user = this.findByUserId(userId);
        if(user.isPresent()){
            return user.get().getArtId();
        }
        else {
            return new ArrayList<>();
        }
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
        message.setFrom("ewhafint@gmail.com");
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
        String encodedPassword = securityConfig.passwordEncoder().encode(tempPassword); // 비밀번호 해싱
        user.setPw(encodedPassword);
        return userRespository.save(user);
    }

    // 지갑 주소 생성
    private String makeWallet() {
        CaverExtKAS caver = new CaverExtKAS();
        caver.initKASAPI(chainId, accessKeyId, secretAccessKey);

        String newWallet = null;
        try{
            Account account = caver.kas.wallet.createAccount();
            newWallet = account.getAddress();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return newWallet;
    }

    // 지갑 업데이트
    private Object updateWallet(User user, String wallet) {
        user.setWallet(wallet);
        return userRespository.save(user);
    }
}