package cn.cherish.springcloud.order.dal.repository;

import cn.cherish.springcloud.order.dal.entity.Order;
import me.cherish.dal.repository.IBaseDAO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderDAO extends IBaseDAO<Order, Long> {

    List<Order> findByUserId(Long userId, Pageable pageable);

}
