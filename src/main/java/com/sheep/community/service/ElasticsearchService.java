package com.sheep.community.service;

import com.alibaba.fastjson.JSONObject;
import com.sheep.community.dao.elasticsearch.DiscussPostRepository;
import com.sheep.community.pojo.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchService {

    @Qualifier("client")
    private RestHighLevelClient restHighLevelClient;
    private DiscussPostRepository repository;
    @Autowired
    public void setRestHighLevelClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }
    @Autowired
    public void setRepository(DiscussPostRepository repository) {
        this.repository = repository;
    }

    public void saveDiscussPost(DiscussPost discussPost){
        repository.save(discussPost);
    }

    public void deleteDiscussPost(int id){
        repository.deleteById(id);
    }

    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) throws IOException {
        SearchRequest request = new SearchRequest("discusspost");

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("context");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(current)
                .size(limit)
                .highlighter(highlightBuilder);


        request.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);


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

        return new PageImpl<>(list, PageRequest.of(current, limit), searchResponse.getHits().getTotalHits().value);
    }
}
