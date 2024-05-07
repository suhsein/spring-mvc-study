package study.datajpa.repository;

import org.springframework.beans.factory.annotation.Value;


/**
 * 프로젝션
 * 인터페이스만 생성해주면 스프링이 프록시 구현체 만들어줌
 *
 * SpEL 사용하는 경우 -> Open Projection . 이 경우 entity 의 테이블 전체 select 해서 db 에서 읽어옴
 * SpEL 사용 안하고 정확히 매칭 -> Close Projection . 이 경우 필요한 필드만 db 에서 읽어옴
 */
public interface UsernameOnly {
//    @Value("#{target.username + ' ' + target.age}")
    String getUsername();
}
