package cn.cherish.springcloud.service.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReq implements java.io.Serializable {

    private static final long serialVersionUID = -3011311529585453957L;

    private Long id;
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 商品ID
     */
    private Long productId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 总金额
     */
    private Integer fee;


}
