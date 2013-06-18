/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.HashSet;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

public class PointSelectorFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void roundTrip() {
    HashSet<String> calcConfigNames = Sets.newHashSet("ccn1", "ccn2");
    PointSelector selector = new PointSelector(calcConfigNames,
                                               ImmutableSet.of(ExternalId.of("s", "v1"), ExternalId.of("s", "v2")),
                                               ExternalScheme.of("anotherScheme"),
                                               Pattern.compile("\\d*"));
    assertEncodeDecodeCycle(PointSelector.class, selector);
  }
}
