package com.tc.lucene.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author AnthubTC
 * @version 1.0
 * @className AnalyzerUtil
 * @description
 * @date 2024/1/19 13:37
 **/
public class AnalyzerUtil {

    /**
     * 显示分词信息
     * Description:         查看分词信息
     * @param str        待分词的字符串
     * @param analyzer    分词器
     *
     */
    public static void displayToken(String str, Analyzer analyzer){
        // 将一个字符串创建成Token流
        try (TokenStream stream  = analyzer.tokenStream("", new StringReader(str))) {
            //获取词与词之间的位置增量
            PositionIncrementAttribute postiona = stream.addAttribute(PositionIncrementAttribute.class);
            //获取各个单词之间的偏移量
            OffsetAttribute offseta = stream.addAttribute(OffsetAttribute.class);
            // 保存相应词汇
            CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
            // 定位倒流的开头
            stream.reset();
            System.out.print("分词结果：");
            while(stream.incrementToken()){
                // System.out.print("位置增量" +postiona.getPositionIncrement()+":\t");
                System.out.print("[" + cta + "]");
                // System.out.print("位置增量" +postiona.getPositionIncrement()+":\t");
                // System.out.println(cta+"\t[" + offseta.startOffset()+" - " + offseta.endOffset() + "]\t<" + typea +">");
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
