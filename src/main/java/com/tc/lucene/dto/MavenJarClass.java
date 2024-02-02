package com.tc.lucene.dto;

import com.tc.lucene.enums.MavenContentType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author AnthubTC
 * @version 1.0
 * @className MavenJarClass
 * @description
 * @date 2024/2/2 14:03
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Getter @Setter
public class MavenJarClass extends MavenArtifact {
    private String className;

    public MavenJarClass() {
        super.setType(MavenContentType.Clazz.getType());
    }

    public static MavenJarClass create(MavenArtifact mavenArtifact) {
        MavenJarClass mavenJarClass = new MavenJarClass();
        mavenJarClass.setGroupId(mavenArtifact.getGroupId());
        mavenJarClass.setArtifactId(mavenArtifact.getArtifactId());
        mavenJarClass.setVersion(mavenArtifact.getVersion());
        mavenJarClass.setFilePath(mavenArtifact.getFilePath());
        return mavenJarClass;
    }

    public static MavenJarClass fromDocument(Document document) {
        MavenArtifact mavenArtifact = MavenArtifact.fromDocument(document);
        MavenJarClass mavenJarClass = create(mavenArtifact);
        mavenJarClass.setClassName(document.get("className"));
        return mavenJarClass;
    }

    public Iterable<? extends IndexableField> toDocument() {
        Document document = (Document) super.toDocument();
        document.add(new TextField("className", this.getClassName(), Field.Store.YES));
        return document;
    }
    public static Iterable<? extends Iterable<? extends IndexableField>> toDocuments(List<MavenJarClass> mavenJarClasses) {
        return mavenJarClasses.stream().map(MavenJarClass::toDocument).collect(Collectors.toList());
    }


    @Override
    public String toString() {
        return "POM xml: \r\n" + super.toString() + "\r\n" +
                "class: " + this.getClassName();
    }
}
