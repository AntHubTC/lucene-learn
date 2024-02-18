package com.tc.lucene.basic;

import com.tc.lucene.LuceneLearnApplicationTests;
import com.tc.lucene.config.LuceneDemoConfig;
import com.tc.lucene.util.AnalyzerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.uhighlight.DefaultPassageFormatter;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * 高亮显示案例
 *
 * @author AnthubTC
 * @version 1.0
 * @className HighlighterTest
 * @description
 * @date 2024/2/18 15:12
 **/
@Slf4j
@DisplayName("高亮查询显示")
public class HighlighterTest extends LuceneLearnApplicationTests {
    @Resource
    private LuceneDemoConfig luceneDemoConfig;

    @DisplayName("QueryParser")
    @ParameterizedTest
    @ValueSource(strings = {
            "谷", "歌", "地图",
            // SmartChineseAnalyzer没有切分出来的分词
            "谷歌地图", "谷歌地图之父加盟"
    })
    // 查询解析器（Query Parser）：Lucene 的查询解析器可以将用户输入的查询字符串解析为 Lucene 查询对象，从而进行有效的匹配和搜索。
    public void queryParserTest(String val) throws ParseException, IOException {
        // QueryParser会使用分词器分词后匹配查询
        // 这里特别注意：查询的分词器要和索引的时候的分词器保持一致，否则搜索结果匹配会不理想
        Analyzer analyzer = new SmartChineseAnalyzer();
        QueryParser parser = new QueryParser("title", analyzer);
        Query query = parser.parse(val);

        System.out.print("查询条件：【" + val + "】,");
        AnalyzerUtil.displayToken(val, analyzer);

        // 设置索引存储路径
        Directory indexDir = FSDirectory.open(new File(luceneDemoConfig.getDemoIndexDbPath("testCreate")).toPath());
        // 创建索引读取器
        try (IndexReader indexReader = DirectoryReader.open(indexDir)) {
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            TopDocs topDocs = indexSearcher.search(query, 10);
            System.out.println("文档搜索结果，命中目标:" + topDocs.totalHits);

            UnifiedHighlighter highlighter = new UnifiedHighlighter(indexSearcher, new SmartChineseAnalyzer());
            // 默认html标签
            // highlighter.setFormatter(new DefaultPassageFormatter());
            // 自定义前后标识
            highlighter.setFormatter(new DefaultPassageFormatter(" <span class=\"high-lighter-block\">", "</span> ", "... ", false));
            //highlighter.setFormatter(new DefaultPassageFormatter(" 【", "】 ", "... ", false));
            String[] contentHightFragments = highlighter.highlight("title", query, topDocs, 10); // 获取高亮显示结果

            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                // 获取文档
                Document doc = indexReader.document(scoreDoc.doc);

                System.out.println("id:" + doc.get("id") + ",score:" + scoreDoc.score + ", \r\n\t\tcontent:" + doc.get("title"));
                // 高亮显示
                System.out.println("高亮显示:" + contentHightFragments[i] + "\n");
            }
        }
    }
}
