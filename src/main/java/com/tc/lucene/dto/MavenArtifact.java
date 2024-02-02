package com.tc.lucene.dto;

import com.tc.lucene.enums.MavenContentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;


/**
 * @author AnthubTC
 * @version 1.0
 * @className MavenArtifactId
 * @description
 * @date 2024/2/2 10:46
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class MavenArtifact extends MavenJar {
    private String groupId;
    private String artifactId;
    private String version;
    private String filePath;

    public MavenArtifact() {
        super.setType(MavenContentType.Artifact.getType());
    }

    public MavenArtifact(String groupId, String artifactId, String version) {
        this();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public Iterable<? extends IndexableField> toDocument() {
        Document document = new Document();
        document.add(new StringField("type", String.valueOf(this.getType()), Field.Store.YES));
        document.add(new StringField("groupId", this.getGroupId(), Field.Store.YES));
        document.add(new TextField("artifactId", this.getArtifactId(), Field.Store.YES));
        document.add(new StringField("version", this.getVersion(), Field.Store.YES));
        document.add(new StoredField("filePath", this.getFilePath()));
        return document;
    }

    public static MavenArtifact fromDocument(Document document) {
        MavenArtifact mavenArtifact = new MavenArtifact();
        mavenArtifact.setType(Integer.valueOf(document.get("type")));
        mavenArtifact.setGroupId(document.get("groupId"));
        mavenArtifact.setArtifactId(document.get("artifactId"));
        mavenArtifact.setVersion(document.get("version"));
        mavenArtifact.setFilePath(document.get("filePath"));
        return mavenArtifact;
    }

    @Override
    public String toString() {
        return "<dependency>\r\n" +
                "   <groupId>" + groupId +"</groupId>\r\n" +
                "   <artifactId>" + artifactId + "</artifactId>\r\n" +
                "   <version>" + version +"</version>\r\n" +
                "</dependency>";
    }

    @Override
    public String toMavenPom() {
        return this.toString();
    }
}
