/*
 * Copyright 2012-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.context.bean.override.mockito;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.mockito.Answers;
import org.mockito.MockSettings;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.core.style.ToStringCreator;
import org.springframework.lang.Nullable;
import org.springframework.test.context.bean.override.BeanOverrideStrategy;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import static org.mockito.Mockito.mock;

/**
 * A complete definition that can be used to create a Mockito mock.
 *
 * @author Phillip Webb
 * @since 6.2
 */
class MockDefinition extends Definition {

	private final Set<Class<?>> extraInterfaces;

	private final Answers answer;

	private final boolean serializable;


	MockDefinition(MockitoBean annotation, Field field, ResolvableType typeToMock) {
		this(annotation.name(), annotation.reset(), field, annotation, typeToMock,
				annotation.extraInterfaces(), annotation.answers(), annotation.serializable());
	}

	MockDefinition(String name, MockReset reset, Field field, Annotation annotation, ResolvableType typeToMock,
			Class<?>[] extraInterfaces, @Nullable Answers answer, boolean serializable) {

		super(name, reset, false, field, annotation, typeToMock, BeanOverrideStrategy.REPLACE_OR_CREATE_DEFINITION);
		Assert.notNull(typeToMock, "TypeToMock must not be null");
		this.extraInterfaces = asClassSet(extraInterfaces);
		this.answer = (answer != null) ? answer : Answers.RETURNS_DEFAULTS;
		this.serializable = serializable;
	}


	@Override
	public String getBeanOverrideDescription() {
		return "mock";
	}

	@Override
	protected Object createOverride(String beanName, BeanDefinition existingBeanDefinition, Object existingBeanInstance) {
		return createMock(beanName);
	}

	private Set<Class<?>> asClassSet(Class<?>[] classes) {
		Set<Class<?>> classSet = new LinkedHashSet<>();
		if (classes != null) {
			classSet.addAll(Arrays.asList(classes));
		}
		return Collections.unmodifiableSet(classSet);
	}

	/**
	 * Return the extra interfaces.
	 * @return the extra interfaces or an empty set
	 */
	Set<Class<?>> getExtraInterfaces() {
		return this.extraInterfaces;
	}

	/**
	 * Return the answers mode.
	 * @return the answers mode; never {@code null}
	 */
	Answers getAnswer() {
		return this.answer;
	}

	/**
	 * Return if the mock is serializable.
	 * @return if the mock is serializable
	 */
	boolean isSerializable() {
		return this.serializable;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		MockDefinition other = (MockDefinition) obj;
		boolean result = super.equals(obj);
		result = result && ObjectUtils.nullSafeEquals(typeToOverride(), other.typeToOverride());
		result = result && ObjectUtils.nullSafeEquals(this.extraInterfaces, other.extraInterfaces);
		result = result && ObjectUtils.nullSafeEquals(this.answer, other.answer);
		result = result && this.serializable == other.serializable;
		return result;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = MULTIPLIER * result + ObjectUtils.nullSafeHashCode(typeToOverride());
		result = MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.extraInterfaces);
		result = MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.answer);
		result = MULTIPLIER * result + Boolean.hashCode(this.serializable);
		return result;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this)
				.append("name", this.name)
				.append("typeToMock", typeToOverride())
				.append("extraInterfaces", this.extraInterfaces)
				.append("answer", this.answer)
				.append("serializable", this.serializable)
				.append("reset", getReset())
				.toString();
	}

	<T> T createMock() {
		return createMock(this.name);
	}

	@SuppressWarnings("unchecked")
	<T> T createMock(String name) {
		MockSettings settings = MockReset.withSettings(getReset());
		if (StringUtils.hasLength(name)) {
			settings.name(name);
		}
		if (!this.extraInterfaces.isEmpty()) {
			settings.extraInterfaces(ClassUtils.toClassArray(this.extraInterfaces));
		}
		settings.defaultAnswer(this.answer);
		if (this.serializable) {
			settings.serializable();
		}
		return (T) mock(typeToOverride().resolve(), settings);
	}

}
