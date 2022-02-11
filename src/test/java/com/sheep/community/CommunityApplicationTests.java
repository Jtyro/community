package com.sheep.community;

import com.sheep.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommunityApplicationTests {
	@Autowired
	private MailClient mailClient;

	@Test
	void contextLoads() {
		mailClient.sendMail("2246218872@qq.com","TEXT", "welcome");
	}

}
