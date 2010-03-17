/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import com.opengamma.financial.greeks.AbstractGreekVisitor;
import com.opengamma.financial.sensitivity.Sensitivity;

/**
 * @author emcleod
 *
 */
public class GreekToValueGreekVisitor extends AbstractGreekVisitor<Sensitivity> {

  @Override
  public Sensitivity visitDelta() {
    return Sensitivity.VALUE_DELTA;
  }

  @Override
  public Sensitivity visitGamma() {
    return Sensitivity.VALUE_GAMMA;
  }

  @Override
  public Sensitivity visitTheta() {
    return Sensitivity.VALUE_THETA;
  }

  @Override
  public Sensitivity visitVanna() {
    return Sensitivity.VALUE_VANNA;
  }

  @Override
  public Sensitivity visitVega() {
    return Sensitivity.VALUE_VEGA;
  }

}
