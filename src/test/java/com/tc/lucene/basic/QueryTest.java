package com.tc.lucene.basic;

import com.tc.lucene.LuceneLearnApplicationTests;
import com.tc.lucene.config.LuceneDemoConfig;
import com.tc.lucene.util.AnalyzerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * @author tangcheng_cd
 * @version 1.0
 * @className BaseDemoTest
 * @description
 * @date 2024/1/15 14:06
 **/
@Slf4j
public class QueryTest extends LuceneLearnApplicationTests {
    @Resource
    private LuceneDemoConfig luceneDemoConfig;

    @DisplayName("QueryParser")
    @ParameterizedTest
    @ValueSource(strings = {
            "谷", "歌", "地图",
            // SmartChineseAnalyzer没有切分出来的分词
            "谷歌地图", "谷歌地图之父加盟"
    })
    public void queryParserTest(String val) throws ParseException, IOException {
        // QueryParser会使用分词器分词后匹配查询
        // 这里特别注意：查询的分词器要和索引的时候的分词器保持一致，否则搜索结果匹配会不理想
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        QueryParser parser = new QueryParser("title", analyzer);
        Query query = parser.parse(val);

        System.out.print("查询条件：【" + val + "】,");
        AnalyzerUtil.displayToken(val, analyzer);

        queryData(query);
    }

    @DisplayName("TermQuery")
    @ParameterizedTest
    @ValueSource(strings = {
            "谷", "歌", "地图",
            // SmartChineseAnalyzer没有切分出来的分词
            "谷歌地图", "谷歌地图之父加盟"
    })
    public void testTermQuery(String val) throws IOException {
        // TermQuery可以用于搜索文档的某个字段中包含指定单词的文档
        Term term = new Term("title", val);
        Query query = new TermQuery(term);

        queryData(query);
    }

    @DisplayName("TermRangeQuery")
    @Test
    public void testTermRangeQuery() throws IOException {
        // TermRangeQuery  当要搜索一系列文本术语时
        // 适用场景：方法通常用于需要根据字段值的字典顺序执行范围查询。
        Query query = TermRangeQuery.newStringRange("title", "faca", "facf", true, true);;

        queryData(query);
    }

    @DisplayName("PrefixQuery")
    @ParameterizedTest
    @ValueSource(strings = {
            "谷", "歌", "地图",
            // SmartChineseAnalyzer没有切分出来的分词
            "谷歌地图", "谷歌地图之父加盟"
    })
    public void testPrefixQuery(String val) throws IOException {
        // PrefixQuery 用于匹配索引以指定字符串开头的分词（注意是短语，而不是源文档）。
        Term term = new Term("title", val);
        Query query = new PrefixQuery(term);

        queryData(query);
    }

    @DisplayName("BooleanQuery")
    @ParameterizedTest
    @ValueSource(strings = {
            "谷,歌",
            "谷,歌,之父加盟"
    })
    public void testBooleanQuery(String val) throws IOException {
        // BooleanQuery 用于搜索使用AND, OR或NOT运算符进行多次查询的文档。

        BooleanQuery.Builder boolQryBuilder = new BooleanQuery.Builder();
        for (String str : val.split(",")) {
            // 多组查询条件
            Query termQuery = new TermQuery(new Term("title", str));
            boolQryBuilder.add(termQuery, BooleanClause.Occur.MUST);
            /*
                BooleanClause.Occur.MUST：表示该子句必须匹配，相当于逻辑上的 AND 操作。换句话说，文档必须满足这个子句才能被包含在查询结果中。
                BooleanClause.Occur.SHOULD：表示该子句应该匹配，但不是必须的，相当于逻辑上的 OR 操作。如果一个文档匹配了至少一个 SHOULD 子句，那么它就有可能会出现在查询结果中。
                BooleanClause.Occur.MUST_NOT：表示该子句必须不匹配，相当于逻辑上的 NOT 操作。文档不能匹配 MUST_NOT 子句才能被包含在查询结果中。
                BooleanClause.Occur.FILTER 用于表示过滤子句，它会对搜索结果进行筛选，但不会影响相关性评分和排序顺序。
             */
        }
        BooleanQuery query = boolQryBuilder.build();

        queryData(query);
    }

    @DisplayName("PhraseQuery")
    @ParameterizedTest
    @ValueSource(strings = {
            "谷,歌",
            "谷,歌,之父加盟"
    })
    public void testPhraseQuery(String val) throws IOException {
        // PhraseQuery 短语查询用于搜索包含特定术语序列的文档。
        PhraseQuery.Builder qryBuilder = new PhraseQuery.Builder();
        for (String str : val.split(",")) {
            // 添加多个单词组成序列
            qryBuilder.add(new Term("title", str));
        }
        PhraseQuery query = qryBuilder.build();

        queryData(query);
    }

    // WildcardQuery 用于使用任何字符序列的'*'等通配符搜索文档，？ 匹配单个字符。

    // FuzzyQuery用于使用模糊实现来搜索文档，模糊实现是基于编辑距离算法的近似搜索。

    // MatchAllDocsQuery 顾名思义匹配所有文档。

    private void queryData(Query query) throws IOException {
        // 设置索引存储路径
        Directory indexDir = FSDirectory.open(new File(luceneDemoConfig.getDemoIndexDbPath("testCreate")).toPath());

        // 创建索引读取器
        try(IndexReader indexReader = DirectoryReader.open(indexDir)) {
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            TopDocs topDocs = indexSearcher.search(query, 10);
            System.out.println("文档搜索结果，命中目标:" + topDocs.totalHits);
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                // 获取文档
                Document doc = indexReader.document(scoreDoc.doc);

                System.out.println("id:" + doc.get("id") + ",score:" + scoreDoc.score + ", \r\n\t\tcontent:" + doc.get("title"));
            }
        }
        // 关闭索引读取器
        // indexReader.close();
    }
}
