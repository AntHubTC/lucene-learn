package com.tc.lucene.basic;

import com.tc.lucene.LuceneLearnApplicationTests;
import com.tc.lucene.config.LuceneDemoConfig;
import com.tc.lucene.util.AnalyzerUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author tangcheng_cd
 * @version 1.0
 * @className BaseDemoTest
 * @description
 * @date 2024/1/15 14:06
 **/
@DisplayName("基础demo")
public class BaseDemoTest extends LuceneLearnApplicationTests {

    @Resource
    private LuceneDemoConfig luceneDemoConfig;

    /**
     * === 索引创建 =====
     * 1 创建文档对象
     * 2 创建存储目录
     * 3 创建分词器
     * 4 创建索引写入器的配置对象
     * 5 创建索引写入器对象
     * 6 将文档交给索引写入器
     * 7 提交
     * 8 关闭
     */
    @Test
    public void testCreate() {
        try {
            // 1 创建文档对象
            List<Document> documents = new ArrayList<>();
            // 收集文档数据
            collectDocument(documents);

            // 2 索引目录类,指定索引在硬盘中的位置
            Directory directory = FSDirectory.open(new File(luceneDemoConfig.getDemoIndexDbPath("testCreate")).toPath());
            // 3 创建分词器对象
            Analyzer analyzer = new SmartChineseAnalyzer();
            // 4 索引写出工具的配置对象
            IndexWriterConfig conf = new IndexWriterConfig(analyzer);
            // 5 创建索引的写出工具类。参数：索引的目录和配置信息
            IndexWriter indexWriter = new IndexWriter(directory, conf);
            // 6 把文档交给IndexWriter
            // indexWriter.addDocument(document);
            indexWriter.addDocuments(documents);
            // 7 提交
            indexWriter.commit();
            // 8 关闭
            indexWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查看分词情况
     */
    @Test
    public void tokenViewer() {
        for (String sourceTxt : getSourceTxtData()) {
            // Analyzer analyzer = new StandardAnalyzer();
            Analyzer analyzer = new SmartChineseAnalyzer();
            AnalyzerUtil.displayToken(sourceTxt, analyzer);
        }
    }

    public static void collectDocument(List<Document> documents) {
        List<String> sourceTxt = getSourceTxtData();
        for (int i = 0; i < sourceTxt.size(); i++) {
            String id = String.valueOf(i + 1);
            String content = sourceTxt.get(i);

            Document document = new Document();
            // 创建并添加字段信息。参数：字段的名称、字段的值、是否存储，这里选Store.YES代表存储到文档列表。Store.NO代表不存储
            document.add(new StringField("id", id, Field.Store.YES));
            // 这里我们title字段需要用TextField，即创建索引又会被分词。
            document.add(new TextField("title", content, Field.Store.YES));

            documents.add(document);
        }
    }

    public static List<String> getSourceTxtData() {
        return Arrays.asList(
                "谷歌地图之父跳槽facebook",
                "谷歌地图之父加盟facebook",
                "谷歌地图创始人拉斯离开谷歌加盟Facebook",
                "谷歌地图之父跳槽Facebook与wave项目取消有关",
                "谷歌地图之父拉斯加盟社交网站Facebook",
                "床前明月光，疑是地上霜; 今晚有点冷，有点睡不戳。"
        );
    }
}
