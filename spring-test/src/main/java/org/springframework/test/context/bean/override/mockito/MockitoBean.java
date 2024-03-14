/*
 * Copyright 2002-2024 the original author or authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mockito.Answers;
import org.mockito.MockSettings;

import org.springframework.test.context.bean.override.BeanOverride;

/**
 * Mark a field to trigger a bean override using a Mockito mock. If no explicit
 * {@link #name()} is specified, the annotated field's name is interpreted to
 * be the target of the override. In either case, if no existing bean is defined
 * a new one will be added to the context.
 *
 * <p>Dependencies that are known to the application context but are not beans
 * (such as those
 * {@link org.springframework.beans.factory.config.ConfigurableListableBeanFactory#registerResolvableDependency(Class, Object)
 * registered directly}) will not be found, and a mocked bean will be added to
 * the context alongside the existing dependency.
 *
 * @author Simon Baslé
 * @since 6.2
 * @see MockitoSpyBean
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BeanOverride(MockitoBeanOverrideProcessor.class)
public @interface MockitoBean {

	/**
	 * The name of the bean to register or replace.
	 * <p>If not specified, the name of the annotated field will be used.
	 * @return the name of the mocked bean
	 */
	String name() default "";

	/**
	 * Extra interfaces that should also be declared on the mock.
	 * <p>Defaults to none.
	 * @return any extra interfaces
	 * @see MockSettings#extraInterfaces(Class...)
	 */
	Class<?>[] extraInterfaces() default {};

	/**
	 * The {@link Answers} type to use on the mock.
	 * <p>Defaults to {@link Answers#RETURNS_DEFAULTS}.
	 * @return the answer type
	 */
	Answers answers() default Answers.RETURNS_DEFAULTS;

	/**
	 * Whether the generated mock is serializable.
	 * <p>Defaults to {@code false}.
	 * @return {@code true} if the mock is serializable
	 * @see MockSettings#serializable()
	 */
	boolean serializable() default false;

	/**
	 * The reset mode to apply to the mock.
	 * <p>The default is {@link MockReset#AFTER} meaning that mocks are
	 * automatically reset after each test method is invoked.
	 * @return the reset mode
	 */
	MockReset reset() default MockReset.AFTER;

}
