/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link AbstractFunctionConfigurationBean} class.
 */
@Test(groups = TestGroup.UNIT)
public class AbstractFunctionConfigurationBeanTest {

  @Test(expectedExceptions = IllegalStateException.class)
  public void testFunctionConfigurationInvalidArguments() {
    AbstractFunctionConfigurationBean.functionConfiguration(AbstractFunction.class);
  }

  public void testCreate() {
    final AbstractFunctionConfigurationBean bean = new AbstractFunctionConfigurationBean() {
      @Override
      protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
        functions.add(functionConfiguration(MockFunction.class));
        functions.add(functionConfiguration(MockFunction.class, "Foo", "Bar"));
      }
    };
    final FunctionConfigurationSource source = bean.getObjectCreating();
    final FunctionConfigurationBundle bundle = source.getFunctionConfiguration(Instant.now());
    final List<FunctionConfiguration> functions = bundle.getFunctions();
    assertEquals(functions.size(), 2);
    assertEquals(functions.get(0).getClass(), StaticFunctionConfiguration.class);
    assertEquals(((StaticFunctionConfiguration) functions.get(0)).getDefinitionClassName(), MockFunction.class.getName());
    assertEquals(functions.get(1).getClass(), ParameterizedFunctionConfiguration.class);
    assertEquals(((ParameterizedFunctionConfiguration) functions.get(1)).getDefinitionClassName(), MockFunction.class.getName());
    assertEquals(((ParameterizedFunctionConfiguration) functions.get(1)).getParameter(), Arrays.asList("Foo", "Bar"));
  }

}
