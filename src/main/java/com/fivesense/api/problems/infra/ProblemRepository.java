package com.fivesense.api.problems.infra;

import com.fivesense.api.problems.domain.Problem;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.util.UUID;

public interface ProblemRepository extends JpaRepository<Problem, UUID> {
    Page<Problem> findByActiveTrue(Pageable pageable);
    List<Problem> findByActiveTrueOrderByNameAsc();
}
