/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * An Equity Variance Swap is a forward contract on the realised variance of equity index or single stock. It differs from the more general Variance Swap only in that dividends
 * are paid on equity and the treatment of these in the computation of the realised variance depends on the contract. Dividends are either corrected for (i.e. the realised variance is
 * that of the total returns process - the position where <b>all</b> dividend payments are immediately reinvested in the stock) or not - either way, the expected variance (the variance
 * strike of a zero value swap) will differ from the no dividend case.
 */
public class EquityVarianceSwap extends VarianceSwap {
  /** Are the dividends to be corrected for in pricing */
  private final boolean _dividendsCorrected;

  /**
   * Set up an equity variance swap
   * @param varianceSwap The variance swap
   * @param correctForDividends true if dividend payments are corrected for in the realised variance calculation
   */
  public EquityVarianceSwap(final VarianceSwap varianceSwap, final boolean correctForDividends) {
    super(varianceSwap);
    _dividendsCorrected = correctForDividends;
  }

  /**
   * Set up an equity variance swap
   * @param timeToObsStart Time of first observation. Negative if observations have begun.
   * @param timeToObsEnd Time of final observation. Negative if observations have finished.
   * @param timeToSettlement Time of cash settlement. If negative, the swap has expired.
   * @param varStrike Fair value of Variance struck at trade date
   * @param varNotional Trade pays the difference between realized and strike variance multiplied by this
   * @param currency Currency of cash settlement
   * @param annualizationFactor Number of business days per year
   * @param nObsExpected Number of observations expected as of trade inception
   * @param nObsDisrupted Number of expected observations that did not occur because of a market disruption
   * @param observations Array of observations of the underlying spot
   * @param observationWeights Array of weights to give observation returns. If null, all weights are 1. Else, length must be: observations.length-1
   * @param correctForDividends true if dividend payments are corrected for in the realised variance calculation
   */
  public EquityVarianceSwap(final double timeToObsStart, final double timeToObsEnd, final double timeToSettlement, final double varStrike, final double varNotional,
      final Currency currency, final double annualizationFactor, final int nObsExpected,
      final int nObsDisrupted, final double[] observations, final double[] observationWeights, final boolean correctForDividends) {
    super(timeToObsStart, timeToObsEnd, timeToSettlement, varStrike, varNotional, currency, annualizationFactor, nObsExpected, nObsDisrupted, observations, observationWeights);
    _dividendsCorrected = correctForDividends;
  }

  /**
   * Returns true if the dividends are to be corrected when pricing.
   * @return true if the dividends are to be corrected when pricing
   */
  public boolean correctForDividends() {
    return _dividendsCorrected;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityVarianceSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityVarianceSwap(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_dividendsCorrected ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof EquityVarianceSwap)) {
      return false;
    }
    final EquityVarianceSwap other = (EquityVarianceSwap) obj;
    if (_dividendsCorrected != other._dividendsCorrected) {
      return false;
    }
    return true;
  }

}
