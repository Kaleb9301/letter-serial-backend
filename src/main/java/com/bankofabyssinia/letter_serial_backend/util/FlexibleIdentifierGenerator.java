package com.bankofabyssinia.letter_serial_backend.util;

import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.UUID;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.IdentityGenerator;

import com.bankofabyssinia.letter_serial_backend.annotation.FlexibleId;

public class FlexibleIdentifierGenerator extends IdentityGenerator implements BeforeExecutionGenerator {

    private final Class<?> idType;

    public FlexibleIdentifierGenerator(FlexibleId annotation, Member member, GeneratorCreationContext context) {
        this.idType = context.getType().getReturnedClass();
    }

    @Override
    public boolean generatedOnExecution() {
        return idType != String.class;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        if (idType == String.class) {
            return UUID.randomUUID().toString();
        }
        return null;
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }
}
