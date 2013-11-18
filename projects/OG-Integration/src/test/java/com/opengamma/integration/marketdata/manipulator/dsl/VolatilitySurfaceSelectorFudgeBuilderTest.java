/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceSelectorFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  public void roundTrip() {
    VolatilitySurfaceSelector selector = new VolatilitySurfaceSelector(
        Sets.newHashSet("Default", "ccn1"),
        Sets.newHashSet("surface1", "surface2"),
        Pattern.compile("\\d*"),
        Pattern.compile("\\w*"),
        Sets.newHashSet("type1", "type2", "type3"),
        Sets.newHashSet("quoteType1", "quoteType2"),
        Sets.newHashSet("quoteUnits1", "quoteUnits2"));
    assertEncodeDecodeCycle(VolatilitySurfaceSelector.class, selector);
  }

}
