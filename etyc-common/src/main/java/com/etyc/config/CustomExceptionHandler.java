package com.etyc.config;

import com.etyc.entity.R;
import com.etyc.exceptions.EtycException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@ConditionalOnClass(ExceptionHandler.class)
@Slf4j
public class CustomExceptionHandler {

	@PostConstruct
	public void postConstruct(){
		log.info("已注册自定义异常处理器！");
	}

	@ExceptionHandler(EtycException.class)
	public R<Void> handleEtycException(EtycException e) {
		if(e.getCause() != null){
			log.error("自定义错误：{}, {}, {}, {}, {}", e.getCode(), e.getMsg(),
					e.getCause().getClass(), e.getCause().getMessage(), e.getStackTrace()[0]);
		} else {
			log.error("自定义错误：{}, {}, {}", e.getCode(), e.getMsg(), e.getStackTrace()[0]);
		}
		return R.error(e.getCode(), e.getMsg());
	}

	@ExceptionHandler(Exception.class)
	public R<Void> handleException(Exception e) {
		log.error("未注册的异常：{}： {}", e.toString(), e.getStackTrace()[0]);
		return R.error(e.getClass().toString());
	}

}
