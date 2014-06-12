/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of an STIR (Short Term Interest Rate) future transaction.
 */
public class InterestRateFutureTransaction extends FuturesTransaction<InterestRateFutureSecurity> {

  /**
   * Constructor from tthe underlying and transaction details.
   * @param underlying The underlying futures security.
   * @param referencePrice The reference price (trading price or last margining price).
   * @param quantity The number of contracts.
   */
  public InterestRateFutureTransaction(final InterestRateFutureSecurity underlying, final double referencePrice, final long quantity) {
    super(underlying, quantity, referencePrice);
  }

  /**
   * Constructor from all the details.
   * @param lastTradingTime Future last trading time.
   * @param iborIndex Ibor index associated to the future.
   * @param fixingPeriodStartTime Fixing period of the reference Ibor starting time.
   * @param fixingPeriodEndTime Fixing period of the reference Ibor end time.
   * @param fixingPeriodAccrualFactor Fixing period of the reference Ibor accrual factor.
   * @param referencePrice The reference price.
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor.
   * @param quantity The quantity.
   * @param name Future name.
   * @param discountingCurveName The discounting curve name.
   * @param forwardCurveName The forward curve name.
   * @deprecated Deprecated since 2.2.M12. Use the constructor that does not that curve names
   */
  @Deprecated
  public InterestRateFutureTransaction(final double lastTradingTime, final IborIndex iborIndex, final double fixingPeriodStartTime, final double fixingPeriodEndTime,
      final double fixingPeriodAccrualFactor, final double referencePrice, final double notional, final double paymentAccrualFactor, final int quantity, final String name,
      final String discountingCurveName, final String forwardCurveName) {
    super(new InterestRateFutureSecurity(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, notional, paymentAccrualFactor, name,
        discountingCurveName, forwardCurveName), quantity, referencePrice);
  }

  /**
   * Constructor from all the details.
   * @param lastTradingTime Future last trading time.
   * @param iborIndex Ibor index associated to the future.
   * @param fixingPeriodStartTime Fixing period of the reference Ibor starting time.
   * @param fixingPeriodEndTime Fixing period of the reference Ibor end time.
   * @param fixingPeriodAccrualFactor Fixing period of the reference Ibor accrual factor.
   * @param referencePrice The reference price.
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor.
   * @param quantity The quantity.
   * @param name Future name.
   * @deprecated Deprecated since 2.2.M12. Use the constructor from the security.
   */
  @Deprecated
  public InterestRateFutureTransaction(final double lastTradingTime, final IborIndex iborIndex, final double fixingPeriodStartTime, final double fixingPeriodEndTime,
      final double fixingPeriodAccrualFactor, final double referencePrice, final double notional, final double paymentAccrualFactor, final int quantity, final String name) {
    super(new InterestRateFutureSecurity(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, notional, paymentAccrualFactor, name),
        quantity, referencePrice);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransaction(this);
  }

}
