package com.sheep.community;

import com.alibaba.fastjson.JSONObject;
import com.sheep.community.dao.DiscussPostMapper;
import com.sheep.community.dao.elasticsearch.DiscussPostRepository;
import com.sheep.community.pojo.DiscussPost;
import com.sheep.community.pojo.Message;
import com.sheep.community.service.CommentService;
import com.sheep.community.service.MessageService;
import com.sheep.community.util.MailClient;
import com.sheep.community.util.SensitiveFilter;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.*;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests {
	@Autowired
	private DiscussPostRepository repository;

	@Qualifier("client")
	@Autowired
	private RestHighLevelClient client;

	@Autowired
	private DiscussPostMapper postMapper;

	@Test
	public void testExist(){
		boolean exists =repository.existsById(274);
		System.out.println(exists);
	}


	@Test
	void  testInsertList() {
//		repository.saveAll(postMapper.selectDiscussPosts(138, 0, 100));
//		repository.saveAll(postMapper.selectDiscussPosts(146, 0, 100));
//		repository.saveAll(postMapper.selectDiscussPosts(145, 0, 100));
//		repository.saveAll(postMapper.selectDiscussPosts(149, 0, 100));
	}

	@Test
	void highLightQuery() throws IOException {
		SearchRequest request = new SearchRequest("discusspost");

		HighlightBuilder highlightBuilder = new HighlightBuilder();
		highlightBuilder.field("title");
		highlightBuilder.field("context");
		highlightBuilder.requireFieldMatch(false);
		highlightBuilder.preTags("<span style='color:red'>");
		highlightBuilder.postTags("</span>");

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
				.query(QueryBuilders.multiMatchQuery("offer", "title", "content"))
				.sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
				.sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
				.sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
				.from(0)
				.size(10)
				.highlighter(highlightBuilder);

		request.source(sourceBuilder);
		SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);

		List<DiscussPost> list = new ArrayList<>();
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
			HighlightField titleField = hit.getHighlightFields().get("title");
			if (titleField != null){
				discussPost.setTitle(titleField.getFragments()[0].toString());
			}
			HighlightField contentFiled = hit.getHighlightFields().get("content");
			if (contentFiled != null){
				discussPost.setContent(contentFiled.getFragments()[0].toString());
			}

			System.out.println(discussPost);
			list.add(discussPost);
		}
		System.out.println(searchResponse.getHits().getTotalHits().value);

	}

}
