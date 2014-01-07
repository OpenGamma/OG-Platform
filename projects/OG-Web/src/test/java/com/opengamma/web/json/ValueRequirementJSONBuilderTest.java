/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ValueRequirementJSONBuilderTest {

  @Test
  public void roundTrip() {
    ValueProperties props = ValueProperties.builder().with("foo", "FOO").with("bar", "[BAR1]", "BAR2").get();
    ValueRequirement req1 = new ValueRequirement("valueName",
                                                ComputationTargetType.PORTFOLIO,
                                                UniqueId.of("foo", "bar"),
                                                props);
    ValueRequirementJSONBuilder builder = new ValueRequirementJSONBuilder();
    String jsonStr = builder.toJSON(req1);
    ValueRequirement req2 = builder.fromJSON(jsonStr);
    assertEquals(req1, req2);
  }
}
