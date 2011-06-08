/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.future.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionSABRMethod;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.method.CapFloorCMSSABRReplicationMethod;
import com.opengamma.financial.interestrate.payments.method.CapFloorIborSABRMethod;
import com.opengamma.financial.interestrate.payments.method.CouponCMSSABRReplicationMethod;
import com.opengamma.financial.interestrate.swaption.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.interestrate.swaption.method.SwaptionCashFixedIborSABRMethod;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;

/**
 * Present value calculator for interest rate instruments using SABR volatility formula.
 */
public final class PresentValueSABRCalculator extends PresentValueCalculator {

  private static final PresentValueSABRCalculator s_instance = new PresentValueSABRCalculator();

  public static PresentValueSABRCalculator getInstance() {
    return s_instance;
  }

  private PresentValueSABRCalculator() {
  }

  @Override
  public Double visitCapFloorIbor(final CapFloorIbor cap, final YieldCurveBundle curves) {
    Validate.notNull(cap);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      CapFloorIborSABRMethod method = new CapFloorIborSABRMethod();
      return method.presentValue(cap, sabr).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitCapFloorIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      SwaptionCashFixedIborSABRMethod method = new SwaptionCashFixedIborSABRMethod();
      return method.presentValue(swaption, sabr);
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitSwaptionCashFixedIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      SwaptionPhysicalFixedIborSABRMethod method = new SwaptionPhysicalFixedIborSABRMethod();
      return method.presentValue(swaption, sabr);
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateDataBundle as data.");

  }

  @Override
  public Double visitCouponCMS(CouponCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      CouponCMSSABRReplicationMethod replication = new CouponCMSSABRReplicationMethod();
      return replication.presentValue(payment, sabrBundle);
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitCouponCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Double visitCapFloorCMS(CapFloorCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      CapFloorCMSSABRReplicationMethod replication = new CapFloorCMSSABRReplicationMethod();
      return replication.presentValue(payment, sabrBundle);
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitCapFloorCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(option);
    if (curves instanceof SABRInterestRateDataBundle) {
      SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      InterestRateFutureOptionMarginTransactionSABRMethod method = new InterestRateFutureOptionMarginTransactionSABRMethod();
      return method.presentValue(option, sabrBundle).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitInterestRateFutureOptionMarginTransaction requires a SABRInterestRateDataBundle as data.");
  }

}
