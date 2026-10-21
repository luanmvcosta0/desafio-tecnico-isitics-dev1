package com.dev1_isitics.ligahub_be.modules.clubs.api.validation;

import com.dev1_isitics.ligahub_be.modules.clubs.api.ClubCreateRequest;
import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubLimits;
import com.dev1_isitics.ligahub_be.shared.text.AlphabeticalComparator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Locale;

public class AcronymMatchesNameValidator
    implements ConstraintValidator<AcronymMatchesName, ClubCreateRequest> {

  @Override
  public boolean isValid(ClubCreateRequest request, ConstraintValidatorContext context) {
    if (request.name() == null
        || request.acronym() == null
        || !request.acronym().matches(ClubLimits.ACRONYM_PATTERN)) {
      return true;
    }
    int[] acronym = request.acronym().toLowerCase(Locale.ROOT).codePoints().toArray();
    int[] nameLetters =
        AlphabeticalComparator.toComparisonKey(request.name())
            .codePoints()
            .filter(Character::isLetter)
            .toArray();
    return !allEqual(acronym) && startsWithFirstLetter(nameLetters, acronym)
        && isSubsequence(nameLetters, acronym);
  }

  private static boolean allEqual(int[] acronym) {
    return acronym[0] == acronym[1] && acronym[1] == acronym[2];
  }

  private static boolean startsWithFirstLetter(int[] nameLetters, int[] acronym) {
    return nameLetters.length > 0 && nameLetters[0] == acronym[0];
  }

  private static boolean isSubsequence(int[] nameLetters, int[] acronym) {
    int matched = 1;
    for (int index = 1; index < nameLetters.length && matched < acronym.length; index++) {
      if (nameLetters[index] == acronym[matched]) {
        matched++;
      }
    }
    return matched == acronym.length;
  }
}
