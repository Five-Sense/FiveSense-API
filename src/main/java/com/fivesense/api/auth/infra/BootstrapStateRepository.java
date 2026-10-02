package com.fivesense.api.auth.infra;

import com.fivesense.api.auth.domain.BootstrapState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface BootstrapStateRepository extends JpaRepository<BootstrapState,Integer> {
    @Override @Lock(LockModeType.PESSIMISTIC_WRITE) Optional<BootstrapState> findById(Integer id);
}
