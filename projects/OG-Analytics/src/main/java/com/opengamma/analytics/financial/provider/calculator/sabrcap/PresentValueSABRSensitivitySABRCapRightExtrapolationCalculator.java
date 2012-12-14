/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabrcap;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborSABRCapExtrapolationRightMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueSABRSensitivitySABRCapRightExtrapolationCalculator extends
    InstrumentDerivativeVisitorSameMethodAdapter<SABRCapProviderInterface, PresentValueSABRSensitivityDataBundle> {

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
  private final CapFloorIborSABRCapExtrapolationRightMethod _methodExtraCap;

  /**
   * Constructor.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public PresentValueSABRSensitivitySABRCapRightExtrapolationCalculator(final double cutOffStrike, final double mu) {
    _mu = mu;
    _cutOffStrike = cutOffStrike;
    _methodExtraCap = new CapFloorIborSABRCapExtrapolationRightMethod(_cutOffStrike, _mu);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative, final SABRCapProviderInterface sabr) {
    return derivative.accept(this, sabr);
  }

  // -----     Payment/Coupon     ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorIbor(final CapFloorIbor cap, final SABRCapProviderInterface sabr) {
    return _methodExtraCap.presentValueSABRSensitivity(cap, sabr);
  }

  // -----     Annuity     ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitGenericAnnuity(final Annuity<? extends Payment> annuity, final SABRCapProviderInterface sabr) {
    ArgumentChecker.notNull(annuity, "Annuity");
    PresentValueSABRSensitivityDataBundle pvss = visit(annuity.getNthPayment(0), sabr);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      pvss = pvss.plus(visit(annuity.getNthPayment(loopp), sabr));
    }
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
