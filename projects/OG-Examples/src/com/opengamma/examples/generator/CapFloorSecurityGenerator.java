/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;


/**
 * Source of random, but reasonable, Cap/Floor security instances.
 */
public class CapFloorSecurityGenerator extends com.opengamma.financial.generator.CapFloorSecurityGenerator {

  @Override
  protected String getCurveConfigName() {
    return "SECONDARY";
  }

}
