/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.function.config.FunctionConfigurationDefinition;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link FunctionConfigurationDefinitionFudgeBuilder}
 */
@Test(groups = TestGroup.UNIT)
public class FunctionConfigurationDefinitionBuilderTest extends AbstractFudgeBuilderTestCase {
    
  @Test
  public void functionConfigurationDefinition() {
    List<String> otherConfigs = ImmutableList.of("CF1", "CF2", "CF3");
    List<StaticFunctionConfiguration> staticFunctions = ImmutableList.of(new StaticFunctionConfiguration("SF1"), 
        new StaticFunctionConfiguration("SF2"));
    List<ParameterizedFunctionConfiguration> parameterizedFunctions = ImmutableList.of(
        new ParameterizedFunctionConfiguration("PF1", ImmutableList.of("P11", "P12")),
        new ParameterizedFunctionConfiguration("PF2", ImmutableList.of("P21", "P22")));
    assertEncodeDecodeCycle(FunctionConfigurationDefinition.class, new FunctionConfigurationDefinition("FUNC_TEST", otherConfigs, staticFunctions, parameterizedFunctions));
  }

}
