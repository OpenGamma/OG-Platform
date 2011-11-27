/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionSABRMethod;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.method.CapFloorCMSSABRReplicationMethod;
import com.opengamma.financial.interestrate.payments.method.CapFloorCMSSpreadSABRBinormalMethod;
import com.opengamma.financial.interestrate.payments.method.CapFloorIborSABRMethod;
import com.opengamma.financial.interestrate.payments.method.CouponCMSSABRReplicationMethod;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.interestrate.swaption.method.SwaptionCashFixedIborSABRMethod;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Present value sensitivity to SABR parameters calculator for interest rate instruments using SABR volatility formula.
 */
public final class PresentValueSABRSensitivitySABRCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, PresentValueSABRSensitivityDataBundle> {

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

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorIbor(final CapFloorIbor cap, final YieldCurveBundle curves) {
    Validate.notNull(cap);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final CapFloorIborSABRMethod method = CapFloorIborSABRMethod.getInstance();
      return method.presentValueSABRSensitivity(cap, sabr);
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitCapFloorIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final SwaptionCashFixedIborSABRMethod method = SwaptionCashFixedIborSABRMethod.getInstance();
      return method.presentValueSABRSensitivity(swaption, sabr);
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitSwaptionCashFixedIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final SwaptionPhysicalFixedIborSABRMethod method = SwaptionPhysicalFixedIborSABRMethod.getInstance();
      return method.presentValueSABRSensitivity(swaption, sabr);
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateDataBundle as data.");

  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponCMS(final CouponCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      final CouponCMSSABRReplicationMethod replication = CouponCMSSABRReplicationMethod.getDefaultInstance();
      return replication.presentValueSABRSensitivity(payment, sabrBundle);
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitCouponCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMS(final CapFloorCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      final CapFloorCMSSABRReplicationMethod replication = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
      return replication.presentValueSABRSensitivity(payment, sabrBundle);
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitCapFloorCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      if (sabrBundle.getSABRParameter() instanceof SABRInterestRateCorrelationParameters) {
        final SABRInterestRateCorrelationParameters sabrCorrelation = (SABRInterestRateCorrelationParameters) sabrBundle.getSABRParameter();
        final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(sabrCorrelation.getCorrelation());
        return method.presentValueSABRSensitivity(payment, sabrBundle);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitCapFloorCMSSpread requires a SABRInterestRateDataBundle with correlation as data.");
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
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRCalculator visitor visitInterestRateFutureOptionMarginTransaction requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    for (final Payment p : annuity.getPayments()) {
      pvss = pvss.plus(visit(p, curves));
    }
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitFixedCouponPayment(final CouponFixed coupon, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(coupon);
    final PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    final DoublesPair expiryMaturity = DoublesPair.of(0., coupon.getPaymentTime());
    pvss.addAlpha(expiryMaturity, 0);
    pvss.addNu(expiryMaturity, 0);
    pvss.addRho(expiryMaturity, 0);
    return pvss;
  }
  
  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponIborFixed(final CouponIborFixed coupon, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(coupon);
    final PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    final DoublesPair expiryMaturity = DoublesPair.of(0., coupon.getPaymentTime());
    pvss.addAlpha(expiryMaturity, 0);
    pvss.addNu(expiryMaturity, 0);
    pvss.addRho(expiryMaturity, 0);
    return pvss;
  }
  
  @Override
  public PresentValueSABRSensitivityDataBundle visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
    PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    for (final Payment p : swap.getFirstLeg().getPayments()) {
      pvss = pvss.plus(visit(p, curves));
    }
    for (final Payment p : swap.getSecondLeg().getPayments()) {
      pvss = pvss.plus(visit(p, curves));
    }
    return pvss;
  }
  
  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    final CapFloorIbor capFloor = CapFloorIbor.from(coupon, 0, true);
    return visit(capFloor, curves);
  }
}
