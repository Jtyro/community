package com.sheep.community;

import com.sheep.community.service.CommentService;
import com.sheep.community.util.MailClient;
import com.sheep.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.PriorityQueue;

@SpringBootTest
class CommunityApplicationTests {
	@Autowired
	private MailClient mailClient;

	@Autowired
	private SensitiveFilter sensitiveFilter;

	@Autowired
	private CommentService service;

	@Test
	void contextLoads() {
		PriorityQueue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>((o1, o2) -> o1.getValue() - o2.getValue());

	}

}
