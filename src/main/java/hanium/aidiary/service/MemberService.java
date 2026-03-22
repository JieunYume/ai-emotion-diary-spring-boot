package hanium.aidiary.service;

import hanium.aidiary.domain.Crop;
import hanium.aidiary.domain.Member;
import hanium.aidiary.exception.CustomException;
import hanium.aidiary.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static hanium.aidiary.handler.ErrorCode.USER_NOT_FOUND;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MemberService { // Module Service Layer
    private final MemberRepository memberRepository;

}