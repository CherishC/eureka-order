package cn.cherish.springcloud.order.dal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements java.io.Serializable {

    private static final long serialVersionUID = -6741390864654405244L;

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private Long id;
    /**
     * 订单号
     */
    @Column(name = "order_sn", unique = true, nullable = false)
    private String orderSn;
    /**
     * 商品ID
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    /**
     * 数量
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    /**
     * 总金额
     */
    @Column(name = "fee", nullable = false)
    private Integer fee;

    /**
     * 订单状态
     * 0 : 未付款
     * 1 : 已付款
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time", nullable = false, length = 19)
    private Date createdTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_time", length = 19)
    private Date modifiedTime;

}
