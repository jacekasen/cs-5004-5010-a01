# Design Introspection

Your own reflection on the design decisions you made this week. Written in your own words — this is
distinct from the LLM's assessment of your code, and distinct from your code-walk video.

Aim for a page. Cite your actual code: name the class and method you are talking about.

## 1. What you built

In two or three sentences: which types you wrote this week, and what each one is responsible for.

This week I built `AgeMonths` and `Animal`. `AgeMonths` is an immutable value object that wraps an animal's age in whole months, where I wrote `years()`, `remainderMonths()`, `isUnderOneYear()`, and `toString()`. `Animal` represents an intake record; I implemented its constructor to validate all fields upfront before assignment, added its four accessors (`name()`, `species()`, `age()`, `intakeDate()`), and wrote its `toString()` by delegating formatting directly to its fields.

## 2. Design decisions

Pick the two or three decisions you actually had to think about, and for each one:

- **What was the choice?** What were the alternatives you considered?
- **What did you pick, and why?** What would have gone wrong with the other option?
- **What did it cost?** Every real design decision costs something.

Good candidates: where you put a piece of behaviour and why it belongs there rather than somewhere
else; how you represented something so that an invalid version could not be built; where you chose to
delegate to existing code rather than re-deriving an answer.

**Decision 1: order of checks in `Animal`'s constructor**

When writing `Animal`'s constructor (`Animal.java:57-71`), I had to choose between bailing out on the very first invalid argument or collecting all errors into a list to report at once. I went with throwing an `IntakeException` on the first failure because it matches how `AgeMonths.of()` works and keeps the code lightweight without building violation lists for rare cases. The downside is that if a caller passes several invalid arguments, they only find out about the first one and have to fix errors one rerun at a time.

**Decision 2: structuring `AgeMonths.toString()`'s pluralization logic**

In `AgeMonths.toString()` (`AgeMonths.java:117-142`), I debated chaining ternary operators into one compact expression versus using plain `if`/`else` branches with local variables. I picked explicit `if`/`else` blocks because stringing ternaries together for singular/plural nouns and omitted months gets messy fast. Splitting it up cost a few extra lines of code, but it made tricky edge cases like "0 months" and whole years much easier to read, trace, and debug.

**Decision 3: skipping defensive copies in `Animal`**

When storing and returning `AgeMonths` and `LocalDate` in `Animal` (`Animal.java:73-76`, `Animal.java:101-115`), I had to decide whether to copy them defensively or just pass references around directly. I chose to skip defensive copying because both classes are already immutable value objects, so cloning them would just waste memory and CPU cycles for no real safety gain. The trade-off is trust: `Animal` relies entirely on `AgeMonths` staying immutable, so if someone ever adds a setter to `AgeMonths` down the road, `Animal` becomes accidentally mutable too.

## 3. Invariants

What does your code guarantee about itself, and where is each guarantee enforced?

For each type that validates its input: what must always be true of an instance once it exists, and
which line makes that true? If a guarantee is enforced in more than one place, say why — and whether
that is deliberate or duplication.

`AgeMonths` guarantees its internal count is always between 0 and 480 (`MAX_MONTHS`). That's enforced entirely inside `AgeMonths.of()` (`AgeMonths.java:45-51`), which throws an `IntakeException` before constructing anything. Because the constructor is private (`AgeMonths.java:29`) and the field is `private final` with no setter, there's no backdoor to create or mutate an invalid instance.

`Animal` guarantees that none of its four fields are null, and that `name` is non-blank and stripped of surrounding whitespace (`Animal.java:57-71`). Crucially, `Animal` only checks that `age` isn't null—it never checks whether the age number makes sense. That's deliberate rather than an oversight, because `AgeMonths.of()` already guarantees valid ranges. Checking it again in `Animal` would just duplicate a rule `AgeMonths` already owns.

## 4. Testing

- Which cases did you add beyond the provided tests, and what made you think of them?
- Which test was hardest to write, and what did writing it teach you about your own design?
- What is still untested, and how would you test it if you had another hour?

I added tests in `AgeMonthsAdditionalTest` and `AnimalAdditionalTest` for gaps the starter suite left open. The provided suite barely checked error messages, so I verified that negative ages mention "negative" (`AgeMonthsAdditionalTest.java:26-38`) and that each rejected argument in `Animal` is specifically named (`AnimalAdditionalTest.java:31-64`). I also tested the boundary at 479 months (`MAX_MONTHS - 1`), verified that tabs and newlines get stripped from names (`AnimalAdditionalTest.java:86-100`), and looped over all 481 valid ages to confirm that `years() * 12 + remainderMonths()` always equals `months()`.

The hardest test to write was `theFirstBrokenRuleIsTheOneReported()` (`AnimalAdditionalTest.java:72-79`), where every argument passed to `Animal` is null. The test itself was only a couple lines, but figuring out what to assert made me realize that check order is part of your public API. Once you pin it down in a test, changing which check runs first breaks caller expectations.

As for what's still untested, I didn't test weird Unicode whitespace like non-breaking spaces in names, nor did I write concurrent multi-threading tests. If spent some more time on this, I'd turn my repetitive age tests into parameterized JUnit tests using `@CsvSource` to cleanly test combinations of year and month boundaries without copy-pasting code.

## 5. What you would change

Given another day, what would you do differently — and what stopped you this week? Be specific;
"write more tests" is not an answer.

If I had another day, I'd cache `AgeMonths` instances in a static array inside `AgeMonths.of()` (`AgeMonths.java:44-53`). Since there are only 481 possible valid ages and they're completely immutable, pre-creating them would drop heap allocations to zero. I didn't do it this week because the starter code came with `new AgeMonths(months)`, and I didn't want to optimize prematurely.

I'd also change `Animal`'s constructor (`Animal.java:57-71`) to collect all validation errors into one combined exception message instead of bailing out on the first one. On real intake forms, getting rejected four separate times is frustrating when you could just see all your mistakes at once. I stuck with fast-failing because it matched the assignment's starter tests and expected exception types.

## 6. What you found hard

The honest one. What took the longest, what did you get wrong first, and what finally made it click?
This is not marked on whether you struggled — everyone does — but on whether you can say clearly
where and why.

Getting `AgeMonths.toString()` (`AgeMonths.java:117-142`) right took me the longest. Plurals are tricky: 0 months is plural, 1 month is singular, whole years omit the months part entirely, and combined ages need independent plural checks on both words.

At first, I tried cramming everything into chained ternary operators. It turned into a mess where 0 months got swallowed or 12 months printed as "1 year, 0 months". What finally made it click was breaking it down into two clean steps: first format `yearsPart` and `monthsPart` with their own singular/plural checks, and then use simple `if` checks to combine them. Once I separated formatting the pieces from assembling the final string, everything passed right away.
