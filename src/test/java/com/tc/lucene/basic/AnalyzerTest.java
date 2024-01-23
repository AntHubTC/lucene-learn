package com.tc.lucene.basic;

import com.tc.lucene.LuceneLearnApplicationTests;
import com.tc.lucene.util.AnalyzerUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.analyzer.JcsegAnalyzer;
import org.lionsoul.jcseg.dic.ADictionary;
import org.lionsoul.jcseg.dic.DictionaryFactory;
import org.lionsoul.jcseg.segmenter.SegmenterConfig;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author tangcheng_cd
 * @version 1.0
 * @className AnalyzerTest
 * @description
 * @date 2024/1/19 11:27
 **/
public class AnalyzerTest extends LuceneLearnApplicationTests {

    public static Stream<Arguments> textSource() {
        List<String> textCases = new ArrayList<>();
        textCases.add("Oh! Damn it, what should we do! He is Chinese");
        textCases.add("hello kim,I am tangcheng,我是 中国人,my email is xxfddsaz@163.com, and my QQ is 1096648780000");
        textCases.add("oh！见鬼，怎么办！ 他是一个中国人，据说他的名字叫刘霸天！");
        textCases.add("人名列表：苏轼、苏辙、毛泽东、普京、刘霸天、刘天霸");
        textCases.add("1月18日，陕西省农业农村厅网站发布文章《关于“谁来种地”重大问题的调研报告》（下称“调研报告”），调研报告显示，自撂荒地整治工作开展以来，全省撂荒耕地面积从2020年底的337.9万亩降至2023年6月底的11.6万亩，下降96.6%。");
        return textCases.stream().map(Arguments::of);
    }

    @DisplayName("英文分词器")
    @ParameterizedTest
    @MethodSource("textSource")
    public void test1(String text) {
        System.out.println("原文：" + text);
        Analyzer aly;

        System.out.println("StandardAnalyzer:");
        aly = new StandardAnalyzer();
        AnalyzerUtil.displayToken(text, aly);

        // StopAnalyzer无法处理中文停用词
        System.out.println("StopAnalyzer:");
        Set<String> myStopWords = new HashSet<>();
        myStopWords.add("is");
        myStopWords.add("是");
        myStopWords.add("and");
        aly = new StopAnalyzer(CharArraySet.unmodifiableSet(new CharArraySet(myStopWords, true)));
        AnalyzerUtil.displayToken(text, aly);

        System.out.println("SimpleAnalyzer:");
        aly = new SimpleAnalyzer();
        AnalyzerUtil.displayToken(text, aly);

        System.out.println("WhitespaceAnalyzer:");
        aly = new WhitespaceAnalyzer();
        AnalyzerUtil.displayToken(text, aly);
    }

    @DisplayName("中文分词器")
    @ParameterizedTest
    @MethodSource("textSource")
    public void chineseAnalyser(String text) {
        Analyzer aly;
        System.out.println("原文：" + text);

        System.out.println("分词器-SmartChineseAnalyzer:");
        aly = new SmartChineseAnalyzer();
        AnalyzerUtil.displayToken(text, aly);

        System.out.println("分词器-IKAnalyzer:");
        aly = new IKAnalyzer();
        AnalyzerUtil.displayToken(text, aly);

        // Jcseg https://gitee.com/lionsoul/jcseg
        for (ISegment.Type segType : ISegment.Type.values()) {
            /*
                七种切分模式：
                     简易模式：FMM算法，适合速度要求场合。
                     复杂模式：MMSEG四种过滤算法，具有较高的歧义去除，分词准确率达到了98.41%。
                     检测模式：只返回词库中已有的词条，很适合某些应用场合。
                     最多模式：细粒度切分，专为检索而生，除了中文处理外（不具备中文的人名，数字识别等智能功能）其他与复杂模式一致（英文，组合词等）。
                     分隔符模式：按照给定的字符切分词条，默认是空格，特定场合的应用。
                     NLP模式：继承自复杂模式，更改了数字，单位等词条的组合方式，增加电子邮件，大陆手机号码，网址，人名，地名，货币等以及无限种自定义实体的识别与返回。
                     n-gram模式：CJK和拉丁系字符的通用n-gram切分实现。
             */
            System.out.println("分词器-Jcseg:" + segType.name);
            SegmenterConfig segConfig = getSegmenterConfig();
            ADictionary dic = DictionaryFactory.createSingletonDictionary(segConfig);
            aly = new JcsegAnalyzer(segType, segConfig, dic);
            //非必须(用于修改默认配置): 获取分词任务配置实例
            // JcsegAnalyzer jcseg = (JcsegAnalyzer) aly;
            // SegmenterConfig config = jcseg.getConfig();
            //更多配置, 请查看 org.lionsoul.jcseg.SegmenterConfig
            AnalyzerUtil.displayToken(text, aly);
        }


        // paoding-analysis
        // mmseg4j-solr
        // ansj_seg
        // imdict-chinese-analyzer
    }

    private static SegmenterConfig getSegmenterConfig() {
        // https://www.cnblogs.com/kangniuniu/p/11138789.html
        // SegmenterConfig segConfig = new SegmenterConfig("absolute or relative jcseg.properties path");
        // 调用SegmenterConfig#load(String proFile)方法来从指定配置文件中初始化配置选项
        // segConfig.load("absolute or relative jcseg.properties path");
        SegmenterConfig segConfig = new SegmenterConfig(true);
        // 配置词典
        String[] lexicon = {"D:\\githubRepository\\lucene-learn\\lexicon"};
        segConfig.setLexiconPath(lexicon);
        // 追加同义词, 需要在 jcseg.properties中配置jcseg.loadsyn=1
        segConfig.setAppendCJKSyn(true);
        // 追加拼音, 需要在jcseg.properties中配置jcseg.loadpinyin=1
        segConfig.setAppendCJKPinyin(false);
        // 识别出中国人名
        segConfig.setICnName(true);
        // 中文姓氏的最大长度
        segConfig.setMaxCnLnadron(3);
        // 最大匹配长度. (5-7)
        segConfig.setMaxLength(7);
        // 是否清除停止语
        segConfig.setClearStopwords(true);
        // 是否将中文数字转换为阿拉伯数字
        segConfig.setCnNumToArabic(true);
        // 是否将中文分式转换为阿拉伯分式
        segConfig.setCnFactionToArabic(false);
        // 是否保留未识别的单词
        segConfig.setKeepUnregWords(true);
        // 是否对复杂英语单词进行二次切分.
        segConfig.setEnSecondSeg(false);
        // 在你知道你在做什么之前最好不要改变它
        // segConfig.setNameSingleThreshold(1000000);
        // 将保留在标记中的标点符号集
        // segConfig.setKeepPunctuations("@#%.&+");
        segConfig.setKeepPunctuations("");
        // 是否自动加载修改后的词典文件
        segConfig.setAutoload(true);
        // 自动加载的轮询时间(秒)
        segConfig.setPollTime(300);
        // 是否加载条目的词性。
        // segConfig.setLoadCJKPos(true);
        // 是否加载条目的拼音
        // segConfig.setLoadCJKPinyin(true);
        // 是否加载条目的同义词
        // segConfig.setLoadCJKSyn(true);
        // 是否加载条目的实体
        // segConfig.setLoadEntity(true);
        return segConfig;
    }
}
