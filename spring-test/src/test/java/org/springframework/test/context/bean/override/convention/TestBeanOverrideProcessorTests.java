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

package org.springframework.test.context.bean.override.convention;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.test.context.bean.override.convention.TestBeanOverrideProcessor.MethodConventionOverrideMetadata;
import org.springframework.test.context.bean.override.example.ExampleService;
import org.springframework.test.context.bean.override.example.FailingExampleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.springframework.test.context.bean.override.convention.TestBeanOverrideProcessor.findTestBeanFactoryMethod;

/**
 * Tests for {@link TestBeanOverrideProcessor}.
 *
 * @author Simon Baslé
 * @author Sam Brannen
 * @since 6.2
 */
class TestBeanOverrideProcessorTests {

	@Test
	void findTestBeanFactoryMethodFindsFromCandidateNames() {
		Class<?> clazz = MethodConventionConf.class;
		Class<?> returnType = ExampleService.class;

		Method method = findTestBeanFactoryMethod(clazz, returnType, "example1", "example2", "example3");

		assertThat(method.getName()).isEqualTo("example2");
	}

	@Test
	void findTestBeanFactoryMethodNotFound() {
		Class<?> clazz = MethodConventionConf.class;
		Class<?> returnType = ExampleService.class;

		assertThatIllegalStateException()
				.isThrownBy(() -> findTestBeanFactoryMethod(clazz, returnType, "example1", "example3"))
				.withMessage("""
						Failed to find a static test bean factory method in %s with return type %s \
						whose name matches one of the supported candidates %s""",
						clazz.getName(), returnType.getName(), List.of("example1", "example3"));
	}

	@Test
	void findTestBeanFactoryMethodTwoFound() {
		Class<?> clazz = MethodConventionConf.class;
		Class<?> returnType = ExampleService.class;

		assertThatIllegalStateException()
				.isThrownBy(() -> findTestBeanFactoryMethod(clazz, returnType, "example2", "example4"))
				.withMessage("""
						Found %d competing static test bean factory methods in %s with return type %s \
						whose name matches one of the supported candidates %s""".formatted(
								2, clazz.getName(), returnType.getName(), List.of("example2", "example4")));
	}

	@Test
	void findTestBeanFactoryMethodNoNameProvided() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> findTestBeanFactoryMethod(MethodConventionConf.class, ExampleService.class))
				.withMessage("At least one candidate method name is required");
	}

	@Test
	void createMetaDataForUnknownExplicitMethod() throws Exception {
		Class<?> clazz = ExplicitMethodNameConf.class;
		Class<?> returnType = ExampleService.class;
		Field field = clazz.getField("a");
		TestBean overrideAnnotation = field.getAnnotation(TestBean.class);
		assertThat(overrideAnnotation).isNotNull();

		TestBeanOverrideProcessor processor = new TestBeanOverrideProcessor();
		assertThatIllegalStateException()
				.isThrownBy(() -> processor.createMetadata(field, overrideAnnotation, ResolvableType.forClass(returnType)))
				.withMessage("""
						Failed to find a static test bean factory method in %s with return type %s \
						whose name matches one of the supported candidates %s""",
						clazz.getName(), returnType.getName(), List.of("explicit1"));
	}

	@Test
	void createMetaDataForKnownExplicitMethod() throws Exception {
		Class<?> returnType = ExampleService.class;
		Field field = ExplicitMethodNameConf.class.getField("b");
		TestBean overrideAnnotation = field.getAnnotation(TestBean.class);
		assertThat(overrideAnnotation).isNotNull();

		TestBeanOverrideProcessor processor = new TestBeanOverrideProcessor();
		assertThat(processor.createMetadata(field, overrideAnnotation, ResolvableType.forClass(returnType)))
				.isInstanceOf(MethodConventionOverrideMetadata.class);
	}

	@Test
	void createMetaDataWithDeferredCheckForExistenceOfConventionBasedFactoryMethod() throws Exception {
		Class<?> returnType = ExampleService.class;
		Field field = MethodConventionConf.class.getField("field");
		TestBean overrideAnnotation = field.getAnnotation(TestBean.class);
		assertThat(overrideAnnotation).isNotNull();

		TestBeanOverrideProcessor processor = new TestBeanOverrideProcessor();
		// When in convention-based mode, createMetadata() will not verify that
		// the factory method actually exists. So, we don't expect an exception
		// for this use case.
		assertThat(processor.createMetadata(field, overrideAnnotation, ResolvableType.forClass(returnType)))
				.isInstanceOf(MethodConventionOverrideMetadata.class);
	}


	static class MethodConventionConf {

		@TestBean
		public ExampleService field;

		@Bean
		ExampleService example1() {
			return new FailingExampleService();
		}

		static ExampleService example2() {
			return new FailingExampleService();
		}

		public static ExampleService example4() {
			return new FailingExampleService();
		}
	}

	static class ExplicitMethodNameConf {

		@TestBean(methodName = "explicit1")
		public ExampleService a;

		@TestBean(methodName = "explicit2")
		public ExampleService b;

		static ExampleService explicit2() {
			return new FailingExampleService();
		}
	}

}
