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
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborSABRCapMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueSABRSensitivitySABRCapCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<SABRCapProviderInterface, PresentValueSABRSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSABRSensitivitySABRCapCalculator INSTANCE = new PresentValueSABRSensitivitySABRCapCalculator();

  /**
   * Constructor.
   */
  private PresentValueSABRSensitivitySABRCapCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueSABRSensitivitySABRCapCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final CapFloorIborSABRCapMethod METHOD_CAP = CapFloorIborSABRCapMethod.getInstance();

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative, final SABRCapProviderInterface sabr) {
    return derivative.accept(this, sabr);
  }

  // -----     Payment/Coupon     ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorIbor(final CapFloorIbor cap, final SABRCapProviderInterface sabr) {
    return METHOD_CAP.presentValueSABRSensitivity(cap, sabr);
  }

  // -----     Annuity     ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitGenericAnnuity(final Annuity<? extends Payment> annuity, final SABRCapProviderInterface sabr) {
    ArgumentChecker.notNull(annuity, "Annuity");
    PresentValueSABRSensitivityDataBundle cs = visit(annuity.getNthPayment(0), sabr);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      cs = cs.plus(visit(annuity.getNthPayment(loopp), sabr));
    }
    return cs;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
