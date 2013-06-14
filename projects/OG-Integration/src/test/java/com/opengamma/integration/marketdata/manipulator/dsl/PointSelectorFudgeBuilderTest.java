/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

public class PointSelectorFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void roundTrip() {
    assertEncodeDecodeCycle(PointSelector.class, new PointSelector(ExternalId.of("scheme", "value"), "ccn"));
  }
}
