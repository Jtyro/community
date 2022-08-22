package com.sheep.community.util;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sheep
 */
@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    /**
     * 替换符
     */
    private static final String REPLACEMENT = "***";
    /**
     * 根节点
     */
    private final TrieNode root = new TrieNode();

    /**
     * 在初始化时，完成敏感词前缀树的构建
     */
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt")
        ) {
            assert is != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))
            ) {
                String keyword;
                while ((keyword = reader.readLine()) != null) {
                    this.addKeyword(keyword);
                }
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    /**
     * 添加敏感词
     *
     * @param keyword 敏感词
     */
    public void addKeyword(String keyword) {
        TrieNode tempNode = root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            tempNode = subNode;
            if (i == keyword.length() - 1) {
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (text == null) {
            return null;
        }
        //定义三个指针
        TrieNode tempNode = root;
        int begin = 0;
        int position = 0;

        StringBuilder sb = new StringBuilder();
        while (begin < text.length()) {
            if (position < text.length()) {
                char c = text.charAt(position);
                if (isSymbol(c)) {
                    if (tempNode == root) {
                        sb.append(c);
                        begin++;
                    }
                    position++;
                    continue;
                }
                tempNode = tempNode.getSubNode(c);
                if (tempNode == null) {
                    sb.append(text.charAt(begin));
                    position = ++begin;
                    tempNode = root;
                } else if (tempNode.isKeywordEnd()) {
                    sb.append(REPLACEMENT);
                    begin = ++position;
                    tempNode = root;
                } else {
                    position++;
                }
            } else {
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = root;
            }
        }
        return sb.toString();
    }

    private boolean isSymbol(Character c) {
        //0x2E80~0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 前缀树节点类
     */
    private static class TrieNode {
        private boolean keywordEnd = false;
        private final Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return keywordEnd;
        }

        public void setKeyWordEnd(boolean keywordEnd) {
            this.keywordEnd = keywordEnd;
        }

        public void addSubNode(Character c, TrieNode subNode) {
            subNodes.put(c, subNode);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
