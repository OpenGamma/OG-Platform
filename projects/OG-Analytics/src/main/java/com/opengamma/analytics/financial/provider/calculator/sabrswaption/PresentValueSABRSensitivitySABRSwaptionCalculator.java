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
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorCMSSABRReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorCMSSpreadSABRBinormalMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponCMSSABRReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborSABRMethod;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueSABRSensitivitySABRSwaptionCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<SABRSwaptionProviderInterface, PresentValueSABRSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSABRSensitivitySABRSwaptionCalculator INSTANCE = new PresentValueSABRSensitivitySABRSwaptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueSABRSensitivitySABRSwaptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueSABRSensitivitySABRSwaptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final CouponCMSSABRReplicationMethod METHOD_CMS_CPN = CouponCMSSABRReplicationMethod.getInstance();
  private static final CapFloorCMSSABRReplicationMethod METHOD_CMS_CAP = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWT_PHYS = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final SwaptionCashFixedIborSABRMethod METHOD_SWT_CASH = SwaptionCashFixedIborSABRMethod.getInstance();

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative, final SABRSwaptionProviderInterface sabr) {
    return derivative.accept(this, sabr);
  }

  // -----     Payment/Coupon     ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponCMS(final CouponCMS payment, final SABRSwaptionProviderInterface sabr) {
    return METHOD_CMS_CPN.presentValueSABRSensitivity(payment, sabr);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMS(final CapFloorCMS payment, final SABRSwaptionProviderInterface sabr) {
    return METHOD_CMS_CAP.presentValueSABRSensitivity(payment, sabr);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final SABRSwaptionProviderInterface sabr) {
    if (sabr.getSABRParameter() instanceof SABRInterestRateCorrelationParameters) {
      // TODO: improve correlation data handling
      final SABRInterestRateCorrelationParameters sabrCorrelation = (SABRInterestRateCorrelationParameters) sabr.getSABRParameter();
      final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(sabrCorrelation.getCorrelation(), METHOD_CMS_CAP, METHOD_CMS_CPN);
      return method.presentValueSABRSensitivity(payment, sabr);
    }
    throw new UnsupportedOperationException("The PresentValueSABRSensitivitySABRSwaptionCalculator visitor visitCapFloorCMSSpread requires a SABRInterestRateCorrelationParameters as data.");
  }

  // -----     Annuity     ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitGenericAnnuity(final Annuity<? extends Payment> annuity, final SABRSwaptionProviderInterface sabr) {
    ArgumentChecker.notNull(annuity, "Annuity");
    PresentValueSABRSensitivityDataBundle cs = visit(annuity.getNthPayment(0), sabr);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      cs = cs.plus(visit(annuity.getNthPayment(loopp), sabr));
    }
    return cs;
  }

  // -----     Swaption     ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabr) {
    return METHOD_SWT_PHYS.presentValueSABRSensitivity(swaption, sabr);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final SABRSwaptionProviderInterface sabr) {
    return METHOD_SWT_CASH.presentValueSABRSensitivity(swaption, sabr);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
