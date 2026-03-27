package com.auditservice.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@Document(indexName = "system_audit_logs")
public class AuditLog {

    @Id
    private String eventId;

    @Field(type = FieldType.Keyword)
    private String timestamp;

    @Field(type = FieldType.Keyword)
    private String serviceName;

    @Field(type = FieldType.Keyword)
    private String action;

    @Field(type = FieldType.Keyword)
    private String performedBy;

    @Field(type = FieldType.Keyword)
    private String resourceId;

    @Field(type = FieldType.Text) // Text allows full-text searching on details
    private String details;
}
