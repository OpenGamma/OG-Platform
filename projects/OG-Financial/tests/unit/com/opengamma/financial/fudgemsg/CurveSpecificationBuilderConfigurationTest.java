/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.ircurve.CurveDefinitionAndSpecifications;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;

public class CurveSpecificationBuilderConfigurationTest extends FinancialTestBase {

  private static final Logger s_logger = LoggerFactory.getLogger(CurveSpecificationBuilderConfigurationTest.class);
  @Test
  public void testCycle() {
    CurveSpecificationBuilderConfiguration configuration = CurveDefinitionAndSpecifications.buildTestConfiguration();
    CurveSpecificationBuilderConfiguration cycleObject = cycleObject(CurveSpecificationBuilderConfiguration.class, configuration);
    s_logger.error(configuration.toString());
    s_logger.error(cycleObject.toString());
    assertEquals(configuration, cycleObject(CurveSpecificationBuilderConfiguration.class, configuration));
  }

}
