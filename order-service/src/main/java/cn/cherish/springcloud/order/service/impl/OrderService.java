package cn.cherish.springcloud.order.service.impl;

import cn.cherish.springcloud.order.dal.entity.Order;
import cn.cherish.springcloud.order.dal.repository.OrderDAO;
import cn.cherish.springcloud.order.service.common.enums.ErrorCode;
import cn.cherish.springcloud.order.service.common.exception.ServiceException;
import cn.cherish.springcloud.order.service.component.MZookeeper;
import cn.cherish.springcloud.service.dto.OrderDTO;
import cn.cherish.springcloud.service.req.OrderReq;
import com.google.common.base.Throwables;
import me.cherish.dal.repository.IBaseDAO;
import me.cherish.service.ABaseService;
import me.cherish.util.ObjectConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService extends ABaseService<Order, Long> {

    private final OrderDAO orderDAO;
    private final MZookeeper zookeeper;

    @Autowired
    public OrderService(OrderDAO orderDAO, MZookeeper zookeeper) {
        this.orderDAO = orderDAO;
        this.zookeeper = zookeeper;
    }

    @Override
    protected IBaseDAO<Order, Long> getEntityDAO() {
        return orderDAO;
    }

    @Transactional
    public OrderDTO update(OrderReq orderReq) {
        Order order = findById(orderReq.getId());
        if (order == null) return null;

        ObjectConvertUtil.objectCopy(order, orderReq);
        order.setModifiedTime(new Date());
        Order update = super.update(order);

        return getOrderDTO(update);
    }

    @Transactional
    public OrderDTO createOrder(OrderReq orderReq) {
        Date date = new Date();
        Order order = new Order();
        ObjectConvertUtil.objectCopy(order, orderReq);
        order.setCreatedTime(date);
        order.setModifiedTime(date);
        order.setStatus(0);
        try {
            //orderSn 使用zookeeper获取全局订单号
            Long orderSn = zookeeper.orderSn();
            order.setId(orderSn);
            order.setOrderSn(System.currentTimeMillis() + "C" + orderSn);
        } catch (Exception e) {
            log.error("【产生订单】 获取全局订单号出错  {}", Throwables.getStackTraceAsString(e));
            throw new ServiceException(ErrorCode.ERROR_CODE_500_001);
        }
        Order save = super.save(order);

        return getOrderDTO(save);
    }

    public OrderDTO findOne(Long id) {
        Order order = orderDAO.findOne(id);
        return getOrderDTO(order);
    }

    public List<OrderDTO> findByUserId(Long userId) {
        PageRequest pageRequest = new PageRequest(0, 20, new Sort(Sort.Direction.DESC, "id"));
        List<Order> orders = orderDAO.findByUserId(userId, pageRequest);
        return orders.stream()
                .map(this::getOrderDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> findAllDTO() {
        List<Order> orders = orderDAO.findAll();
        return orders.stream()
                .map(this::getOrderDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO getOrderDTO(Order order) {
        if (order == null) {
            return null;
        }
        OrderDTO orderDTO = new OrderDTO();
        ObjectConvertUtil.objectCopy(orderDTO, order);
        return orderDTO;
    }


}
