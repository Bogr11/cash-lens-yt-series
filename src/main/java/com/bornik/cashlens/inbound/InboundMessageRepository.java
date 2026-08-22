package com.bornik.cashlens.inbound;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface InboundMessageRepository extends JpaRepository<InboundMessage, Long> {

    boolean existsByExternalId(String externalId);

    Optional<InboundMessage> findByExternalId(String externalId);
}