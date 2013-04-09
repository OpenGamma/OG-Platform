/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CreditInstrumentDefinition;
import com.opengamma.analytics.financial.credit.CreditInstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;

/**
 * 
 */
public class ISDACreditDefaultSwapPVCalculator {
  private static final ISDACompliantPremiumLegCalculator PREMIUM_LEG_CALCULATOR = new ISDACompliantPremiumLegCalculator();
  private static final ISDACompliantContingentLegCalculator CONTINGENT_LEG_CALCULATOR = new ISDACompliantContingentLegCalculator();

  public double getPresentValue(final CreditInstrumentDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data, final ZonedDateTime valuationDate,
      final PriceType priceType) {
    final TempVisitor visitor = new TempVisitor(valuationDate, priceType);
    return cds.accept(visitor, data);
  }

  private class TempVisitor extends CreditInstrumentDefinitionVisitorAdapter<ISDAYieldCurveAndHazardRateCurveProvider, Double> {
    private final ZonedDateTime _valuationDate;
    private final PriceType _priceType;

    public TempVisitor(final ZonedDateTime valuationDate, final PriceType priceType) {
      _valuationDate = valuationDate;
      _priceType = priceType;
    }

    @Override
    public Double visitLegacyVanillaCDS(final LegacyVanillaCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      // Calculate the value of the premium leg (including accrued if required)
      final ISDADateCurve yieldCurve = data.getYieldCurve();
      final HazardRateCurve hazardRateCurve = data.getHazardRateCurve();
      final double presentValuePremiumLeg = PREMIUM_LEG_CALCULATOR.calculatePremiumLeg(_valuationDate, cds, yieldCurve, hazardRateCurve, _priceType);
      // Calculate the value of the contingent leg
      final double presentValueContingentLeg = CONTINGENT_LEG_CALCULATOR.calculateContingentLeg(_valuationDate, cds, yieldCurve, hazardRateCurve);
      // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
      double presentValue = -(cds.getParSpread() / 10000.0) * presentValuePremiumLeg + presentValueContingentLeg;

      //TODO
      /*
        // If we require the clean price, then calculate the accrued interest and add this to the PV
        if (priceType == PriceType.CLEAN) {
          presentValue += (cds.getParSpread() / 10000.0) * presentValueCreditDefaultSwap.calculateAccruedInterest(valuationDate, cds);
        }
       */
      // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
      if (cds.getBuySellProtection() == BuySellProtection.SELL) {
        presentValue = -1 * presentValue;
      }
      return presentValue;
    }
  }
}
