/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

public class PointSelectorFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void roundTrip() {
    PointSelector selector = new PointSelector("ccn",
                                               ExternalId.of("scheme", "value"),
                                               ExternalScheme.of("anotherScheme"),
                                               Pattern.compile("\\d*"));
    assertEncodeDecodeCycle(PointSelector.class, selector);
  }
}
