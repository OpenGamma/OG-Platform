/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionSABRMethod;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.method.CapFloorCMSSABRReplicationMethod;
import com.opengamma.financial.interestrate.payments.method.CapFloorCMSSpreadSABRBinormalMethod;
import com.opengamma.financial.interestrate.payments.method.CapFloorIborSABRMethod;
import com.opengamma.financial.interestrate.payments.method.CouponCMSSABRReplicationMethod;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.interestrate.swaption.method.SwaptionCashFixedIborSABRMethod;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.financial.model.option.definition.SABRInterestRateCorrelationParameters;
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
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final CapFloorIborSABRMethod method = CapFloorIborSABRMethod.getInstance();
      return method.presentValue(cap, sabr).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitCapFloorIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final SwaptionCashFixedIborSABRMethod method = SwaptionCashFixedIborSABRMethod.getInstance();
      return method.presentValue(swaption, sabr).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitSwaptionCashFixedIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final SwaptionPhysicalFixedIborSABRMethod method = SwaptionPhysicalFixedIborSABRMethod.getInstance();
      return method.presentValue(swaption, sabr).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateDataBundle as data.");

  }

  @Override
  public Double visitCouponCMS(final CouponCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      final CouponCMSSABRReplicationMethod replication = CouponCMSSABRReplicationMethod.getInstance();
      return replication.presentValue(payment, sabrBundle);
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitCouponCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Double visitCapFloorCMS(final CapFloorCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      final CapFloorCMSSABRReplicationMethod replication = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
      return replication.presentValue(payment, sabrBundle).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitCapFloorCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Double visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      if (sabrBundle.getSABRParameter() instanceof SABRInterestRateCorrelationParameters) {
        final SABRInterestRateCorrelationParameters sabrCorrelation = (SABRInterestRateCorrelationParameters) sabrBundle.getSABRParameter();
        final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(sabrCorrelation.getCorrelation());
        return method.presentValue(payment, sabrBundle).getAmount();
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitCapFloorCMSSpread requires a SABRInterestRateDataBundle with correlation as data.");
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(option);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      final InterestRateFutureOptionMarginTransactionSABRMethod method = InterestRateFutureOptionMarginTransactionSABRMethod.getInstance();
      return method.presentValue(option, sabrBundle).getAmount();
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitInterestRateFutureOptionMarginTransaction requires a SABRInterestRateDataBundle as data.");
  }

}
