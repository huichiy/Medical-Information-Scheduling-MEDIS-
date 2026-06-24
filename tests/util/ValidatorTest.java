package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

class ValidatorTest {
    @Test void isNotBlank_rejectsNull()       { assertFalse(Validator.isNotBlank(null)); }
    @Test void isNotBlank_rejectsEmpty()      { assertFalse(Validator.isNotBlank("")); }
    @Test void isNotBlank_rejectsWhitespace() { assertFalse(Validator.isNotBlank("   ")); }
    @Test void isNotBlank_acceptsRealText()   { assertTrue (Validator.isNotBlank("Alice")); }

    @Test void isAgeValid_rejectsNegative()   { assertFalse(Validator.isAgeValid(-1)); }
    @Test void isAgeValid_rejectsTooHigh()    { assertFalse(Validator.isAgeValid(151)); }
    @Test void isAgeValid_acceptsZero()       { assertTrue (Validator.isAgeValid(0)); }
    @Test void isAgeValid_acceptsNormal()     { assertTrue (Validator.isAgeValid(30)); }
    @Test void isAgeValid_acceptsUpperBound() { assertTrue (Validator.isAgeValid(150)); }

    @Test void isFutureDate_rejectsPast()     { assertFalse(Validator.isFutureDate(LocalDateTime.now().minusDays(1))); }
    @Test void isFutureDate_acceptsFuture()   { assertTrue (Validator.isFutureDate(LocalDateTime.now().plusDays(1))); }
    @Test void isFutureDate_rejectsNull()     { assertFalse(Validator.isFutureDate(null)); }
}
