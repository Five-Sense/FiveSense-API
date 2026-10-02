package com.fivesense.api.occurrences.infra;

import com.fivesense.api.occurrences.domain.Occurrence;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OccurrenceRepository extends JpaRepository<Occurrence, UUID> {}
