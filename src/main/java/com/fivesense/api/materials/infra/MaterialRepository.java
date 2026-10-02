package com.fivesense.api.materials.infra;

import com.fivesense.api.materials.domain.Material;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.util.UUID;

public interface MaterialRepository extends JpaRepository<Material, UUID> {
    Page<Material> findByActiveTrue(Pageable pageable);
    List<Material> findByActiveTrueOrderByNameAsc();
}
