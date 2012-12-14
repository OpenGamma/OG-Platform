/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabrswaption;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorCMSSpreadSABRBinormalMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueSABRSwaptionRightExtrapolationCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<SABRSwaptionProviderInterface, MultipleCurrencyAmount> {

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
  private final SwaptionPhysicalFixedIborSABRExtrapolationRightMethod _methodSwptPhys;
  private final SwaptionCashFixedIborSABRExtrapolationRightMethod _methodSwptCash;

  /**
   * Constructor.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public PresentValueSABRSwaptionRightExtrapolationCalculator(final double cutOffStrike, final double mu) {
    _mu = mu;
    _cutOffStrike = cutOffStrike;
    _methodExtraCMSCpn = new CouponCMSSABRExtrapolationRightReplicationMethod(_cutOffStrike, _mu);
    _methodExtraCMSCap = new CapFloorCMSSABRExtrapolationRightReplicationMethod(_cutOffStrike, _mu);
    _methodSwptPhys = new SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(_cutOffStrike, _mu);
    _methodSwptCash = new SwaptionCashFixedIborSABRExtrapolationRightMethod(_cutOffStrike, _mu);
  }

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative, final SABRSwaptionProviderInterface sabr) {
    return derivative.accept(this, sabr);
  }

  // -----     Payment/Coupon     ------

  @Override
  public MultipleCurrencyAmount visitCouponCMS(final CouponCMS payment, final SABRSwaptionProviderInterface sabr) {
    return _methodExtraCMSCpn.presentValue(payment, sabr);
  }

  @Override
  public MultipleCurrencyAmount visitCapFloorCMS(final CapFloorCMS payment, final SABRSwaptionProviderInterface sabr) {
    return _methodExtraCMSCap.presentValue(payment, sabr);
  }

  @Override
  public MultipleCurrencyAmount visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final SABRSwaptionProviderInterface sabr) {
    if (sabr.getSABRParameter() instanceof SABRInterestRateCorrelationParameters) {
      // TODO: improve correlation data handling
      final SABRInterestRateCorrelationParameters sabrCorrelation = (SABRInterestRateCorrelationParameters) sabr.getSABRParameter();
      final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(sabrCorrelation.getCorrelation(), _methodExtraCMSCap, _methodExtraCMSCpn);
      return method.presentValue(payment, sabr);
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitCapFloorCMSSpread requires a SABRInterestRateDataBundle with correlation as data.");
  }

  // -----     Annuity     ------

  @Override
  public MultipleCurrencyAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity, final SABRSwaptionProviderInterface sabr) {
    ArgumentChecker.notNull(annuity, "Annuity");
    MultipleCurrencyAmount pv = visit(annuity.getNthPayment(0), sabr);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      pv = pv.plus(visit(annuity.getNthPayment(loopp), sabr));
    }
    return pv;
  }

  // -----     Swaption     ------

  @Override
  public MultipleCurrencyAmount visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabr) {
    return _methodSwptPhys.presentValue(swaption, sabr);
  }

  @Override
  public MultipleCurrencyAmount visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final SABRSwaptionProviderInterface sabr) {
    return _methodSwptCash.presentValue(swaption, sabr);
  }

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
