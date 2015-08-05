package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.Saga;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;

class DynamicSagaTypeBuilder {
    /**
     * Dynamically created classes will contain the following string in their type name.
     */
    static final String CONTAINED_IN_DYNAMIC_TYPE_CLASS_NAME = "$ByteBuddy$";

    private final Class<? extends Saga> sagaUnderTestClass;
    private DynamicType.Builder<? extends Saga> dynamicTypeBuilder;

    DynamicSagaTypeBuilder(final Class<? extends Saga> sagaUnderTestClass) {
        this.sagaUnderTestClass = sagaUnderTestClass;

        dynamicTypeBuilder = new ByteBuddy()
                .subclass(sagaUnderTestClass, ConstructorStrategy.Default.IMITATE_SUPER_TYPE_PUBLIC);
    }

    /**
     * Creates the builder that can build the sub class for the given {@code sagaUnderTestClass}.
     */
    static DynamicSagaTypeBuilder generateSubClassFor(final Class<? extends Saga> sagaUnderTestClass) {
        return new DynamicSagaTypeBuilder(sagaUnderTestClass);
    }

    /**
     * Defines a default constructor (if none is present) that calls a super constructor with all {@code null} arguments.
     */
    DynamicSagaTypeBuilder withDefaultNullPassingConstructor() {
        dynamicTypeBuilder = defineConstructorIfNecessary(sagaUnderTestClass, dynamicTypeBuilder);
        return this;
    }

    DynamicSagaTypeBuilder and(final Function<DynamicType.Builder<? extends Saga>, DynamicType.Builder<? extends Saga>> fluentBuilder) {
        dynamicTypeBuilder = fluentBuilder.apply(dynamicTypeBuilder);
        return this;
    }

    public Class<? extends Saga> buildAndLoad() {
        return dynamicTypeBuilder.make()
                                 .load(sagaUnderTestClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                                 .getLoaded();
    }

    /**
     * Defines a default constructor if the saga class does not already define one.
     * If a new constructor is defined this one will call its super constructor with all null arguments.
     * This of course means that no fields will be correctly initialized which is no problem because we do not directly interact with the generated type
     * instead we will normally only use it for delegating to the real sut instance.
     */
    private static DynamicType.Builder<? extends Saga> defineConstructorIfNecessary(
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

    static boolean isDynamicSubClass(final Saga saga) {
        return saga.getClass().getName().contains(CONTAINED_IN_DYNAMIC_TYPE_CLASS_NAME);
    }
}


