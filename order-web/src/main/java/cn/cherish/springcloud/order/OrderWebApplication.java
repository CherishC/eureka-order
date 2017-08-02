package cn.cherish.springcloud.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class OrderWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderWebApplication.class, args);
	}
}
