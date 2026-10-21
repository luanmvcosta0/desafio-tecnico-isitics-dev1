package com.dev1_isitics.ligahub_be.modules.clubs.infrastructure;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Integer> {}
