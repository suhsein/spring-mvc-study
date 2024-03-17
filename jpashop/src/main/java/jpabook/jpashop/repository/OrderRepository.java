package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;
    public void save(Order order){
        em.persist(order);
    }
    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch){
        String memberName = orderSearch.getMemberName();
        OrderStatus orderStatus = orderSearch.getOrderStatus();
        String jpql = "select o from Order o join o.member m";
        boolean andFlag = false;

        if(StringUtils.hasText(memberName) || orderStatus != null){
            jpql += " where";
        }
        if (StringUtils.hasText(memberName)) {
            jpql += " m.name like :name";
            andFlag = true;
        }
        if(orderStatus != null){
            if(andFlag){
                jpql += " and";
            }
            jpql += " o.orderStatus = :status";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                                    .setMaxResults(1000); // 최대 1000건
        if (StringUtils.hasText(memberName)) {
            query.setParameter("name", memberName);
        }
        if(orderStatus != null) {
            query.setParameter("status", orderStatus);
        }
        return query.getResultList();
    }


}
