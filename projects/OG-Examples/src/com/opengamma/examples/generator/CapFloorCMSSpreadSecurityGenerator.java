/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.generator.AbstractCapFloorCMSSpreadSecurityGenerator;

/**
 * Source of random, but reasonable, Cap/Floor CMS spread security instances.
 */
public class CapFloorCMSSpreadSecurityGenerator extends AbstractCapFloorCMSSpreadSecurityGenerator {

  @Override
  protected String getCurveConfigName() {
    return "SECONDARY";
  }

}
