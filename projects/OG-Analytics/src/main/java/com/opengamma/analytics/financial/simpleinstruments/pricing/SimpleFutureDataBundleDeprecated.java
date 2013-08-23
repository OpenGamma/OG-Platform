/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.simpleinstruments.pricing;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * @deprecated This has been replaced by SimpleFutureDataBundle
 */
@Deprecated
public class SimpleFutureDataBundleDeprecated {
  private final YieldAndDiscountCurve _fundingCurve;
  private final Double _spotValue;
  private final Double _costOfCarry;
  private final Double _dividendYield;
  private final Double _marketPrice;

  public SimpleFutureDataBundleDeprecated(final YieldAndDiscountCurve fundingCurve, final double spot, final double costOfCarry) {
    Validate.notNull(fundingCurve, "yield curve");
    _fundingCurve = fundingCurve;
    _spotValue = spot;
    _costOfCarry = costOfCarry;
    _dividendYield = null;
    _marketPrice = null;
  }

  public SimpleFutureDataBundleDeprecated(final YieldAndDiscountCurve fundingCurve, final Double marketPrice, final Double spotValue, final Double dividendYield, final Double costOfCarry) {
    _fundingCurve = fundingCurve;
    _marketPrice = marketPrice;
    _spotValue = spotValue;
    _dividendYield = dividendYield;
    _costOfCarry = costOfCarry;
  }

  public YieldAndDiscountCurve getFundingCurve() {
    return _fundingCurve;
  }

  public Double getSpotValue() {
    return _spotValue;
  }

  public Double getCostOfCarry() {
    return _costOfCarry;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_costOfCarry == null) ? 0 : _costOfCarry.hashCode());
    result = prime * result + ((_dividendYield == null) ? 0 : _dividendYield.hashCode());
    result = prime * result + _fundingCurve.hashCode();
    result = prime * result + ((_marketPrice == null) ? 0 : _marketPrice.hashCode());
    result = prime * result + ((_spotValue == null) ? 0 : _spotValue.hashCode());
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
    if (!(obj instanceof SimpleFutureDataBundleDeprecated)) {
      return false;
    }
    final SimpleFutureDataBundleDeprecated other = (SimpleFutureDataBundleDeprecated) obj;
    if (Double.compare(_marketPrice, other._marketPrice) != 0) {
      return false;
    }
    if (Double.compare(_spotValue, other._spotValue) != 0) {
      return false;
    }
    if (Double.compare(_dividendYield, other._dividendYield) != 0) {
      return false;
    }
    if (Double.compare(_costOfCarry, other._costOfCarry) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_fundingCurve, other._fundingCurve)) {
      return false;
    }
    return true;
  }

}
