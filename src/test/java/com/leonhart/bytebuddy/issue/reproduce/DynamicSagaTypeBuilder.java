package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.Saga;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;

/**
 * A builder that uses byte buddy to dynamically build a sub class of the saga under test class.
 * This can be used to intercept calls and prepare context dependent setup before the actual call to the sut.
 */
final class DynamicSagaTypeBuilder {

    /**
     * Defines a default constructor if the saga class does not already define one.
     * If a new constructor is defined this one will call its super constructor with all null arguments.
     * This of course means that no fields will be correctly initialized which is no problem because we do not directly interact with the generated type
     * instead we will only use it for delegating to the real sut instance.
     */
    static DynamicType.Builder<? extends Saga> defineConstructorIfNecessary(
            final Class<? extends Saga> sagaClass,
            final DynamicType.Builder<? extends Saga> dynamicTypeBuilder) {
        List<Constructor<?>> publicConstructors = Arrays.asList(sagaClass.getConstructors());
        Optional<Constructor<?>> defaultCtor = findDefaultCtor(publicConstructors.stream());

        DynamicType.Builder<? extends Saga> subClassBuilder;
        if (defaultCtor.isPresent()) {
            subClassBuilder = dynamicTypeBuilder;
        } else {
            Constructor<?> anyBaseConstructor = publicConstructors.stream().findFirst().orElseThrow(
                    () -> new UnsupportedOperationException("No public constructor present. Type must have at least one public constructor."));

            subClassBuilder = defineConstructorWithSuperInvocation(dynamicTypeBuilder, anyBaseConstructor);
        }

        return subClassBuilder;
    }

    /**
     * Defines a public default constructor (no arguments) and calls the given super constructor with all null arguments.
     */
    private static DynamicType.Builder<? extends Saga> defineConstructorWithSuperInvocation(
            final DynamicType.Builder<? extends Saga> dynamicTypeBuilder,
            final Constructor<?> superConstructor) {
        return dynamicTypeBuilder
                .defineConstructor(Collections.<Class<?>>emptyList(), Visibility.PUBLIC)
                .intercept(
                        MethodCall.invoke(superConstructor)
                                  .onSuper()
                                  .with(createNullObjectParametersFor(superConstructor)));
    }

    private static Optional<Constructor<?>> findDefaultCtor(final Stream<Constructor<?>> constructorStream) {
        return constructorStream.filter(DynamicSagaTypeBuilder::isDefaultConstructor).findFirst();
    }

    private static boolean isDefaultConstructor(final Constructor<?> constructor) {
        return constructor.getParameterCount() == 0;
    }

    private static Object[] createNullObjectParametersFor(final Constructor<?> constructor) {
        return new Object[constructor.getParameterCount()];
    }
}

