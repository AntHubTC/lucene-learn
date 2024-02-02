package com.tc.lucene.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author AnthubTC
 * @version 1.0
 * @className MavenContentType
 * @description
 * @date 2024/2/2 13:58
 **/
@AllArgsConstructor
@Getter
public enum MavenContentType {
    Artifact(1, "Artifact"),
    Clazz(2, "Class")
    ;

    private final Integer type;
    private final String name;

    public static MavenContentType getEnum(String type) {
        for (MavenContentType value : MavenContentType.values()) {
            if (String.valueOf(value.getType()).equals(type)) {
                return value;
            }
        }
        return null;
    }
}
