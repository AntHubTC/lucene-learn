package com.tc.lucene.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author AnthubTC
 * @version 1.0
 * @className MavenJar
 * @description
 * @date 2024/2/2 13:57
 **/
@Getter
@Setter
public abstract class MavenJar {
    /**
     * 内容类型
     *
     * @see com.tc.lucene.enums.MavenContentType
     */
    private Integer type;

    public abstract String toMavenPom();
}
