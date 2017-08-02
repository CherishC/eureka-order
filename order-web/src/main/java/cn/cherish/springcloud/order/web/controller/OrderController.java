package cn.cherish.springcloud.order.web.controller;

import cn.cherish.springcloud.order.service.impl.OrderService;
import cn.cherish.springcloud.service.dto.OrderDTO;
import cn.cherish.springcloud.service.req.OrderReq;
import me.cherish.web.MResponse;
import me.cherish.web.controller.ABaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController extends ABaseController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public MResponse show(@PathVariable Long id) {
        return buildResponse(Boolean.TRUE, "", orderService.findOne(id));
    }

    @GetMapping("/list")
    public MResponse getList() {
        return buildResponse(Boolean.TRUE, "", orderService.findAllDTO());
    }

    @GetMapping("/listByUserId/{userId}")
    public MResponse listByUserId(@PathVariable("userId") Long userId) {
        return buildResponse(Boolean.TRUE, "", orderService.findByUserId(userId));
    }

    @PostMapping("/create")
    public MResponse create(@RequestBody OrderReq orderReq) {
        log.info("【生成新订单】 {}", orderReq);
        OrderDTO newOrder = orderService.createOrder(orderReq);
        log.info("【生成新订单】 ID: {}", newOrder.getId());
        return buildResponse(Boolean.TRUE, "成功生成订单", null);
    }

    @DeleteMapping("/{id}")
    public MResponse delete(@PathVariable Long id){
        orderService.delete(id);
        return buildResponse(Boolean.TRUE, "删除成功", null);
    }

}
