package com.dev1_isitics.ligahub_be.modules.clubs.application;

import com.dev1_isitics.ligahub_be.modules.clubs.api.ClubCreateRequest;
import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubLimits;
import com.dev1_isitics.ligahub_be.modules.clubs.infrastructure.ClubRepository;
import com.dev1_isitics.ligahub_be.shared.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubService {

  private static final String CLUB_LIMIT_MESSAGE =
      "A liga já tem o máximo de " + ClubLimits.MAX_CLUBS + " clubes.";
  private static final String NAME_TAKEN_MESSAGE = "Já existe um clube com o nome \"%s\".";
  private static final String ACRONYM_TAKEN_MESSAGE = "Já existe um clube com a sigla %s.";

  private final ClubRepository clubRepository;

  @Transactional
  public Club create(ClubCreateRequest request) {
    if (clubRepository.count() >= ClubLimits.MAX_CLUBS) {
      throw new BusinessRuleException(CLUB_LIMIT_MESSAGE);
    }
    if (clubRepository.existsByNameIgnoringCase(request.name())) {
      throw new BusinessRuleException(NAME_TAKEN_MESSAGE.formatted(request.name()));
    }
    if (clubRepository.existsByAcronym(request.acronym())) {
      throw new BusinessRuleException(ACRONYM_TAKEN_MESSAGE.formatted(request.acronym()));
    }
    return clubRepository.save(Club.found(request.name(), request.acronym()));
  }
}
