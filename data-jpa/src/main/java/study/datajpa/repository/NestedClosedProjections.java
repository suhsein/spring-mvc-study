package study.datajpa.repository;

/**
 * 중첩 Projection
 * root 엔티티에 대해서는 최적화. 원하는 필드만 가져옴
 * 하지만 연관 엔티티는 특정 필드만 가져오지 않고 모두 가져옴
 */
public interface NestedClosedProjections {
    String getUsername();

    TeamInfo getTeam();

    interface TeamInfo{
        String getName();
    }
}
