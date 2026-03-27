package com.auditservice.repository;

import com.auditservice.model.AuditLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends ElasticsearchRepository<AuditLog, String> {
    // Spring Data ES gives  save(), findById(), etc. automatically
}