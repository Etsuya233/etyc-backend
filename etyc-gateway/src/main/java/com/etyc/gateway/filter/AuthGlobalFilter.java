package com.etyc.gateway.filter;

import cn.hutool.core.collection.CollUtil;
import com.etyc.constant.Constants;
import com.etyc.exceptions.EtycException;
import com.etyc.gateway.config.AuthProperties;
import com.etyc.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

	private final AuthProperties authProperties;
	private final AntPathMatcher antPathMatcher = new AntPathMatcher();
	private final JwtUtils jwtUtils;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();

		//判断是否需要验证
		String path = request.getPath().toString();
		boolean surpass = false;
		for(int i = 0; authProperties.forceAuthPatterns != null && i < authProperties.forceAuthPatterns.size(); i++){
			if(antPathMatcher.match(authProperties.forceAuthPatterns.get(i), path)){
				surpass = true;
				break;
			}
		}
		for(int i = 0; !surpass && authProperties.noAuthPatterns != null &&
				i < authProperties.noAuthPatterns.size(); i++){
			if(antPathMatcher.match(authProperties.noAuthPatterns.get(i), path)){
				return chain.filter(exchange);
			}
		}

		//获取请求头
		HttpHeaders headers = request.getHeaders();

		//获取Authorization
		List<String> authorizations = headers.get(Constants.AUTHORIZATION_HEADER);
		String token;
		if(CollUtil.isEmpty(authorizations)){
			throw new EtycException("登陆失败，用户Token为空！");
		}
		token = authorizations.getFirst().substring(7); //Bearer !!!

		//检验Token
		Map<String, Object> claims;
		try {
			claims = jwtUtils.parseToken(token);
		} catch (Exception e){
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}
		String userId = claims.get(Constants.JWT_USER_KEY).toString();

		//添加userId至请求头中，给送过去
		exchange.mutate().request(r -> r.header(Constants.JWT_USER_KEY, userId));

		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
