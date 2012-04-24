/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.analytics.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorCMSSpreadSABRBinormalMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swap.definition.Swap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionCashFixedIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateExtrapolationParameters;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Present value sensitivity to SABR parameters calculator for interest rate instruments using SABR volatility formula.
 */
public final class PresentValueSABRSensitivitySABRRightExtrapolationCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, PresentValueSABRSensitivityDataBundle> {

  /**
   * The unique instance of the SABR sensitivity calculator.
   */
  private static final PresentValueSABRSensitivitySABRRightExtrapolationCalculator INSTANCE = new PresentValueSABRSensitivitySABRRightExtrapolationCalculator();

  /**
   * Returns the instance of the calculator.
   * @return The instance.
   */
  public static PresentValueSABRSensitivitySABRRightExtrapolationCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private PresentValueSABRSensitivitySABRRightExtrapolationCalculator() {
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
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final CapFloorIborSABRExtrapolationRightMethod method = new CapFloorIborSABRExtrapolationRightMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return method.presentValueSABRSensitivity(cap, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRExtrapolationRightCalculator visitor visitCapFloorIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final SwaptionCashFixedIborSABRExtrapolationRightMethod method = new SwaptionCashFixedIborSABRExtrapolationRightMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return method.presentValueSABRSensitivity(swaption, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRExtrapolationRightCalculator visitor visitSwaptionCashFixedIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final SwaptionPhysicalFixedIborSABRExtrapolationRightMethod method = new SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return method.presentValueSABRSensitivity(swaption, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRExtrapolationRightCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateDataBundle as data.");

  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponCMS(final CouponCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final CouponCMSSABRExtrapolationRightReplicationMethod replication = new CouponCMSSABRExtrapolationRightReplicationMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return replication.presentValueSABRSensitivity(payment, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRExtrapolationRightCalculator visitor visitCouponCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMS(final CapFloorCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final CapFloorCMSSABRExtrapolationRightReplicationMethod replication = new CapFloorCMSSABRExtrapolationRightReplicationMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return replication.presentValueSABRSensitivity(payment, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRExtrapolationRightCalculator visitor visitCapFloorCMS requires a SABRInterestRateDataBundle as data.");
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
    throw new UnsupportedOperationException(
        "The PresentValueSABRSensitivitySABRExtrapolationRightCalculator visitor visitCapFloorCMSSpread requires a SABRInterestRateDataBundle with correlation as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    for (final Payment p : annuity.getPayments()) {
      pvss = PresentValueSABRSensitivityDataBundle.plus(pvss, visit(p, curves));
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
      pvss = PresentValueSABRSensitivityDataBundle.plus(pvss, visit(p, curves));
    }
    for (final Payment p : swap.getSecondLeg().getPayments()) {
      pvss = PresentValueSABRSensitivityDataBundle.plus(pvss, visit(p, curves));
    }
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    final CapFloorIbor capFloor = CapFloorIbor.from(coupon, 0, true);
    return visit(capFloor, curves);
  }
}
