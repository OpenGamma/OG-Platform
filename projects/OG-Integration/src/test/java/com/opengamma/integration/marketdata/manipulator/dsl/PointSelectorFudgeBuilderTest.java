/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PointSelectorFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  public void roundTrip() {
    HashSet<String> calcConfigNames = Sets.newHashSet("ccn1", "ccn2");
    Set<String> securityTypes = Sets.newHashSet("EQUITY", "FX_FORWARD");
    PointSelector selector = new PointSelector(
        calcConfigNames,
        ImmutableSet.of(ExternalId.of("s", "v1"), ExternalId.of("s", "v2")),
        ExternalScheme.of("anotherScheme"),
        Pattern.compile("\\d*"),
        ExternalScheme.of("anotherScheme2"),
        Pattern.compile("\\w*"),
        securityTypes);
    assertEncodeDecodeCycle(PointSelector.class, selector);
  }

  public void roundTripWithNulls() {
    PointSelector selector = new PointSelector(null, null, null, null, null, null, null);
    assertEncodeDecodeCycle(PointSelector.class, selector);
  }

}
