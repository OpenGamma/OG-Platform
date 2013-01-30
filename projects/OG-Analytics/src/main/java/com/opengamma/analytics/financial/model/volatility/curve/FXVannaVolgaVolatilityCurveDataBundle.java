/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.curve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class FXVannaVolgaVolatilityCurveDataBundle {
  private final double _delta;
  private final double _riskReversal;
  private final double _atm;
  private final double _vegaWeightedButterfly;
  private final ZonedDateTime _maturity;

  public FXVannaVolgaVolatilityCurveDataBundle(final double delta, final double riskReversal, final double atm, final double vegaWeightedButterfly, final ZonedDateTime maturity) {
    ArgumentChecker.notNegative(atm, "atm");
    Validate.notNull(maturity, "maturity");
    _delta = delta;
    _riskReversal = riskReversal;
    _atm = atm;
    _vegaWeightedButterfly = vegaWeightedButterfly;
    _maturity = maturity;
  }

  public double getDelta() {
    return _delta;
  }

  public double getRiskReversal() {
    return _riskReversal;
  }

  public double getAtTheMoney() {
    return _atm;
  }

  public double getVegaWeightedButterfly() {
    return _vegaWeightedButterfly;
  }

  public ZonedDateTime getMaturity() {
    return _maturity;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_atm);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_delta);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _maturity.hashCode();
    temp = Double.doubleToLongBits(_riskReversal);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_vegaWeightedButterfly);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FXVannaVolgaVolatilityCurveDataBundle other = (FXVannaVolgaVolatilityCurveDataBundle) obj;
    if (Double.doubleToLongBits(_atm) != Double.doubleToLongBits(other._atm)) {
      return false;
    }
    if (Double.doubleToLongBits(_delta) != Double.doubleToLongBits(other._delta)) {
      return false;
    }
    if (!ObjectUtils.equals(_maturity, other._maturity)) {
      return false;
    }
    if (Double.doubleToLongBits(_riskReversal) != Double.doubleToLongBits(other._riskReversal)) {
      return false;
    }
    if (Double.doubleToLongBits(_vegaWeightedButterfly) != Double.doubleToLongBits(other._vegaWeightedButterfly)) {
      return false;
    }
    return true;
  }

}
