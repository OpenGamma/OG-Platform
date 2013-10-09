/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorCMSSpreadSABRBinormalMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionCashFixedIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;

/**
 * Present value sensitivity to SABR parameters calculator for interest rate instruments using SABR volatility formula.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class PresentValueSABRSensitivitySABRRightExtrapolationCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, PresentValueSABRSensitivityDataBundle> {

  /**
   * The cut-off strike. The smile is extrapolated above that level.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;

  /**
   * The methods.
   */
  private final CouponCMSSABRExtrapolationRightReplicationMethod _methodExtraCMSCpn;
  private final CapFloorCMSSABRExtrapolationRightReplicationMethod _methodExtraCMSCap;

  /**
   * Constructor.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public PresentValueSABRSensitivitySABRRightExtrapolationCalculator(final double cutOffStrike, final double mu) {
    _mu = mu;
    _cutOffStrike = cutOffStrike;
    _methodExtraCMSCpn = new CouponCMSSABRExtrapolationRightReplicationMethod(_cutOffStrike, _mu);
    _methodExtraCMSCap = new CapFloorCMSSABRExtrapolationRightReplicationMethod(_cutOffStrike, _mu);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorIbor(final CapFloorIbor cap, final YieldCurveBundle curves) {
    Validate.notNull(cap);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final CapFloorIborSABRExtrapolationRightMethod method = new CapFloorIborSABRExtrapolationRightMethod(_cutOffStrike, _mu);
      return method.presentValueSABRSensitivity(cap, sabr);

    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRExtrapolationRightCalculator visitor visitCapFloorIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final SwaptionCashFixedIborSABRExtrapolationRightMethod method = new SwaptionCashFixedIborSABRExtrapolationRightMethod(_cutOffStrike, _mu);
      return method.presentValueSABRSensitivity(swaption, sabr);

    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRExtrapolationRightCalculator visitor visitSwaptionCashFixedIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final SwaptionPhysicalFixedIborSABRExtrapolationRightMethod method = new SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(_cutOffStrike, _mu);
      return method.presentValueSABRSensitivity(swaption, sabr);

    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRExtrapolationRightCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateDataBundle as data.");

  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponCMS(final CouponCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      return _methodExtraCMSCpn.presentValueSABRSensitivity(payment, sabr);

    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRExtrapolationRightCalculator visitor visitCouponCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMS(final CapFloorCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      return _methodExtraCMSCap.presentValueSABRSensitivity(payment, sabr);

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
        final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(sabrCorrelation.getCorrelation(), _methodExtraCMSCap, _methodExtraCMSCpn);
        return method.presentValueSABRSensitivity(payment, sabrBundle);
      }
    }
    throw new UnsupportedOperationException(
        "The PresentValueSABRSensitivitySABRExtrapolationRightCalculator visitor visitCapFloorCMSSpread requires a SABRInterestRateDataBundle with correlation as data.");
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    for (final Payment p : annuity.getPayments()) {
      pvss = pvss.plus(p.accept(this, curves));
    }
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponFixed(final CouponFixed coupon, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(coupon);
    final PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
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
    Validate.notNull(curves);
    Validate.notNull(coupon);
    final PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponIborSpread(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(coupon);
    final PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    return pvss;
  }

}
