# LLM Evaluation

Produced with an LLM using the prompt in `LLM-Evaluation-prompt.md`, then reviewed and answered by
you. **Both halves are required** — an unread LLM assessment pasted in whole is worth nothing.

---

## Assessment

Paste the LLM's assessment here, in full and unedited.

---

## Review

### 1. Immutability and encapsulation — 25/25

Every field in both classes is `private final`:

- `Animal`: `private final String name; private final Species species; private final AgeMonths age; private final LocalDate intakeDate;` (`Animal.java:41-44`)
- `AgeMonths`: `private final int months;` (`AgeMonths.java:22`)

No setters, no mutating methods anywhere. Every accessor hands back either a reference to an already-immutable type (`String`, the `Species` enum constant, the immutable `AgeMonths`, and `LocalDate`, which is itself immutable) or a primitive, so no caller can reach back and mutate shelter state through an accessor — `intakeDate()`'s Javadoc even states the reasoning for why this is safe (`Animal.java:108-110`). `AgeMonths`'s constructor is `private`, forcing all construction through the validating `of(int)` factory (`AgeMonths.java:29`). Nothing is `public` without reason — `MAX_MONTHS` is `public static final` because it's part of the documented contract and is referenced by the test suite itself.

### 2. Constructor validation — 25/25

All five required checks are present, each with a message naming the offending argument, and all checks run before any field is assigned:

```java
if (name == null) { throw new IntakeException("name cannot be null"); }
if (name.isBlank()) { throw new IntakeException("name cannot be blank"); }
if (species == null) { throw new IntakeException("species cannot be null"); }
if (age == null) { throw new IntakeException("age cannot be null"); }
if (intakeDate == null) { throw new IntakeException("intakeDate cannot be null"); }
this.name = name.strip();
```

(`Animal.java:57-76`)

- Null name ✓, blank/whitespace-only name ✓ (`isBlank()` covers both), null species ✓, null age ✓, null intake date ✓.
- Trimming uses `name.strip()`, which (unlike `trim()`) correctly also strips exotic Unicode whitespace.
- All five checks execute before the first field assignment — a partially-valid `Animal` can never exist.
- No generic messages — each one names the exact argument and, for name, the exact rule broken ("cannot be null" vs "cannot be blank").

### 3. Correctness — 15/15

`AgeMonths.toString()` (`AgeMonths.java:117-142`) traces correctly for every required case: 0→"0 months", 1→"1 month", 11→"11 months", 12→"1 year", 23→"1 year, 11 months", 24→"2 years", 25→"2 years, 1 month". Singular/plural agreement is handled independently for the years part and months part, and the months part is omitted only when `remainder == 0` and the age is not under one year.

`Animal.toString()`:

```java
return name + " (" + species + ", " + age + ", intake " + intakeDate + ")";
```

produces `"Luna (Cat, 1 year, 11 months, intake 2026-09-21)"` exactly — `species` resolves via its own `toString()` to the display label ("Cat", not "CAT"), `age` resolves via `AgeMonths.toString()`, and `intakeDate` resolves via `LocalDate`'s ISO `toString()`.

### 4. Testing and coverage — 13/15

Strong boundary coverage: 0 months, the 11/12 boundary, `MAX_MONTHS`, `MAX_MONTHS - 1`, and `MAX_MONTHS + 1` are all exercised (`AgeMonthsAdditionalTest.java:49-68`), and every constructor exception path is tested with `assertThrows(IntakeException.class, ...)` rather than a bare `Exception` or `RuntimeException` — so a test would genuinely fail if the wrong exception type were thrown.

Three specific gaps:

1. Check-order is only verified for the pathological case where *all four* arguments are null (`theFirstBrokenRuleIsTheOneReported`, `AnimalAdditionalTest.java:72-79`). There is no test isolating, say, `species == null` together with `age == null` and a valid name, to confirm species really is reported before age rather than the all-null case coincidentally matching the first check.
2. No `Animal`-level test exercises an age at `AgeMonths.MAX_MONTHS` (40 years) through `Animal.toString()` — the `Animal` `toString` tests only use 0, 1, 23, and 24 months, so a regression that breaks formatting specifically for large whole-year ages inside `Animal` would not be caught.
3. Exotic whitespace in names (e.g., non-breaking space) is untested — the student's own `introspection.md` admits this: *"I didn't test weird Unicode whitespace like non-breaking spaces in names."*

### 5. Code quality and style — 9/10

Most public members carry Javadoc that adds real information (e.g., `intakeDate()`'s note on why returning the field directly is safe, `Animal.java:104-110`). Two accessors are weaker — they restate the method name without adding anything a reader couldn't guess:

```java
/** Returns this animal's species. @return the species, never {@code null} */
public Species species() { ... }
/** Returns this animal's age at intake. @return the age, never {@code null} */
public AgeMonths age() { ... }
```

(`Animal.java:85-99`)

`Animal.toString()` does delegate correctly — `age` and `species` are concatenated directly into the format string rather than re-deriving years/months or a label, so a future change to `AgeMonths.toString()`'s format automatically propagates to `Animal` without any change here.

### 6. Scope discipline and code walk — 6/10

No inheritance, no collections, and no `equals`/`hashCode` were added — `AnimalTest.twoIdenticalAnimalsAreDifferentObjects()` is left untouched and the student's own test comments explicitly acknowledge the boundary rather than trying to "fix" it. Good discipline.

However, `introspection.md` contains no discussion of `Species` at all — not in "What you built," not in "Design decisions," not anywhere in the document. The assignment explicitly requires evidence the student understands why `Species` is "an `enum` with a field and a method rather than three constants," and this file gives none. This is a real gap, not a minor one, since it's the one piece of shipped code the student is still accountable for explaining.

---

**Totals: 25 + 25 + 15 + 13 + 9 + 6 = 93/100**

**Most important thing to do differently:** Go back and actually write the required reflection on `Species` in `introspection.md` — understanding *why* a design choice was made in code you didn't write is as much the point of this lab as the code you did write, and it's currently just missing.

**What they did genuinely well:** The validation and trimming logic in `Animal`'s constructor is exemplary — it validates every argument before assigning any field, uses `strip()` (not the Unicode-unsafe `trim()`), and gives every failure message a specific, debuggable cause. Combined with the delegated `toString()`, this is a textbook execution of "make an invalid object impossible to construct."

---

Its scoring categories are the ones in `LLM-Evaluation-prompt.md`, which are the same categories and
the same weights as the rubric in `how-to-submit.md`. If the LLM invents different categories or
weights, say so below rather than silently accepting them.

---

**Coverage reported:** 100% [line coverage % from `build/reports/jacoco/test/html/index.html`]

---

## Your response

The part that is actually marked. For each point below, a few sentences.

### Where it is right

Which criticisms do you accept? For each, say what you would change and why you agree.

I agree with some of the testing criticism. My original test verifies the check order only when all four arguments are null, which confirms that the first validation rule is checked first. However, it does not independently verify the ordering of the later checks. For example, testing a valid name with both `species` and `age` null would confirm that `species` is reported before `age`. Adding similar cases would make the intended validation order explicit and better protected against regressions.

### Where it is wrong

Which criticisms do you reject, and on what grounds? LLMs confidently misread code, invent
requirements that are not in the specification, and flag correct code as broken. Disagreeing with a
specific reason is worth more marks here than agreeing with everything.

I disagree with the second testing cricisim.`Animal.toString()` does not perform any age-formatting logic itself; it delegates directly to `AgeMonths.toString()` by concatenating the `age` object. `AgeMonths.MAX_MONTHS` is already tested at the class responsible for that formatting. The existing `Animal.toString()` tests verify that `Animal` correctly delegates to and incorporates the age representation, so adding a 40-year case at the `Animal` level would largely duplicate coverage rather than test distinct behavior.

### What it missed

What do you know is weak in your submission that the assessment did not mention? Volunteering this
costs you nothing and demonstrates you understand your own code.

The assessment didn't mention that Animal's intake date has no sanity check beyond being non-null. While this meets the exact assignment spec, in a real shelter domain, accepting intake dates in the year 3000 or the 1800s doesn't make sense. The constructor could have validated that the intake date isn't in the future relative to LocalDate.now().

### What you changed

If you changed anything as a result, say what and why. If you changed nothing, say that and defend
it.

I did not change any code. The LLM's only valid criticism was adding more check-order tests for combinations of null arguments, but my existing tests already satisfy the requirements and verify that invalid animals cannot be constructed.

---

## Declaration

- Which LLM and version you used: Claude Sonnet 5
- Confirm you understand every line you submitted, regardless of who or what wrote it: yes
