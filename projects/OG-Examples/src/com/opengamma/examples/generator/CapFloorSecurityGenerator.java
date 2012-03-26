/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.generator.AbstractCapFloorSecurityGenerator;

/**
 * Source of random, but reasonable, Cap/Floor security instances.
 */
public class CapFloorSecurityGenerator extends AbstractCapFloorSecurityGenerator {

  @Override
  protected String getCurveConfigName() {
    return "SECONDARY";
  }

}
