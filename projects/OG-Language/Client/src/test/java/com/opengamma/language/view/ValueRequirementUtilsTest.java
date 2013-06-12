/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.tuple.Triple;

@Test
public class ValueRequirementUtilsTest {

  public void testParseRequirements() {
    assertEquals(Triple.of("Default", "FairValue", ValueProperties.none()), ValueRequirementUtils.parseRequirement("FairValue"));
    assertEquals(Triple.of("Default", "Present Value", ValueProperties.none()), ValueRequirementUtils.parseRequirement("Present Value"));
    assertEquals(Triple.of("Default", "FairValue", ValueProperties.none()), ValueRequirementUtils.parseRequirement("Default/FairValue"));
    assertEquals(Triple.of("Default", "Present Value", ValueProperties.none()), ValueRequirementUtils.parseRequirement("Default/Present Value"));
    assertEquals(Triple.of("Foo", "FairValue", ValueProperties.none()), ValueRequirementUtils.parseRequirement("Foo/FairValue"));
    assertEquals(Triple.of("SHIFT 10BPS", "Present Value", ValueProperties.none()), ValueRequirementUtils.parseRequirement("SHIFT 10BPS/Present Value"));
    assertEquals(Triple.of("Default", "FairValue", ValueProperties.with("x", "A").with("y", "B").get()), ValueRequirementUtils.parseRequirement("FairValue[x=A,y=B]"));
    assertEquals(Triple.of("Default", "Present Value", ValueProperties.with("x", "A").with("y", "B").get()), ValueRequirementUtils.parseRequirement("Present Value{x=A,y=B}"));
    assertEquals(Triple.of("Foo Bar", "Present Value", ValueProperties.with("x", "=[\\,{}]").get()), ValueRequirementUtils.parseRequirement("Foo Bar/Present Value[x=\\=\\[\\\\\\,{}\\]]"));
    assertEquals(Triple.of("Foo Bar", "Present Value", ValueProperties.with("x", "=[\\,{}] ").get()), ValueRequirementUtils.parseRequirement("Foo Bar/Present Value[x=\\=\\[\\\\\\,{}\\]\\ ]"));
  }

}
