/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionSABRMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorCMSSABRReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorCMSSpreadSABRBinormalMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorIborSABRMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponCMSSABRReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionCashFixedIborSABRMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Present value sensitivity to SABR parameters calculator for interest rate instruments using SABR volatility formula.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class PresentValueSABRSensitivitySABRCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, PresentValueSABRSensitivityDataBundle> {

  /**
   * The unique instance of the SABR sensitivity calculator.
   */
  private static final PresentValueSABRSensitivitySABRCalculator s_instance = new PresentValueSABRSensitivitySABRCalculator();

  /**
   * Returns the instance of the calculator.
   * @return The instance.
   */
  public static PresentValueSABRSensitivitySABRCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PresentValueSABRSensitivitySABRCalculator() {
  }

  /**
   * Methods.
   */
  public static final CapFloorIborSABRMethod METHOD_IBOR_CAP = CapFloorIborSABRMethod.getInstance();

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorIbor(final CapFloorIbor cap, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(cap, "cap");
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      return METHOD_IBOR_CAP.presentValueSABRSensitivity(cap, sabr);
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitCapFloorIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(swaption, "swaption");
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final SwaptionCashFixedIborSABRMethod method = SwaptionCashFixedIborSABRMethod.getInstance();
      return method.presentValueSABRSensitivity(swaption, sabr);
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitSwaptionCashFixedIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(curves, "curves");
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final SwaptionPhysicalFixedIborSABRMethod method = SwaptionPhysicalFixedIborSABRMethod.getInstance();
      return method.presentValueSABRSensitivity(swaption, sabr);
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateDataBundle as data.");

  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponCMS(final CouponCMS payment, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(payment, "payment");
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      final CouponCMSSABRReplicationMethod replication = CouponCMSSABRReplicationMethod.getInstance();
      return replication.presentValueSABRSensitivity(payment, sabrBundle);
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitCouponCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMS(final CapFloorCMS payment, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(payment, "payment");
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      final CapFloorCMSSABRReplicationMethod replication = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
      return replication.presentValueSABRSensitivity(payment, sabrBundle);
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitCapFloorCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(payment, "payment");
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      if (sabrBundle.getSABRParameter() instanceof SABRInterestRateCorrelationParameters) {
        final SABRInterestRateCorrelationParameters sabrCorrelation = (SABRInterestRateCorrelationParameters) sabrBundle.getSABRParameter();
        final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(sabrCorrelation.getCorrelation(), CapFloorCMSSABRReplicationMethod.getDefaultInstance(),
            CouponCMSSABRReplicationMethod.getInstance());
        return method.presentValueSABRSensitivity(payment, sabrBundle);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitCapFloorCMSSpread requires a SABRInterestRateDataBundle with correlation as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(annuity, "annuity");
    PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    for (final Payment p : annuity.getPayments()) {
      pvss = pvss.plus(p.accept(this, curves));
    }
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponFixed(final CouponFixed coupon, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(coupon, "coupon");
    final PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(swap, "swap");
    PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    for (final Payment p : swap.getFirstLeg().getPayments()) {
      pvss = pvss.plus(p.accept(this, curves));
    }
    for (final Payment p : swap.getSecondLeg().getPayments()) {
      pvss = pvss.plus(p.accept(this, curves));
    }
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(coupon, "coupon");
    final PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponIborSpread(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(coupon, "coupon");
    final PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(option);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      final InterestRateFutureOptionMarginTransactionSABRMethod method = InterestRateFutureOptionMarginTransactionSABRMethod.getInstance();
      return method.presentValueSABRSensitivity(option, sabrBundle);
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitInterestRateFutureOptionMarginTransaction requires a SABRInterestRateDataBundle as data.");
  }

}
