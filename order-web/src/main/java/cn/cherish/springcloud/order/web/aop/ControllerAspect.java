package cn.cherish.springcloud.order.web.aop;

import cn.cherish.springcloud.order.service.common.enums.ErrorCode;
import cn.cherish.springcloud.order.service.common.exception.ServiceException;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import me.cherish.web.MResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import java.util.Set;

@Slf4j
@Aspect
@Component
public class ControllerAspect {

	private final LocalValidatorFactoryBean localValidatorFactoryBean;

	@Autowired
	public ControllerAspect(LocalValidatorFactoryBean localValidatorFactoryBean) {
		this.localValidatorFactoryBean = localValidatorFactoryBean;
	}

    /**
     * 对返回JSON 的做切面，不包括后台模板ModelAndView那种
     * @return Map
     */
	@Around("execution(public me.cherish.web.MResponse" +
			" cn.cherish.springcloud.order.web.controller.*Controller.*(..))")
	public MResponse serviceAccess(ProceedingJoinPoint joinPoint) {
		//计时
		StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		MResponse response = null;
		//获得接口名
		String interfaceName = joinPoint.getSignature().getDeclaringTypeName();
		//获得方法名
		String methodName = joinPoint.getSignature().getName();
		//服务名
		String controllerName = interfaceName + "." + methodName;
		//获得参数列表
		Object[] args = joinPoint.getArgs();
		if (args != null) {
			try {
				if (args.length > 0){
					Object argObject = args[0];
					log.info("Start to handle {}, PARAMETER: {}", controllerName, argObject);
					//参数校验
					validate(argObject);
				}
				//业务执行
				response = (MResponse) joinPoint.proceed();
				stopwatch.stop();
				log.info("Finish handling {}, RESULT: {}, ELAPSED: {}", controllerName, response, stopwatch);
			} catch (Throwable throwable) {
				if (throwable instanceof ServiceException) {
					//逻辑错误
					ServiceException e = (ServiceException) throwable;
					response = new MResponse<>(e.getCode(), Boolean.FALSE, e.getMessage(), null);
					stopwatch.stop();
					log.info("Failed to call {}, RESULT: {}, ELAPSED: {}", controllerName, response, stopwatch);
				} else if (throwable instanceof DataAccessException) {
					//数据库有问题
					response = new MResponse<>(ErrorCode.ERROR_CODE_500_002.getCode(), Boolean.FALSE,
							ErrorCode.ERROR_CODE_500_002.getDesc(), null);
					stopwatch.stop();
					log.info("Failed to call {}, RESULT: {}, ELAPSED: {}", controllerName, response, stopwatch);
					log.error("Failed to call {}, RESULT: {}, CAUSE: {}", controllerName, response, Throwables.getStackTraceAsString(throwable));
				} else {
					//内部错误
					response = new MResponse<>(ErrorCode.ERROR_CODE_500_001.getCode(), Boolean.FALSE,
							ErrorCode.ERROR_CODE_500_001.getDesc(), null);
					stopwatch.stop();
					log.info("Failed to call {}, RESULT: {}, ELAPSED: {}", controllerName, response, stopwatch);
					log.error("Failed to call {}, RESULT: {}, CAUSE: {}", controllerName, response, Throwables.getStackTraceAsString(throwable));
				}
			}
		}
		return response;
	}

	/**
	 * 参数校验
	 * @param object 校验对象
	 * @throws ServiceException 服务异常
	 */
	private <T> void validate(T object, Class<?>... groups) throws ServiceException {
		Set<ConstraintViolation<T>> constraintViolations = localValidatorFactoryBean.validate(object, groups);
		if (constraintViolations != null && constraintViolations.size() > 0) {
			ConstraintViolation c = constraintViolations.iterator().next();
            throw new ServiceException(ErrorCode.ERROR_CODE_400.getCode(), c.getMessage());
        }
	}

}
