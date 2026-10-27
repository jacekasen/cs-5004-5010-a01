# Assignment 01 — TA feedback: Jace (Jace Kasen)

- **Evaluated by:** Yilin Pan (TA)
- **Date:** 2026-10-05

## Scores

| Category | Pts | Student's LLM eval | TA verified | Notes |
|---|---|---|---|---|
| Immutability & encapsulation | 25 | 25 | 25 | |
| Constructor validation | 25 | 25 | 25 | |
| Correctness | 15 | 15 | 15 | |
| Testing | 15 | 13 | 14 | No Unicode whitespace test |
| Code quality & style | 10 | 9 | 10 | Accessor Javadoc came from the starter |
| Introspection, LLM eval & code walk | 10 | 6 | 10 | `Species` is graded from the code walk |
| **Total** | **100** | **93** | **99** | |

## Checks

- [x] All tests pass (66/66)
- [x] No `UnsupportedOperationException` stubs left
- [x] Checkstyle warnings: 0
- [x] Coverage (lines / branches): 100% / 100%
- [x] Nothing out of scope (inheritance, collections, `equals`/`hashCode`)
- [x] `submission/LLM-Evaluation.md` present
- [x] `submission/introspection.md` present

## Where the TA disagrees with the student's LLM evaluation

- Testing +1: the only gap I'd take a point for is Unicode whitespace. You chose `strip()`/`isBlank()`,
  but no test uses something like `"\u2003"` (an em space), so switching to `trim()` would still pass. The other
  two gaps your LLM listed are nice-to-haves.
- Code quality +1: the `species()`/`age()` Javadoc came from the starter.
- Cat 6 +4: explaining `Species` is part of the code walk, not the introspection, and your code walk covered it, so full credit.

## Feedback

Really nice work. Each rule has its own message, and there's a test for each one. Next time, test the
case your design choice exists for: if you pick `strip()`, add a test that `trim()` would fail.

## Grade

**Final grade:** 99 / 100
