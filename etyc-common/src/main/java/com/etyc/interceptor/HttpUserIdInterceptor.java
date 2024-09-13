package com.etyc.interceptor;

import cn.hutool.core.util.StrUtil;
import com.etyc.constant.Constants;
import com.etyc.entity.BaseContext;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Configuration
@ConditionalOnClass(HandlerInterceptor.class)
public class HttpUserIdInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String userId = request.getHeader(Constants.JWT_USER_KEY);
		if(userId != null)
			BaseContext.setUserId(Long.valueOf(userId));
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		BaseContext.removeUserId();
	}
}
