package cn.cherish.springcloud.order.service.component.rocketmq;

import cn.cherish.springcloud.order.dal.entity.Order;
import cn.cherish.springcloud.order.service.impl.OrderService;
import cn.cherish.springcloud.service.dto.OrderDTO;
import cn.cherish.springcloud.service.req.OrderReq;
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * @author Cherish
 * @version 1.0
 * @date 2017/6/20 11:54
 */
@Slf4j
@Component
public class RocketMQ {

    private static final String NamesrvAddr = "39.108.151.46:9876";
    private static final String PRODUCER_GROUP = "order_status_group";

    private static final String TOPIC = "springcloud_order";
    private static final String TAG_ORDER_STATUS = "TAG_ORDER_STATUS";
    private static final String TAG_CREATE_ORDER = "TAG_CREATE_ORDER";

    @Autowired
    private OrderService orderService;

    public RocketMQ() {
        try {
            initConsumer();
            initProducer();

            // 获取生成订单的消息
            initCreateOrderConsumer();
        } catch (MQClientException e) {
            log.error("【RocketMQ】{}", Throwables.getStackTraceAsString(e));
        }
    }

    private DefaultMQProducer producer;
    private void initProducer(){
        //Instantiate with a producer PRODUCER_GROUP name.
        producer = new DefaultMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(NamesrvAddr);
        producer.setInstanceName("order_status_producer1");
        producer.setVipChannelEnabled(false);
        //Launch the instance.
        try {
            producer.start();
            log.info("【RocketMQ启动】 Producer Started");
        } catch (MQClientException e) {
            log.error("【RocketMQ启动】{}", Throwables.getStackTraceAsString(e));
        }
    }

    private void initConsumer() throws MQClientException {

        DefaultMQPushConsumer consumer  = new DefaultMQPushConsumer("order_status_consumer_group");
        consumer.setNamesrvAddr(NamesrvAddr);
        consumer.setInstanceName("order_status_consumer1");
        consumer.setVipChannelEnabled(false);
        consumer.subscribe(TOPIC, TAG_ORDER_STATUS);
        // Register message listener
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
                for (MessageExt message : messages) {
                    log.info("【检查订单状态】 Consumer Receive: {} at {} ", message, System.currentTimeMillis());
                    String body = new String(message.getBody());
                    log.info("【检查订单状态】 body: {}", body);

                    JSONObject jsonObject = JSON.parseObject(body);
                    Long orderId = jsonObject.getLong("orderId");
                    Order order = orderService.findById(orderId);

                    if (0 == order.getStatus()) {
                        log.info("【检查订单状态】 orderId:{} 未付款, 取消该订单", orderId);
                        order.setStatus(-1);
                        orderService.update(order);
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        log.info("【检查订单状态】 检查订单状态 Consumer Started");
    }

    private void initCreateOrderConsumer() throws MQClientException {
        DefaultMQPushConsumer createOrderConsumer = new DefaultMQPushConsumer("order_create_consumer_group");
        createOrderConsumer.setNamesrvAddr(NamesrvAddr);
        createOrderConsumer.setInstanceName("order_create_consumer1");
        createOrderConsumer.setVipChannelEnabled(false);
        createOrderConsumer.subscribe(TOPIC, TAG_CREATE_ORDER);
        // Register message listener
        createOrderConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
                for (MessageExt message : messages) {
                    log.info("【获取生成订单】 Consumer Receive: {} ", message);
                    String body = new String(message.getBody());
                    OrderCreateMsg orderCreateMsg = JSON.parseObject(body, OrderCreateMsg.class);
                    log.info("【获取生成订单】 orderCreateMsg: {}", orderCreateMsg);

                    OrderReq orderReq = new OrderReq();
                    orderReq.setUserId(orderCreateMsg.getUserId());
                    orderReq.setProductId(orderCreateMsg.getProductId());
                    orderReq.setQuantity(orderCreateMsg.getNum());
                    orderReq.setFee(orderCreateMsg.getFee());
                    OrderDTO orderDTO = orderService.createOrder(orderReq);

                    log.info("【获取生成订单】 order: {}", orderDTO);

                    // 发送到延时处理订单状态
                    sendMsgToDealOrderStatusDelay("{\"orderId\":" + orderDTO.getId() + "}");
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        createOrderConsumer.start();
        log.info("【获取生成订单】 获取生成订单的消息 Consumer Started");
    }

    private boolean sendMsgToDealOrderStatusDelay(String body) {
        Message msg = new Message(
                TOPIC,
                TAG_ORDER_STATUS,
                body.getBytes()
        );
        msg.setKeys(UUID.randomUUID().toString());
        // messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        // 延时10分钟
        msg.setDelayTimeLevel(14);
        //发送消息，只要不抛异常就是成功
        try {
            log.info("【RocketMQ】 消息内容：{}  {}", body, msg);
            SendResult sendResult = producer.send(msg);
            log.info("【RocketMQ】 发送结果： {}", sendResult);
        } catch (Exception e){
            log.error("【RocketMQ】 {}", Throwables.getStackTraceAsString(e));
            return false;
        }
        return true;
    }


}
