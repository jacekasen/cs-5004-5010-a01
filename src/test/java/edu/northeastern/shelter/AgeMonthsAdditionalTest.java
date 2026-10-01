package edu.northeastern.shelter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * My own tests for {@link AgeMonths}, covering what the provided suite leaves open.
 *
 * <p>The provided suite pins the happy path and one exception message. These tests add the content
 * of the <em>other</em> refusal message, the boundaries either side of {@link AgeMonths#MAX_MONTHS},
 * and the invariants the Javadoc states but no provided test checks.
 */
@Tag("current")
class AgeMonthsAdditionalTest {

  // --- Exception message content -------------------------------------------------------------
  // AgeMonthsTest.theRefusalNamesTheOffendingValue checks only the too-old message. The negative
  // message is equally graded and equally untested.

  @Test
  void theNegativeRefusalNamesTheOffendingValue() {
    IntakeException negative = assertThrows(IntakeException.class, () -> AgeMonths.of(-1));
    assertTrue(negative.getMessage().contains("-1"), "the message should name the rejected value");
  }

  @Test
  void theNegativeRefusalSaysWhatRuleWasBroken() {
    IntakeException negative = assertThrows(IntakeException.class, () -> AgeMonths.of(-7));
    assertTrue(
        negative.getMessage().toLowerCase().contains("negative"),
        "the message should say the age was negative, not just that something was wrong");
  }

  @Test
  void theTooOldRefusalNamesTheLimitItself() {
    IntakeException tooOld =
        assertThrows(IntakeException.class, () -> AgeMonths.of(AgeMonths.MAX_MONTHS + 1));
    assertTrue(
        tooOld.getMessage().contains(String.valueOf(AgeMonths.MAX_MONTHS)),
        "a reader should learn what the limit is, not only that they exceeded it");
  }

  @Test
  void aRefusalIsUncheckedSoCallersNeedNoTryCatch() {
    // IntakeException extends IllegalArgumentException on purpose; this pins that decision.
    assertThrows(IllegalArgumentException.class, () -> AgeMonths.of(-1));
  }

  // --- Boundaries ----------------------------------------------------------------------------
  // The provided suite checks MAX_MONTHS and MAX_MONTHS + 1. The value just inside the limit is
  // the one an off-by-one error would break.

  @Test
  void oneBelowTheMaximumIsAccepted() {
    assertEquals(AgeMonths.MAX_MONTHS - 1, AgeMonths.of(AgeMonths.MAX_MONTHS - 1).months());
  }

  @Test
  void theMaximumDescribesItselfAsWholeYears() {
    assertEquals("40 years", AgeMonths.of(AgeMonths.MAX_MONTHS).toString());
    assertEquals(40, AgeMonths.of(AgeMonths.MAX_MONTHS).years());
    assertEquals(0, AgeMonths.of(AgeMonths.MAX_MONTHS).remainderMonths());
  }

  @Test
  void oneBelowTheMaximumDescribesItselfWithBothParts() {
    assertEquals("39 years, 11 months", AgeMonths.of(AgeMonths.MAX_MONTHS - 1).toString());
  }

  @Test
  void theOldestAgeIsNotUnderOneYear() {
    assertFalse(AgeMonths.of(AgeMonths.MAX_MONTHS).isUnderOneYear());
  }

  // --- Invariants stated in the Javadoc but never asserted --------------------------------------

  @Test
  void yearsAndRemainderAlwaysReconstructTheOriginalMonths() {
    for (int m = 0; m <= AgeMonths.MAX_MONTHS; m++) {
      AgeMonths age = AgeMonths.of(m);
      assertEquals(m, age.years() * 12 + age.remainderMonths(), "broke at " + m + " months");
    }
  }

  @Test
  void remainderMonthsIsAlwaysBetweenZeroAndEleven() {
    for (int m = 0; m <= AgeMonths.MAX_MONTHS; m++) {
      int remainder = AgeMonths.of(m).remainderMonths();
      assertTrue(remainder >= 0 && remainder <= 11, m + " months gave a remainder of " + remainder);
    }
  }

  @Test
  void yearsIsNeverNegative() {
    for (int m = 0; m <= AgeMonths.MAX_MONTHS; m++) {
      assertTrue(AgeMonths.of(m).years() >= 0, "broke at " + m + " months");
    }
  }

  @Test
  void toStringIsNeverNullAndNeverEmpty() {
    for (int m = 0; m <= AgeMonths.MAX_MONTHS; m++) {
      String description = AgeMonths.of(m).toString();
      assertNotNull(description, "toString returned null at " + m + " months");
      assertFalse(description.isBlank(), "toString was blank at " + m + " months");
    }
  }

  @Test
  void isUnderOneYearAgreesWithYears() {
    for (int m = 0; m <= AgeMonths.MAX_MONTHS; m++) {
      AgeMonths age = AgeMonths.of(m);
      assertEquals(age.years() == 0, age.isUnderOneYear(), "disagreed at " + m + " months");
    }
  }

  // --- Plural agreement beyond the examples in the Javadoc --------------------------------------

  @Test
  void singularAndPluralAgreeAtEveryYearBoundary() {
    assertEquals("1 year", AgeMonths.of(12).toString());
    assertEquals("2 years", AgeMonths.of(24).toString());
    assertEquals("3 years", AgeMonths.of(36).toString());
  }

  @Test
  void aSingleLeftoverMonthIsSingularAtEveryYear() {
    assertEquals("1 year, 1 month", AgeMonths.of(13).toString());
    assertEquals("3 years, 1 month", AgeMonths.of(37).toString());
    assertEquals("10 years, 1 month", AgeMonths.of(121).toString());
  }

  @Test
  void theMonthsPartIsOmittedOnlyWhenItIsZero() {
    // 12 months omits it; 14 months must not.
    assertEquals("1 year", AgeMonths.of(12).toString());
    assertEquals("1 year, 2 months", AgeMonths.of(14).toString());
  }
}
