package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {
        String memberName = orderSearch.getMemberName();
        OrderStatus orderStatus = orderSearch.getOrderStatus();
        String jpql = "select o from Order o join o.member m";
        boolean andFlag = false;

        if (StringUtils.hasText(memberName) || orderStatus != null) {
            jpql += " where";
        }
        if (StringUtils.hasText(memberName)) {
            jpql += " m.name like :name";
            andFlag = true;
        }
        if (orderStatus != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " o.orderStatus = :status";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); // 최대 1000건
        if (StringUtils.hasText(memberName)) {
            query.setParameter("name", memberName);
        }
        if (orderStatus != null) {
            query.setParameter("status", orderStatus);
        }
        return query.getResultList();
    }

    /**
     * QueryDSL
     * 직관적이고 동적 쿼리에 대한 예외처리 쉬움
     * 컴파일 시점에 오타 에러 발견 가능.
     */

    public List<Order> findAll(OrderSearch orderSearch) {
        JPAQueryFactory query = new JPAQueryFactory(em);
        QOrder order = QOrder.order;
        QMember member = QMember.member;

        return query.select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private static BooleanExpression nameLike(String memberName) {
        if(!StringUtils.hasText(memberName)){
            return null;
        }
        return QMember.member.name.like(memberName);
    }

    private BooleanExpression statusEq(OrderStatus statusCond){
        if(statusCond == null){
            return null;
        }
        return QOrder.order.orderStatus.eq(statusCond);
    }

    /**
     * 패치 조인
     * fetch 는 JPA 에만 있는 문법이다.
     */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery("select o from Order o"
                + " join fetch o.member m"
                + " join fetch o.delivery d", Order.class).getResultList();
    }

    /**
     * 패치 조인
     * fetch 는 JPA 에만 있는 문법이다.
     */
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery("select o from Order o"
                        + " join fetch o.member m"
                        + " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }


    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.orderStatus, d.address)"
                                + " from Order o"
                                + " join o.member m"
                                + " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }

    public List<Order> findAllWithItem() {
        return em.createQuery("select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .getResultList();
    }
}
