/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class TotalReturnSwap implements InstrumentDerivative {
  /** The funding leg */
  private final Annuity<? extends Payment> _fundingLeg;
  /** The asset */
  private final InstrumentDerivative _asset;

  /**
   * @param fundingLeg The funding leg, not null
   * @param asset The asset, not null
   */
  public TotalReturnSwap(final Annuity<? extends Payment> fundingLeg, final InstrumentDerivative asset) {
    ArgumentChecker.notNull(fundingLeg, "fundingLeg");
    ArgumentChecker.notNull(asset, "asset");
    _fundingLeg = fundingLeg;
    _asset = asset;
  }

  /**
   * Gets the funding leg.
   * @return The funding leg
   */
  public Annuity<? extends Payment> getFundingLeg() {
    return _fundingLeg;
  }

  /**
   * Gets the asset.
   * @return The asset
   */
  public InstrumentDerivative getAsset() {
    return _asset;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _asset.hashCode();
    result = prime * result + _fundingLeg.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TotalReturnSwap)) {
      return false;
    }
    final TotalReturnSwap other = (TotalReturnSwap) obj;
    if (!ObjectUtils.equals(_asset, other._asset)) {
      return false;
    }
    if (!ObjectUtils.equals(_fundingLeg, other._fundingLeg)) {
      return false;
    }
    return true;
  }
}
