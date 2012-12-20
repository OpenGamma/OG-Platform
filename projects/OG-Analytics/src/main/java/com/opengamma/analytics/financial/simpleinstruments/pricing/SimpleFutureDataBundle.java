/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.simpleinstruments.pricing;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Market data requirements for pricing the SimpleFuture and EquityFuture.<p>
 * NOTE: Each EquityFuturesPricingMethod requires different data. 
 * Some members of the data bundle may be null!
 */
public class SimpleFutureDataBundle {

  private final YieldAndDiscountCurve _fundingCurve;

  /** MARK_TO_MARKET */
  private final Double _marketPrice;

  /** DIVIDEND_YIELD */
  private final Double _spotValue;
  private final Double _dividendYield;

  /** COST_OF_CARRY */
  private final Double _costOfCarry;

  /**
   * @param fundingCurve Used for discounting 
   * @param marketPrice Quoted futures price
   * @param spotValue Quoted market spot value of the underlying
   * @param dividendYield An estimate of the continuous dividend yield over the life of the future
   * @param costOfCarry An estimate of the cost of carry, as a rate => FwdPrice = Spot * exp(costOfCarry * T)
   */
  public SimpleFutureDataBundle(YieldAndDiscountCurve fundingCurve, Double marketPrice, Double spotValue, Double dividendYield, Double costOfCarry) {
    _fundingCurve = fundingCurve;
    _marketPrice = marketPrice;
    _spotValue = spotValue;
    _dividendYield = dividendYield;
    _costOfCarry = costOfCarry;
  }

  /**
   * Gets the fundingCurve.
   * @return the fundingCurve
   */
  public final YieldAndDiscountCurve getFundingCurve() {
    return _fundingCurve;
  }

  /**
   * Gets the marketPrice.
   * @return the marketPrice
   */
  public final Double getMarketPrice() {
    return _marketPrice;
  }

  /**
   * Gets the spotValue.
   * @return the spotValue
   */
  public final Double getSpotValue() {
    return _spotValue;
  }

  /**
   * Gets the dividendYield.
   * @return the dividendYield
   */
  public final Double getDividendYield() {
    return _dividendYield;
  }

  /**
   * Gets the costOfCarry.
   * @return the costOfCarry
   */
  public final Double getCostOfCarry() {
    return _costOfCarry;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_costOfCarry == null) ? 0 : _costOfCarry.hashCode());
    result = prime * result + ((_dividendYield == null) ? 0 : _dividendYield.hashCode());
    result = prime * result + ((_fundingCurve == null) ? 0 : _fundingCurve.hashCode());
    result = prime * result + ((_marketPrice == null) ? 0 : _marketPrice.hashCode());
    result = prime * result + ((_spotValue == null) ? 0 : _spotValue.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof SimpleFutureDataBundle)) {
      return false;
    }
    SimpleFutureDataBundle other = (SimpleFutureDataBundle) obj;
    if (_costOfCarry == null) {
      if (other._costOfCarry != null) {
        return false;
      }
    } else if (!_costOfCarry.equals(other._costOfCarry)) {
      return false;
    }
    if (_dividendYield == null) {
      if (other._dividendYield != null) {
        return false;
      }
    } else if (!_dividendYield.equals(other._dividendYield)) {
      return false;
    }
    if (_fundingCurve == null) {
      if (other._fundingCurve != null) {
        return false;
      }
    } else if (!_fundingCurve.equals(other._fundingCurve)) {
      return false;
    }
    if (_marketPrice == null) {
      if (other._marketPrice != null) {
        return false;
      }
    } else if (!_marketPrice.equals(other._marketPrice)) {
      return false;
    }
    if (_spotValue == null) {
      if (other._spotValue != null) {
        return false;
      }
    } else if (!_spotValue.equals(other._spotValue)) {
      return false;
    }
    return true;
  }

}
