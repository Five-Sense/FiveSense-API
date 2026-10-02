package com.fivesense.api.teams.infra;

import com.fivesense.api.teams.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {}
