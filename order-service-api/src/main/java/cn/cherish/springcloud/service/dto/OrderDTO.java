package cn.cherish.springcloud.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO implements java.io.Serializable {

    private static final long serialVersionUID = 1810251335161969442L;

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
    /**
     * 订单状态
     */
    private Integer status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "UTC+8")
    private Date createdTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "UTC+8")
    private Date modifiedTime;

}
