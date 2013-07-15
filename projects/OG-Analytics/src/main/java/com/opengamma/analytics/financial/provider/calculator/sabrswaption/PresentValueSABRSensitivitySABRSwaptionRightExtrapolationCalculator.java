/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabrswaption;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorCMSSpreadSABRBinormalMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator extends
    InstrumentDerivativeVisitorSameMethodAdapter<SABRSwaptionProviderInterface, PresentValueSABRSensitivityDataBundle> {

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
  private final SwaptionCashFixedIborSABRExtrapolationRightMethod _methodSwptCash;

  /**
   * Constructor.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator(final double cutOffStrike, final double mu) {
    _mu = mu;
    _cutOffStrike = cutOffStrike;
    _methodExtraCMSCpn = new CouponCMSSABRExtrapolationRightReplicationMethod(_cutOffStrike, _mu);
    _methodExtraCMSCap = new CapFloorCMSSABRExtrapolationRightReplicationMethod(_cutOffStrike, _mu);
    _methodSwptCash = new SwaptionCashFixedIborSABRExtrapolationRightMethod(_cutOffStrike, _mu);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative, final SABRSwaptionProviderInterface sabr) {
    return derivative.accept(this, sabr);
  }

  // -----     Payment/Coupon     ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponCMS(final CouponCMS payment, final SABRSwaptionProviderInterface sabr) {
    return _methodExtraCMSCpn.presentValueSABRSensitivity(payment, sabr);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMS(final CapFloorCMS payment, final SABRSwaptionProviderInterface sabr) {
    return _methodExtraCMSCap.presentValueSABRSensitivity(payment, sabr);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final SABRSwaptionProviderInterface sabr) {
    if (sabr.getSABRParameter() instanceof SABRInterestRateCorrelationParameters) {
      // TODO: improve correlation data handling
      final SABRInterestRateCorrelationParameters sabrCorrelation = (SABRInterestRateCorrelationParameters) sabr.getSABRParameter();
      final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(sabrCorrelation.getCorrelation(), _methodExtraCMSCap, _methodExtraCMSCpn);
      return method.presentValueSABRSensitivity(payment, sabr);
    }
    throw new UnsupportedOperationException(
        "The PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator visitor visitCapFloorCMSSpread requires a SABRInterestRateCorrelationParameters as data.");
  }

  // -----     Annuity     ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitGenericAnnuity(final Annuity<? extends Payment> annuity, final SABRSwaptionProviderInterface sabr) {
    ArgumentChecker.notNull(annuity, "Annuity");
    PresentValueSABRSensitivityDataBundle pvss = visit(annuity.getNthPayment(0), sabr);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      pvss = pvss.plus(visit(annuity.getNthPayment(loopp), sabr));
    }
    return pvss;
  }

  // -----     Swaption     ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final SABRSwaptionProviderInterface sabr) {
    return _methodSwptCash.presentValueSABRSensitivity(swaption, sabr);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
