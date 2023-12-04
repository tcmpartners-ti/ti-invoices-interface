package com.tcmp.tiapi.shared.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateFormatValidator.class)
public @interface ValidDateFormat {
  String message() default "Invalid date format";

  String pattern();

  Class<?>[] groups() default {};

  Class<?>[] payload() default {};
}

class ValidDateFormatValidator implements ConstraintValidator<ValidDateFormat, String> {
  private String pattern;

  @Override
  public void initialize(ValidDateFormat constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
    this.pattern = constraintAnnotation.pattern();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return true; // Null values should be handled by `@NotNull`

    try {
      //noinspection ResultOfMethodCallIgnored
      LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
