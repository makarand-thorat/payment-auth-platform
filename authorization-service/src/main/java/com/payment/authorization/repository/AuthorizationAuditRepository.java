package com.payment.authorization.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.payment.authorization.model.AuthorizationAudit;

@Repository
public interface AuthorizationAuditRepository
        extends CassandraRepository<AuthorizationAudit, String> {
}
