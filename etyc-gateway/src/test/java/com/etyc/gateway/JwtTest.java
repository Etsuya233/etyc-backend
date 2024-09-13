package com.etyc.gateway;

import com.etyc.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class JwtTest {
	@Resource
	private JwtUtils jwtUtils;

	@Test
	public void testCreateToken(){
		String token = jwtUtils.createToken(Map.of("userId", "1008611"), 60 * 60 * 24L);
		System.out.println(token);
	}
}
