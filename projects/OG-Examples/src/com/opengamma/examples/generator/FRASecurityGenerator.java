/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.generator.AbstractFRASecurityGenerator;

/**
 * Source of random, but reasonable, FRA security instances.
 */
public class FRASecurityGenerator extends AbstractFRASecurityGenerator {

  @Override
  protected String getCurveConfigName() {
    return "SECONDARY";
  }

}
