package cn.cherish.springcloud.order.service.component;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.atomic.PromotedToLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Cherish
 * @version 1.0
 * @date 2017/6/18 12:02
 */
@Slf4j
@Component
public class MZookeeper {

    @Value("${zookeeper.connectString}")
    private String connectString = "119.23.30.142:2181,39.108.67.111:2181,39.108.151.46:2181";
    private CuratorFramework client;

    public MZookeeper(){
        init();
    }

    private void init(){
        String namespace = "springcloud";// 本项目的命名空间
        client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(30000)
                .connectionTimeoutMs(30000)
                .canBeReadOnly(false)
                .retryPolicy(new ExponentialBackoffRetry(100, Integer.MAX_VALUE))
                .namespace(namespace)
                .defaultData(null)
                .build();
        client.start();
    }

    public CuratorFramework client(){
        return client;
    }

    private String orderSnPath = "/orderSn";
    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(100, Integer.MAX_VALUE);
    private PromotedToLock promotedToLock = PromotedToLock.builder().lockPath(orderSnPath).build();

    public Long orderSn() throws Exception {
        DistributedAtomicLong atomicLong = new DistributedAtomicLong(client(), orderSnPath, retryPolicy, promotedToLock);
        AtomicValue<Long> value = atomicLong.increment();
        while (!value.succeeded()) {
            atomicLong.increment();
        }
        log.debug("【zookeeper】 全局订单号orderSn: {}", value.postValue());
        return value.postValue();
    }

//    public static void main(String[] args) throws Exception {
//        Zookeeper zookeeper = new Zookeeper();
//        Long orderSn = zookeeper.orderSn();
//        System.out.println("orderSn = " + orderSn);
//        CloseableUtils.closeQuietly(zookeeper.client());
//    }
}
