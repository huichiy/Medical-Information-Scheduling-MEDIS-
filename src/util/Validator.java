package util;

import java.time.LocalDateTime;

public class Validator {
    public static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isAgeValid(int age) {
        return age >= 0 && age <= 150;
    }

    public static boolean isFutureDate(LocalDateTime dt) {
        return dt != null && dt.isAfter(LocalDateTime.now());
    }
}
