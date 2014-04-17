/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TotalReturnSwap;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class TotalReturnSwapDefinition implements InstrumentDefinitionWithData<TotalReturnSwap, ZonedDateTimeDoubleTimeSeries> {
  /** The funding leg */
  private final AnnuityDefinition<? extends PaymentDefinition> _fundingLeg;
  /** The asset */
  private final InstrumentDefinition<?> _asset;

  /**
   * @param fundingLeg The funding leg, not null
   * @param asset The asset, not null
   */
  public TotalReturnSwapDefinition(final AnnuityDefinition<? extends PaymentDefinition> fundingLeg, final InstrumentDefinition<?> asset) {
    ArgumentChecker.notNull(fundingLeg, "fundingLeg");
    ArgumentChecker.notNull(asset, "asset");
    _fundingLeg = fundingLeg;
    _asset = asset;
  }

  /**
   * Gets the funding leg.
   * @return The funding leg
   */
  public AnnuityDefinition<? extends PaymentDefinition> getFundingLeg() {
    return _fundingLeg;
  }

  /**
   * Gets the asset.
   * @return The asset
   */
  public InstrumentDefinition<?> getAsset() {
    return _asset;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitTotalReturnSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitTotalReturnSwapDefinition(this);
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
    if (!(obj instanceof TotalReturnSwapDefinition)) {
      return false;
    }
    final TotalReturnSwapDefinition other = (TotalReturnSwapDefinition) obj;
    if (!ObjectUtils.equals(_asset, other._asset)) {
      return false;
    }
    if (!ObjectUtils.equals(_fundingLeg, other._fundingLeg)) {
      return false;
    }
    return true;
  }

}
