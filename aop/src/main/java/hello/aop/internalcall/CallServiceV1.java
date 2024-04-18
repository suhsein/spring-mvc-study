package hello.aop.internalcall;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 수정자(setter) 주입
 */
@Slf4j
@Component
public class CallServiceV1 {
    private CallServiceV1 callServiceV1;

    // 순환 참조 문제로 자기 자신을 생성자 주입 받을 수 없음
    // 대신 setter 주입을 통해 주입받는다. (스프링부트 2.6 이상부터는 이조차도 기본 허용 x)
    // (properties = "spring.main.allow-circular-references=true") 추가
    @Autowired
    public void setCallServiceV1(CallServiceV1 callServiceV1) {
//        log.info("callServiceV1 setter={}", callServiceV1.getClass()); // 프록시가 주입됨
        this.callServiceV1 = callServiceV1;
    }

    public void external() {
        log.info("call external");
        callServiceV1.internal(); // 외부 메서드 호출
    }

    public void internal() {
        log.info("call internal");
    }
}
