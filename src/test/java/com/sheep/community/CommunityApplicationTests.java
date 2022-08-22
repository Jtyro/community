package com.sheep.community;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests {



	@Test
	void  testInsertList() {
		Map<String, List<String>> map = new HashMap<>();
		for(String key : map.keySet()){
			List<String> strings = map.get(key);
		}
	}



}
