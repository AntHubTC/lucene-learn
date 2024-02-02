package com.tc.lucene.basic;

import com.tc.lucene.LuceneLearnApplicationTests;
import com.tc.lucene.config.LuceneDemoConfig;
import com.tc.lucene.util.AnalyzerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author AnthubTC
 * @version 1.0
 * @className BaseDemoTest
 * @description
 * @date 2024/1/15 14:06
 **/
@Slf4j
@DisplayName("查询讲解")
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
    // 查询解析器（Query Parser）：Lucene 的查询解析器可以将用户输入的查询字符串解析为 Lucene 查询对象，从而进行有效的匹配和搜索。
    public void queryParserTest(String val) throws ParseException, IOException {
        // QueryParser会使用分词器分词后匹配查询
        // 这里特别注意：查询的分词器要和索引的时候的分词器保持一致，否则搜索结果匹配会不理想
        Analyzer analyzer = new SmartChineseAnalyzer();
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
        Query query = TermRangeQuery.newStringRange("title", "faca", "facf", true, true);

        queryData(query);
    }

    @DisplayName("PrefixQuery")
    @ParameterizedTest
    @ValueSource(strings = {
            "ls", "ch", "p", "ps"
    })
    public void testPrefixQuery(String val) throws IOException {
        // PrefixQuery 用于匹配索引以指定字符串开头的分词（注意是短语，而不是源文档）。
        List<String> sourceTxt = Arrays.asList("ls","lsof","lspci","ps","ps aux","pkill","pwd","cat","cd","cp","mv",
                "rm","touch","grep","find","chmod","chown","chgrp","history","man");
        // 文档对象列表
        List<Document> documents = sourceTxt.stream().map(srcTxt -> {
            Document document = new Document();
            document.add(new TextField("title", srcTxt, Field.Store.YES));
            return document;
        }).collect(Collectors.toList());
        Directory indexDir = writeIndexDir(new StandardAnalyzer(), documents);

        Term term = new Term("title", val);
        Query query = new PrefixQuery(term);

        queryData(query, indexDir);
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
            "歌,谷", // 和顺序有关
            "谷,歌,之父加盟"
    })
    public void testPhraseQuery(String val) throws IOException {
        // PhraseQuery 短语查询用于搜索包含特定术语序列的文档。
        PhraseQuery.Builder qryBuilder = new PhraseQuery.Builder();
        for (String str : val.split(",")) {
            // 添加多个单词组成序列
            qryBuilder.add(new Term("title", str));
        }
        // 所有项都必须满足
        PhraseQuery query = qryBuilder.build();

        queryData(query);
    }

    @DisplayName("WildcardQuery")
    @ParameterizedTest
    @ValueSource(strings = {
            "谷歌",
            "谷歌地图之父*",
            "谷歌地图之父??facebook",
            "*faceboo?"
    })
    public void testWildcardQuery(String val) throws IOException {
        // KeywordAnalyzer，它可以用于不对输入进行分词处理，而是将整个输入作为一个单独的标记
        Directory indexDirectory = writeIndexDir(new KeywordAnalyzer());

        // WildcardQuery 用于使用任何字符序列的'*'等通配符搜索文档，？ 匹配单个字符。
        Term term = new Term("title", val);
        Query query = new WildcardQuery(term);

        queryData(query, indexDirectory);
    }

    @DisplayName("FuzzyQuery")
    @ParameterizedTest
    @ValueSource(strings = {
            "apple",
            "appl",
            "banana"
    })
    public void testFuzzyQuery(String val) throws IOException {
        // FuzzyQuery 用于执行模糊查询，允许在搜索时对术语进行模糊匹配。
        List<String> sourceTxt = Arrays.asList("apple", "banana", "application", "aple", "appler");
        // 文档对象列表
        List<Document> documents = sourceTxt.stream().map(srcTxt -> {
            Document document = new Document();
            document.add(new TextField("title", srcTxt, Field.Store.YES));
            return document;
        }).collect(Collectors.toList());
        Directory indexDir = writeIndexDir(new StandardAnalyzer(), documents);

        Term term = new Term("title", val);
        Query query = new FuzzyQuery(term);

        queryData(query, indexDir);
    }

    @DisplayName("MatchAllDocsQuery")
    @Test
    public void testMatchAllDocsQuery() throws IOException {
        // MatchAllDocsQuery 它的作用是匹配索引中的所有文档。当你希望检索索引中的所有文档时，可以使用 MatchAllDocsQuery。这在一些需要遍历
        // 整个索引的情况下非常有用，比如统计文档总数、分析整个索引的内容等。
        Query query = new MatchAllDocsQuery();

        queryData(query);
    }

    @DisplayName("SpanQuery")
    @Test
    public void testSpanQuery() throws IOException {
        // SpanQuery 是 Lucene 中的一种特殊类型的查询，它用于在文本中执行更精细的短语和位置相关的匹配
        // 创建 SpanTermQuery 对象，用于匹配 "quick"
        SpanTermQuery quickQuery = new SpanTermQuery(new Term("title", "今晚"));
        // 创建 SpanTermQuery 对象，用于匹配 "brown"
        SpanTermQuery brownQuery = new SpanTermQuery(new Term("title", "冷"));
        // 创建 query 对象，将 quickQuery 和 brownQuery 之间的距离限制为1
        SpanQuery query = new SpanNearQuery(new SpanQuery[]{quickQuery, brownQuery}, 1, true);

        queryData(query);
    }

    private static Directory writeIndexDir() throws IOException {
        return writeIndexDir(new StandardAnalyzer());
    }

    private static Directory writeIndexDir(Analyzer analyzer) throws IOException {
        // 文档对象列表
        List<Document> documents = new ArrayList<>();
        // 收集文档数据
        BaseDemoTest.collectDocument(documents);

        return writeIndexDir(analyzer, documents);
    }

    private static Directory writeIndexDir(Analyzer analyzer, List<Document> documents) throws IOException {
        // RAMDirectory写到内存的索引，已经过期，有其它类可以代替
        Directory indexDirectory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDirectory, config);
        writer.addDocuments(documents);
        writer.close();
        return indexDirectory;
    }

    private void queryData(Query query) throws IOException {
        // 设置索引存储路径
        Directory indexDir = FSDirectory.open(new File(luceneDemoConfig.getDemoIndexDbPath("testCreate")).toPath());

        queryData(query, indexDir);
    }

    private static void queryData(Query query, Directory indexDir) throws IOException {
        // 创建索引读取器
        try (IndexReader indexReader = DirectoryReader.open(indexDir)) {
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
