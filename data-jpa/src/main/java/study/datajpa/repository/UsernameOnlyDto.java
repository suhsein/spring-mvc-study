package study.datajpa.repository;

/**
 * 프로젝션 2
 * 인터페이스 아니라 실제 클래스 기반으로도 프로젝션 가능
 *
 * 중요한 것은 생성자를 둬야하고, 생성자의 파라미터명이 엔티티 필드명과 동일해야 함
 */
public class UsernameOnlyDto {
    private final String username;

    public UsernameOnlyDto(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
