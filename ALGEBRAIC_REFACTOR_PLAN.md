# Algebraic Interface Refactoring Plan

## Executive Summary

**Goal:** Remove algebraic interfaces (`PlusMonoid`, `MultMonoid`, `Ring`, etc.) from Java type hierarchy and move all algebraic operations to the instruction layer.

**Why:** Type refinements (e.g., `nat`, `int[?>0]`) may not satisfy algebraic properties that their base types do. The Java type system can't express conditional algebraic properties, leading to incorrect guarantees.

**Solution:**
1. Create `Algebras` static helper class for type-based algebraic checks and operations
2. Remove all algebraic interfaces from Obj types
3. Remove all algebraic methods (`plus()`, `mult()`, `zero()`, `one()`, etc.)
4. Replace all usage with instruction-based operations via `Algebras` helper

**Impact:**
- **Files to create:** 1 (Algebras.java)
- **Files to modify:** 22 (13 type interfaces + 9 usage updates)
- **Instructions to add:** 16 (ZERO_INST_TID and ONE_INST_TID for 8 types)
- **Estimated effort:** 6-9 hours
- **Risk level:** Medium (lots of compilation errors to fix, but straightforward)

---

## Problem Statement

The current implementation has algebraic interfaces (`PlusMonoid`, `MultMonoid`, `Ring`, etc.) implemented at the Java type level. This creates issues with type refinements where algebraic properties may not hold.

**Example Issue:**
- `Int` implements `Ring.O<Int>` with `plus()` and `mult()` methods
- But `nat` (natural numbers) is a refinement of `Int` that doesn't form a ring (no additive inverses)
- The Java type system can't express "this type forms a ring only under certain conditions"

**Solution:** Remove algebraic operations from Java API and handle them exclusively through the instruction layer where runtime type checking and constraint validation can occur.

---

## Types Implementing Algebraic Interfaces

### 1. **Rel** - `Poly<Rel, Tuple.Pair<Obj, Obj>>, MultMonoid.O<Rel>, PlusMonoid.O<Rel>`
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Rel.java:50`
   - **Methods to remove:**
     - `Rel plus(Rel rhs)` (line 243)
     - `Rel mult(Rel rhs)` (line 209)
     - `Rel neg()` (line 233)
     - `Rel one()` (line 167)
     - `Rel zero()` (line 190)
     - `boolean isOne()` (line 176)
     - `boolean isZero()` (line 199)
   - **Keep helper methods:** `one()`, `zero()`, `isOne()`, `isZero()` (useful for instruction implementations)
   - **Instructions already handle:** `PLUS_INST_TID`, `MULT_INST_TID`, `NEG_INST_TID`, `ONE_INST_TID`, `ZERO_INST_TID`

### 2. **Int** - `Mono, Ring.O<Int>`
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Int.java:47`
   - **Methods to remove:**
     - `Int plus(Int rhs)` (line 88)
     - `Int mult(Int rhs)` (line 93)
     - `Int neg()` (line 98)
   - **Keep helper methods:** `zero()` (line 78), `one()` (line 83)
   - **Instructions already handle:** `PLUS_INST_TID`, `MULT_INST_TID`, `NEG_INST_TID` (lines 116-118)

### 3. **Real** - `Mono, Ring.O<Real>, MultGroup.O<Real>`
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Real.java:42`
   - **Methods to remove:**
     - `Real plus(Real rhs)` (line 94)
     - `Real mult(Real rhs)` (line 99)
     - `Real neg()` (line 103)
     - `Real inv()` (line 84)
     - `Real div(Real rhs)` (line 89)
   - **Keep helper methods:** `zero()` (line 74), `one()` (line 79)
   - **Instructions already handle:** `PLUS_INST_TID`, `MULT_INST_TID`, `NEG_INST_TID`, `DIV_INST_TID`, `INV_INST_TID` (lines 120-126)

### 4. **Uri** - `Mono, Ring.O<Uri>`
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Uri.java:50`
   - **Methods to remove:**
     - `Uri plus(Uri rhs)` (line 111)
     - `Uri mult(Uri rhs)` (line 100)
     - `Uri neg()` (line 116)
   - **Keep helper methods:** `zero()` (line 106), `one()` (line 95)
   - **Instructions already handle:** `PLUS_INST_TID`, `MULT_INST_TID` (lines 179-180)

### 5. **Str** - `Mono, PlusMonoid.O<Str>`
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Str.java:49`
   - **Methods to remove:**
     - `Str plus(Str rhs)` (line 105) - **NOTE: No plus() method found in file!**
   - **Keep helper methods:** `zero()` (line 73)
   - **Instructions already handle:** `PLUS_INST_TID` (line 103)

### 6. **Lst** - `Poly<Lst, List<Obj>>, PlusMonoid.O<Lst>`
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Lst.java:53`
   - **Methods to remove:**
     - `Lst plus(Lst rhs)` (line 183)
   - **Keep helper methods:** `zero()` (line 178)
   - **Instructions already handle:** `PLUS_INST_TID` (line 234)

### 7. **Rec** - `Poly<Rec, Map<Obj, Obj>>, PlusMonoid.O<Rec>`
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Rec.java:51`
   - **Methods to remove:**
     - `Rec plus(Rec rhs)` (line 172)
   - **Keep helper methods:** `zero()` (line 62)
   - **Instructions already handle:** `PLUS_INST_TID` (line 252)

### 8. **Bytes** - `Mono, PlusMonoid.O<Bytes>`
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Bytes.java:43`
   - **Methods to remove:**
     - `Bytes plus(Bytes rhs)` (line 76)
   - **Keep helper methods:** `zero()` (line 71)
   - **Instructions already handle:** `PLUS_INST_TID` (line 112)

### 9. **Objs** - `Obj, PlusMonoid.O<Objs>`
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Objs.java:40`
   - **Methods to remove:**
     - `Objs plus(Objs other)` (line 102)
   - **Keep helper methods:** `zero()` (line 97)
   - **Instructions:** No PLUS_INST_TID found (may need to add)

### 10. **Call** - `Obj, Ring<Call>`
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Call.java:36`
   - **Methods to remove:**
     - `Call plus(Call rhs)` (line 137)
     - `Call mult(Call rhs)` (line 149)
     - `Call zero()` (line 132)
     - `Call one()` (line 144)
     - `Call neg()` (line 154)
   - **Instructions:** Need to check if instructions exist

### 11. **Fail** - `Obj, PlusMonoid<Fail>`
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Fail.java:42`
   - **Methods to remove:**
     - `Fail plus(Fail rhs)` (line 52) - declared but implementation not shown
   - **Instructions:** Need to check if instructions exist

### 12. **Bool** - `Mono` (NO algebraic interfaces!)
   - **Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Bool.java:39`
   - **Has helper methods:** `zero()` (line 65), `one()` (line 69)
   - **Instructions handle:** `PLUS_INST_TID` (line 81), `MULT_INST_TID` (line 82)
   - **Action:** None needed - already correct!

---

## Current Usage Analysis

### isZero() and isOne() Usage (60 matches):

**Most are on cInt (coefficient type) - these should REMAIN:**
- `fURI.java` - 3 matches on coefficient operations
- `cInt.java` - 2 matches on coefficient operations
- `AbstractfURI.java` - 5 matches on coefficient operations
- `Inst.java` - 8 matches on coefficient/type checking
- `MObjs.java` - 2 matches on coefficient operations
- `LazyObjs.java` - 1 match on coefficient operations
- `MUri.java` - 2 matches on coefficient checking
- Various machine/router files - coefficient checking

**On Obj types - need to replace with Algebras.isZero()/isOne():**
- `Rel.java` - 17 matches (lines 84, 88, 211-213, 245-246, 281-282, 287-288, 293, 297, 299, 301, 306, 308)
- `Call.java` - 5 matches (lines 137-138, 146, 148-149)
- `Obj.java` - 1 match (line 371) - `this.c().isZero()` (this is on coefficient, keep as-is)

**isPlusMonoid() Usage (1 match):**
- `Rec.java:176` - Checks if objects are PlusMonoid before calling `.plus()`

**isRing(), isMultMonoid() Usage:**
- No direct usage found in type code

### ZERO_INST_TID and ONE_INST_TID Coverage:

**Types WITH both ZERO_INST_TID and ONE_INST_TID:**
- Int ✓
- Real ✓
- Rel ✓

**Types WITH only ZERO_INST_TID:**
- Str (line 89)
- Lst (line 229)
- Rec (line 235)
- Bytes (line 111)

**Types MISSING both:**
- Uri (has `zero()` and `one()` methods but no instructions!)
- Bool (has `zero()` and `one()` methods but no instructions!)
- Objs (has `zero()` method but no instruction!)
- Call (has `zero()` and `one()` methods but no instructions!)
- Fail (no zero/one at all)

## Code That Uses Algebraic Methods

### Direct Method Calls Found (69 matches):

1. **Rec.java** (line 176): Uses `isPlusMonoid()` check and calls `.plus()`
   ```java
   v.isPlusMonoid() && o.isPlusMonoid() ? (Obj) v.<PlusMonoid.O>as().plus(o.jvm().get1().<PlusMonoid.O>as()) :
   ```
   - **Action:** Replace with instruction-based approach

2. **Objs.java** (line 105): Calls `.plus()` on PlusMonoid
   ```java
   final PlusMonoid.O<?> result = ... ((PlusMonoid.O) first).plus((PlusMonoid.O) second);
   ```
   - **Action:** Replace with instruction-based approach

3. **Call.java** (lines 112, 140, 152): Uses `.plus()` and `.mult()`
   - **Action:** Replace with instruction-based approach

4. **Str.java** (line 109): Uses `.plus()` in WITHIN_INST_TID
   ```java
   .reduce((a, b) -> (PlusMonoid.O) a.plus(b))
   ```
   - **Action:** Replace with instruction-based approach

5. **Multiple c() operations** using `.mult()`, `.plus()`, `.div()` on cInt
   - These are on the coefficient type, not Obj types
   - **Action:** These should remain as they are internal to the coefficient system

6. **SUM_INST_TID and PROD_INST_TID implementations** use `.plus()` and `.mult()`
   - Found in: Int.java, Real.java, Uri.java, Lst.java
   - **Action:** Replace with instruction application instead of direct method calls

---

## Obj.java Helper Methods

**Location:** `src/main/java/studio/phaseshift/metatron/isa/m/type/Obj.java`

These type-checking methods exist:
- `boolean isRing()` (line 422)
- `boolean isPlusMonoid()` (line 426)
- `boolean isMultMonoid()` (line 430)

**Action:** These can remain as they're useful for runtime type checking, but they should be used carefully since removing the interfaces means these will return false.

---

## Updated Refactoring Steps (Based on User Feedback)

### Phase 0: Create Algebras Helper Class
**Priority: HIGH - Do this first**

Create `src/main/java/studio/phaseshift/metatron/isa/m/type/Algebras.java`:

```java
package studio.phaseshift.metatron.isa.m.type;

import static studio.phaseshift.metatron.isa.m.mInstSet.*;
import static studio.phaseshift.metatron.isa.m.type.impl.MInst.instB;
import static studio.phaseshift.metatron.isa.m.type.impl.MLst.lst;

public class Algebras {

    // Type-based algebraic property checks
    public static boolean isPlusMonoid(Obj obj) {
        // Check if obj's type supports PLUS_INST_TID
        return obj.type().insts().stream()
            .anyMatch(inst -> inst.tid().basePath().equals(PLUS_INST_TID));
    }

    public static boolean isMultMonoid(Obj obj) {
        // Check if obj's type supports MULT_INST_TID
        return obj.type().insts().stream()
            .anyMatch(inst -> inst.tid().basePath().equals(MULT_INST_TID));
    }

    public static boolean isRing(Obj obj) {
        return isPlusMonoid(obj) && isMultMonoid(obj) && hasNeg(obj);
    }

    public static boolean hasNeg(Obj obj) {
        return obj.type().insts().stream()
            .anyMatch(inst -> inst.tid().basePath().equals(NEG_INST_TID));
    }

    // Identity element checks via instructions
    public static boolean isZero(Obj obj) {
        try {
            Obj zero = obj.apply(instB(ZERO_INST_TID.dom(obj.tid()).rng(obj.tid()), lst()));
            return obj.equals(zero);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isOne(Obj obj) {
        try {
            Obj one = obj.apply(instB(ONE_INST_TID.dom(obj.tid()).rng(obj.tid()), lst()));
            return obj.equals(one);
        } catch (Exception e) {
            return false;
        }
    }

    // Algebraic operation utilities
    public static Obj plus(Obj a, Obj b) {
        return a.apply(instB(PLUS_INST_TID.dom(a.tid()).rng(a.tid()), lst(b)));
    }

    public static Obj mult(Obj a, Obj b) {
        return a.apply(instB(MULT_INST_TID.dom(a.tid()).rng(a.tid()), lst(b)));
    }

    public static Obj neg(Obj a) {
        return a.apply(instB(NEG_INST_TID.dom(a.tid()).rng(a.tid()), lst()));
    }

    public static Obj zero(Obj obj) {
        return obj.apply(instB(ZERO_INST_TID.dom(obj.tid()).rng(obj.tid()), lst()));
    }

    public static Obj one(Obj obj) {
        return obj.apply(instB(ONE_INST_TID.dom(obj.tid()).rng(obj.tid()), lst()));
    }
}
```

### Phase 1: Add Missing Instructions
**Priority: HIGH - Do before removing methods**

Add missing `ZERO_INST_TID` and `ONE_INST_TID` instructions:

1. **Uri** - Add both ZERO_INST_TID and ONE_INST_TID
2. **Bool** - Add both ZERO_INST_TID and ONE_INST_TID
3. **Str** - Add ONE_INST_TID (already has ZERO)
4. **Lst** - Add ONE_INST_TID (already has ZERO)
5. **Rec** - Add ONE_INST_TID (already has ZERO)
6. **Bytes** - Add ONE_INST_TID (already has ZERO)
7. **Objs** - Add both ZERO_INST_TID and ONE_INST_TID
8. **Call** - Add both ZERO_INST_TID and ONE_INST_TID

### Phase 2: Update Type Declarations
1. Remove algebraic interface extensions from each type:
   - `Rel`: Remove `MultMonoid.O<Rel>, PlusMonoid.O<Rel>`
   - `Int`: Remove `Ring.O<Int>`
   - `Real`: Remove `Ring.O<Real>, MultGroup.O<Real>`
   - `Uri`: Remove `Ring.O<Uri>`
   - `Str`: Remove `PlusMonoid.O<Str>`
   - `Lst`: Remove `PlusMonoid.O<Lst>`
   - `Rec`: Remove `PlusMonoid.O<Rec>`
   - `Bytes`: Remove `PlusMonoid.O<Bytes>`
   - `Objs`: Remove `PlusMonoid.O<Objs>`
   - `Call`: Remove `Ring<Call>`
   - `Fail`: Remove `PlusMonoid<Fail>`

### Phase 3: Remove Algebraic Methods
For each type, remove ALL algebraic methods including helper methods:
- `plus()`
- `mult()`
- `neg()`
- `inv()` (Real only)
- `div()` (Real only)
- `minus()` (if explicitly defined)
- `zero()` - **REMOVE** (use Algebras.zero() instead)
- `one()` - **REMOVE** (use Algebras.one() instead)
- `isZero()` - **REMOVE** (use Algebras.isZero() instead)
- `isOne()` - **REMOVE** (use Algebras.isOne() instead)

### Phase 4: Update Obj Interface
Remove type-checking methods from Obj interface:
- Remove `boolean isRing()` (line 422)
- Remove `boolean isPlusMonoid()` (line 426)
- Remove `boolean isMultMonoid()` (line 430)

These are replaced by `Algebras.isRing()`, `Algebras.isPlusMonoid()`, `Algebras.isMultMonoid()`.

### Phase 5: Update Code Using Algebraic Methods
Replace direct method calls with instruction-based operations:

**Pattern to replace:**
```java
// OLD
obj1.plus(obj2)
obj1.mult(obj2)
obj1.neg()
obj1.zero()
obj1.one()
obj1.isZero()
obj1.isOne()
obj.isPlusMonoid()

// NEW (using Algebras helper)
Algebras.plus(obj1, obj2)
Algebras.mult(obj1, obj2)
Algebras.neg(obj1)
Algebras.zero(obj1)
Algebras.one(obj1)
Algebras.isZero(obj1)
Algebras.isOne(obj1)
Algebras.isPlusMonoid(obj)
```

**Critical Locations to update:**

1. **Rec.java:176** - Replace `isPlusMonoid()` check and `.plus()` call:
   ```java
   // OLD
   v.isPlusMonoid() && o.isPlusMonoid() ? (Obj) v.<PlusMonoid.O>as().plus(o.jvm().get1().<PlusMonoid.O>as()) :

   // NEW
   Algebras.isPlusMonoid(v) && Algebras.isPlusMonoid(o) ? Algebras.plus(v, o.jvm().get1()) :
   ```

2. **Objs.java:105** - Replace `.plus()` call:
   ```java
   // OLD
   final PlusMonoid.O<?> result = ... ((PlusMonoid.O) first).plus((PlusMonoid.O) second);

   // NEW
   final Obj result = ... Algebras.plus(first, second);
   ```

3. **Call.java:112, 140, 152** - Replace `.plus()` and `.mult()` calls with Algebras methods

4. **Str.java:109** - Replace `.plus()` in reduce:
   ```java
   // OLD
   .reduce((a, b) -> (PlusMonoid.O) a.plus(b))

   // NEW
   .reduce((a, b) -> Algebras.plus(a, b))
   ```

5. **All SUM_INST_TID implementations** - Replace `.plus()`:
   - Int.java:124
   - Real.java:128
   - Uri.java:181
   - Lst.java:236

6. **All PROD_INST_TID implementations** - Replace `.mult()`:
   - Uri.java:182

7. **Rel.java** - Replace all `isZero()` and `isOne()` calls (17 locations):
   - Lines 211-213, 245-246, 281-282, 287-288, 293, 297, 299, 301, 306, 308

8. **Call.java** - Replace all `isZero()` and `isOne()` calls (5 locations):
   - Lines 137-138, 146, 148-149

9. **All instruction implementations** that call `.zero()` or `.one()`:
   - Replace with `Algebras.zero(lhs)` or `Algebras.one(lhs)`

### Phase 6: Testing
1. Run all existing tests
2. Verify instruction-based operations work correctly
3. Verify type refinements don't break
4. Check that helper methods (`zero()`, `one()`, etc.) still work

---

## Summary of Changes

### Files to Create:
1. `src/main/java/studio/phaseshift/metatron/isa/m/type/Algebras.java` - New static helper class

### Files to Modify (Type Interfaces):
1. `Rel.java` - Remove interfaces, remove methods, update instruction implementations
2. `Int.java` - Remove interfaces, remove methods, update instruction implementations
3. `Real.java` - Remove interfaces, remove methods, update instruction implementations
4. `Uri.java` - Remove interfaces, remove methods, ADD instructions, update implementations
5. `Str.java` - Remove interfaces, remove methods, ADD ONE_INST_TID, update implementations
6. `Lst.java` - Remove interfaces, remove methods, ADD ONE_INST_TID, update implementations
7. `Rec.java` - Remove interfaces, remove methods, ADD ONE_INST_TID, update implementations
8. `Bytes.java` - Remove interfaces, remove methods, ADD ONE_INST_TID, update implementations
9. `Objs.java` - Remove interfaces, remove methods, ADD instructions, update implementations
10. `Call.java` - Remove interfaces, remove methods, ADD instructions, update implementations
11. `Fail.java` - Remove interfaces, remove methods
12. `Bool.java` - ADD instructions (already doesn't have algebraic interfaces!)
13. `Obj.java` - Remove `isRing()`, `isPlusMonoid()`, `isMultMonoid()` methods

### Files to Modify (Usage Updates):
1. `Rec.java` - Update `.plus()` and `isPlusMonoid()` usage
2. `Objs.java` - Update `.plus()` usage
3. `Call.java` - Update `.plus()`, `.mult()`, `.isZero()`, `.isOne()` usage
4. `Str.java` - Update `.plus()` in reduce
5. `Rel.java` - Update all `.isZero()` and `.isOne()` calls (17 locations)
6. `Int.java` - Update SUM_INST_TID implementation
7. `Real.java` - Update SUM_INST_TID implementation
8. `Uri.java` - Update SUM_INST_TID and PROD_INST_TID implementations
9. `Lst.java` - Update SUM_INST_TID implementation

### Instruction Additions Needed:
- **Uri**: ZERO_INST_TID, ONE_INST_TID
- **Bool**: ZERO_INST_TID, ONE_INST_TID
- **Str**: ONE_INST_TID
- **Lst**: ONE_INST_TID
- **Rec**: ONE_INST_TID
- **Bytes**: ONE_INST_TID
- **Objs**: ZERO_INST_TID, ONE_INST_TID
- **Call**: ZERO_INST_TID, ONE_INST_TID

---

## Benefits of This Refactoring

1. **Type Safety:** Java type system won't make false promises about algebraic properties
2. **Flexibility:** Instructions can validate constraints at runtime
3. **Refinement Support:** Type refinements can have specialized instruction implementations
4. **Consistency:** All operations go through the same instruction layer
5. **Correctness:** Prevents invalid operations on refined types (e.g., `nat.neg()`)

---

## Potential Issues & Mitigations

1. **Performance:** Instruction-based calls may be slower than direct method calls
   - **Mitigation:** Profile after refactoring, optimize hot paths if needed
   - **Note:** Most algebraic operations are not in tight loops

2. **Code Complexity:** More verbose code when performing algebraic operations
   - **Mitigation:** `Algebras` helper class makes it cleaner: `Algebras.plus(a, b)` vs `a.apply(inst(...))`

3. **Breaking Changes:** Any external code using these methods will break
   - **Mitigation:** This is internal refactoring, no external API guarantees yet

4. **Instruction Lookup Overhead:** Creating instructions on-the-fly has overhead
   - **Mitigation:** Consider caching instruction instances in `Algebras` class

5. **Type Checking Changes:** `instanceof` checks will fail after removing interfaces
   - **Mitigation:** All checks go through `Algebras` which uses instruction availability

---

## Implementation Order & Effort Estimate

### Recommended Order:

1. **Phase 0: Create Algebras class** (30 min)
   - Create the file with all static methods
   - Test basic functionality

2. **Phase 1: Add missing instructions** (1-2 hours)
   - Add ZERO_INST_TID and ONE_INST_TID to 8 types
   - Test each instruction works correctly

3. **Phase 2: Update type declarations** (30 min)
   - Remove `extends` clauses from 11 types
   - This will cause compilation errors - that's expected!

4. **Phase 3: Remove algebraic methods** (1 hour)
   - Remove all `plus()`, `mult()`, `neg()`, `zero()`, `one()`, etc.
   - More compilation errors - still expected!

5. **Phase 4: Update Obj interface** (15 min)
   - Remove `isRing()`, `isPlusMonoid()`, `isMultMonoid()`

6. **Phase 5: Fix all compilation errors** (2-3 hours)
   - Update ~60 locations that use algebraic methods
   - Replace with `Algebras.*` calls
   - This is the bulk of the work

7. **Phase 6: Testing** (1-2 hours)
   - Run test suite
   - Fix any runtime issues
   - Verify instruction-based operations work

**Total Estimated Time: 6-9 hours**

### Risk Assessment:

- **Low Risk:** Creating Algebras class, adding instructions
- **Medium Risk:** Removing interfaces and methods (lots of compilation errors to fix)
- **High Risk:** Performance impact (needs profiling after completion)

---

## Answers from User

### #1 - Zero/One Helper Methods
**Decision:** Remove `zero()` and `one()` from Java API. Keep them at instruction level only.

**Reasoning:** We can't assume zero is universal. For example, `int[?>0]` (positive integers) has no zero. Each type's zero/one should be accessed via `ZERO_INST_TID` and `ONE_INST_TID` instructions.

**Action Items:**
- Remove `zero()` and `one()` methods from all Obj types
- Keep `ZERO_INST_TID` and `ONE_INST_TID` instruction implementations
- Add missing `ONE_INST_TID` for types that don't have it (Uri, Str, Lst, Rec, Bytes, Objs)

### #2 - Type Checking Methods (isRing, isPlusMonoid, isMultMonoid)
**Decision:** Create a static helper class `Algebras` with public static methods.

**Design:**
```java
public class Algebras {
    public static boolean isRing(Obj obj) { /* check based on Type, not Java interface */ }
    public static boolean isPlusMonoid(Obj obj) { /* check based on Type */ }
    public static boolean isMultMonoid(Obj obj) { /* check based on Type */ }
    public static boolean isZero(Obj obj) { /* check via ZERO_INST_TID */ }
    public static boolean isOne(Obj obj) { /* check via ONE_INST_TID */ }
    // ... other algebraic checks
}
```

**Action Items:**
- Create `src/main/java/studio/phaseshift/metatron/isa/m/type/Algebras.java`
- Move `isZero()` and `isOne()` logic to static methods
- Implement type-based checking (not interface-based)
- Update all usages of `obj.isRing()`, `obj.isPlusMonoid()`, etc. to `Algebras.isRing(obj)`
- Remove `isRing()`, `isPlusMonoid()`, `isMultMonoid()` from Obj interface

### #3 - Utility Methods for Instruction-Based Operations
**Decision:** YES - create utilities beyond just the `Algebras` class.

**Potential utilities:**
- `Algebras.plus(Obj a, Obj b)` - applies PLUS_INST_TID
- `Algebras.mult(Obj a, Obj b)` - applies MULT_INST_TID
- `Algebras.neg(Obj a)` - applies NEG_INST_TID
- etc.

This makes the code more readable than manually creating instruction applications.

### #4 - Performance-Critical Sections
**Analysis needed:** Review codebase for hot paths where algebraic operations are called frequently.

**Identified Critical Sections:**
1. **Rec.plus()** (line 173-179) - Used in record merging, potentially hot path
2. **Objs.plus()** (line 102-107) - Used in object collection operations
3. **cInt operations** - These are coefficient operations, NOT Obj operations (should remain as-is)
4. **SUM_INST_TID/PROD_INST_TID** - Reduction operations that call `.plus()`/`.mult()` in loops

**Performance Concerns:**
- Instruction application has overhead (lookup, validation, execution)
- Direct method calls are faster but semantically incorrect for refined types
- Need to balance correctness vs performance

**Recommendation:**
- Start with correctness (all instruction-based)
- Profile after refactoring
- If performance issues arise, consider:
  - Caching instruction lookups
  - JIT optimization hints
  - Special-case optimizations for known types

---

## Quick Reference: Search & Replace Patterns

For quick implementation, use these patterns:

### Pattern 1: Remove Interface Extensions
```java
// SEARCH FOR:
extends.*(?:PlusMonoid|MultMonoid|Ring|MultGroup|PlusGroup)

// In each match, remove the algebraic interface(s)
```

### Pattern 2: Replace Method Calls
```java
// Direct replacements:
\.plus\(          → Algebras.plus(
\.mult\(          → Algebras.mult(
\.neg\(\)         → Algebras.neg(
\.zero\(\)        → Algebras.zero(
\.one\(\)         → Algebras.one(
\.isZero\(\)      → Algebras.isZero(
\.isOne\(\)       → Algebras.isOne(
\.isPlusMonoid\(\) → Algebras.isPlusMonoid(
\.isMultMonoid\(\) → Algebras.isMultMonoid(
\.isRing\(\)      → Algebras.isRing(
```

### Pattern 3: Method Signatures to Remove
```java
// Search for these method signatures and remove them:
default \w+ plus\(
default \w+ mult\(
default \w+ neg\(\)
default \w+ inv\(\)
default \w+ div\(
default \w+ zero\(\)
default \w+ one\(\)
default boolean isZero\(\)
default boolean isOne\(\)
```

### Pattern 4: Instruction Template
```java
// Template for adding ZERO_INST_TID:
instC(ZERO_INST_TID.dom(TYPE_TID).rng(TYPE_TID), lst(),
      (lhs, inst) -> /* return zero value */)

// Template for adding ONE_INST_TID:
instC(ONE_INST_TID.dom(TYPE_TID).rng(TYPE_TID), lst(),
      (lhs, inst) -> /* return one value */)
```

---

## Checklist for Implementation

- [ ] **Phase 0:** Create `Algebras.java` with all static methods
- [ ] **Phase 1:** Add missing instructions (16 total)
  - [ ] Uri: ZERO_INST_TID, ONE_INST_TID
  - [ ] Bool: ZERO_INST_TID, ONE_INST_TID
  - [ ] Str: ONE_INST_TID
  - [ ] Lst: ONE_INST_TID
  - [ ] Rec: ONE_INST_TID
  - [ ] Bytes: ONE_INST_TID
  - [ ] Objs: ZERO_INST_TID, ONE_INST_TID
  - [ ] Call: ZERO_INST_TID, ONE_INST_TID
- [ ] **Phase 2:** Remove interface extensions (11 types)
  - [ ] Rel, Int, Real, Uri, Str, Lst, Rec, Bytes, Objs, Call, Fail
- [ ] **Phase 3:** Remove algebraic methods (11 types)
  - [ ] Remove plus(), mult(), neg(), inv(), div(), zero(), one(), isZero(), isOne()
- [ ] **Phase 4:** Update Obj interface
  - [ ] Remove isRing(), isPlusMonoid(), isMultMonoid()
- [ ] **Phase 5:** Fix compilation errors (~60 locations)
  - [ ] Rec.java (isPlusMonoid + plus usage)
  - [ ] Objs.java (plus usage)
  - [ ] Call.java (plus, mult, isZero, isOne)
  - [ ] Str.java (plus in reduce)
  - [ ] Rel.java (17 isZero/isOne calls)
  - [ ] Int.java, Real.java, Uri.java, Lst.java (SUM_INST_TID)
  - [ ] Uri.java (PROD_INST_TID)
- [ ] **Phase 6:** Testing
  - [ ] Run full test suite
  - [ ] Verify instruction-based operations
  - [ ] Profile performance if needed

---

## Ready to Begin?

The research is complete! The plan is comprehensive and ready for implementation.

**Next steps:**
1. Review this plan with the team
2. Get approval to proceed
3. Start with Phase 0 (create Algebras class)
4. Work through phases sequentially
5. Test thoroughly after each phase

Good luck with the refactoring! 🚀
