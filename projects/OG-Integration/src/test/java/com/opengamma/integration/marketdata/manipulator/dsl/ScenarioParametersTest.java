/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opengamma.engine.marketdata.manipulator.ScenarioParameters;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ScenarioParametersTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void setParametersFromScript() throws IOException {
    String scriptFile = "src/test/groovy/ScenarioParametersTest.groovy";
    String script = IOUtils.toString(new BufferedReader(new FileReader(scriptFile)));
    ScenarioParameters scenarioParameters = ScenarioDslParameters.of(script);
    Map<String, Object> parameters = scenarioParameters.getParameters();
    assertEquals("foo", parameters.get("aString"));
    assertEquals(Lists.newArrayList(1, 2, 3), parameters.get("aList"));
    assertEquals(1.234, ((Number) parameters.get("aDouble")).doubleValue());
    assertEquals(ImmutableMap.of("key1", "value1", "key2", "value2"), parameters.get("aMap"));
    assertEquals(LocalDate.of(2011, 3, 8), parameters.get("aLocalDate"));
  }

  @Test
  public void fudgeRoundTrip() {
    ScenarioDslParameters parameters = ScenarioDslParameters.of("str = \"foo\"\ndbl = 1.23");
    assertEncodeDecodeCycle(ScenarioDslParameters.class, parameters);
  }

}
