package edu.northeastern.shelter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * My own tests for {@link Animal}, covering what the provided suite leaves open.
 *
 * <p>The provided suite checks that each bad argument is refused, but checks the <em>content</em>
 * of only one refusal message. It also leaves the trimming rule, the no-defensive-copy decision and
 * the delegated {@code toString} parts under-tested. These tests fill those gaps.
 */
@Tag("current")
class AnimalAdditionalTest {

  private static final LocalDate INTAKE = LocalDate.of(2026, 9, 21);
  private static final AgeMonths ONE_MONTH = AgeMonths.of(1);

  // --- Exception message content ---------------------------------------------------------------
  // AnimalTest.theRefusalSaysWhichArgumentWasWrong checks the blank-name message only. Each of the
  // other four refusals is graded on naming its argument and none of them is pinned by a test.

  @Test
  void theNullNameRefusalNamesTheArgument() {
    IntakeException thrown =
        assertThrows(
            IntakeException.class, () -> new Animal(null, Species.DOG, ONE_MONTH, INTAKE));
    assertTrue(thrown.getMessage().toLowerCase().contains("name"), thrown.getMessage());
  }

  @Test
  void theNullSpeciesRefusalNamesTheArgument() {
    IntakeException thrown =
        assertThrows(IntakeException.class, () -> new Animal("Rex", null, ONE_MONTH, INTAKE));
    assertTrue(thrown.getMessage().toLowerCase().contains("species"), thrown.getMessage());
  }

  @Test
  void theNullAgeRefusalNamesTheArgument() {
    IntakeException thrown =
        assertThrows(IntakeException.class, () -> new Animal("Rex", Species.DOG, null, INTAKE));
    assertTrue(thrown.getMessage().toLowerCase().contains("age"), thrown.getMessage());
  }

  @Test
  void theNullIntakeDateRefusalNamesTheArgument() {
    IntakeException thrown =
        assertThrows(IntakeException.class, () -> new Animal("Rex", Species.DOG, ONE_MONTH, null));
    assertTrue(thrown.getMessage().toLowerCase().contains("intake"), thrown.getMessage());
  }

  @Test
  void theEmptyNameRefusalNamesTheArgument() {
    IntakeException thrown =
        assertThrows(IntakeException.class, () -> new Animal("", Species.DOG, ONE_MONTH, INTAKE));
    assertTrue(thrown.getMessage().toLowerCase().contains("name"), thrown.getMessage());
  }

  @Test
  void aRefusalIsUncheckedSoCallersNeedNoTryCatch() {
    assertThrows(
        IllegalArgumentException.class, () -> new Animal("", Species.DOG, ONE_MONTH, INTAKE));
  }

  @Test
  void theFirstBrokenRuleIsTheOneReported() {
    // Every argument is wrong here. The constructor checks in the order the Javadoc lists them, so
    // the name is what gets named. This documents the choice rather than leaving it accidental.
    IntakeException thrown =
        assertThrows(IntakeException.class, () -> new Animal(null, null, null, null));
    assertTrue(thrown.getMessage().toLowerCase().contains("name"), thrown.getMessage());
  }

  // --- The trimming rule -------------------------------------------------------------------------
  // AnimalTest covers spaces. The rule is about whitespace generally, and about refusing a name
  // that is only whitespace whatever kind of whitespace it is.

  @Test
  void tabsAndNewlinesAreStrippedFromTheEnds() {
    assertEquals("Luna", new Animal("\tLuna\n", Species.CAT, ONE_MONTH, INTAKE).name());
  }

  @Test
  void aNameOfOnlyTabsIsRefused() {
    assertThrows(
        IntakeException.class, () -> new Animal("\t\t", Species.DOG, ONE_MONTH, INTAKE));
  }

  @Test
  void aNameOfOnlyANewlineIsRefused() {
    assertThrows(IntakeException.class, () -> new Animal("\n", Species.DOG, ONE_MONTH, INTAKE));
  }

  @Test
  void aSingleCharacterNameIsEnough() {
    assertEquals("X", new Animal(" X ", Species.BIRD, ONE_MONTH, INTAKE).name());
  }

  @Test
  void theStoredNameIsTheTrimmedOneNotTheOriginal() {
    Animal padded = new Animal("  Luna  ", Species.CAT, ONE_MONTH, INTAKE);
    assertEquals(4, padded.name().length());
  }

  // --- No defensive copies, because every field type is immutable ---------------------------------

  @Test
  void theAgeIsHandedBackWithoutCopying() {
    AgeMonths age = AgeMonths.of(23);
    assertSame(age, new Animal("Luna", Species.CAT, age, INTAKE).age());
  }

  @Test
  void theSpeciesIsTheSameEnumConstant() {
    assertSame(Species.CAT, new Animal("Luna", Species.CAT, ONE_MONTH, INTAKE).species());
  }

  // --- toString delegation ------------------------------------------------------------------------
  // The provided suite checks two ages. These check the cases where a re-derived format would slip:
  // zero months, whole years, and each species label.

  @Test
  void toStringHandlesZeroMonths() {
    assertEquals(
        "Pip (Dog, 0 months, intake 2026-09-21)",
        new Animal("Pip", Species.DOG, AgeMonths.of(0), INTAKE).toString());
  }

  @Test
  void toStringOmitsTheMonthsPartForAWholeNumberOfYears() {
    assertEquals(
        "Rex (Dog, 2 years, intake 2026-09-21)",
        new Animal("Rex", Species.DOG, AgeMonths.of(24), INTAKE).toString());
  }

  @Test
  void toStringUsesTheSpeciesDisplayLabelNotTheConstantName() {
    String description = new Animal("Kiwi", Species.BIRD, ONE_MONTH, INTAKE).toString();
    assertTrue(description.contains("Bird"), description);
    assertFalse(
        description.contains("BIRD"), "the enum constant name should not leak: " + description);
  }

  @Test
  void toStringUsesTheTrimmedName() {
    assertEquals(
        "Luna (Cat, 1 month, intake 2026-09-21)",
        new Animal("  Luna  ", Species.CAT, ONE_MONTH, INTAKE).toString());
  }

  @Test
  void toStringWritesTheDateInIsoForm() {
    String description =
        new Animal("Rex", Species.DOG, ONE_MONTH, LocalDate.of(2026, 1, 5)).toString();
    assertTrue(description.contains("intake 2026-01-05"), description);
  }

  // --- Dates are deliberately unconstrained -------------------------------------------------------

  @Test
  void anIntakeDateInThePastIsAccepted() {
    LocalDate longAgo = LocalDate.of(1999, 12, 31);
    assertEquals(longAgo, new Animal("Rex", Species.DOG, ONE_MONTH, longAgo).intakeDate());
  }

  @Test
  void anIntakeDateInTheFutureIsAccepted() {
    // The spec asks for a null check and nothing more, so a future date is not this lab's problem.
    LocalDate later = LocalDate.of(2099, 1, 1);
    assertEquals(later, new Animal("Rex", Species.DOG, ONE_MONTH, later).intakeDate());
  }
}
