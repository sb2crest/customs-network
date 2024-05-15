package com.customs.network.fdapn.repository;

import com.customs.network.fdapn.model.PGAIdentifierDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PGAIdentifierRepository extends JpaRepository<PGAIdentifierDetails, String> {
}
