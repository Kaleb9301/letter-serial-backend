package com.bankofabyssinia.letter_serial_backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

import com.bankofabyssinia.letter_serial_backend.util.FlexibleIdentifierGenerator;

@IdGeneratorType(FlexibleIdentifierGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface FlexibleId {
}
