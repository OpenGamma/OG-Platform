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
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCollateralizedVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyFixedRecoveryCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyForwardStartingCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyMuniCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyQuantoCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyRecoveryLockCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacySovereignCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardCollateralizedVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardFixedRecoveryCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardForwardStartingCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardMuniCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardQuantoCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardRecoveryLockCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardSovereignCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardVanillaCreditDefaultSwapDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACreditDefaultSwapPVCalculator {
  private static final ISDACompliantLegCalculator PREMIUM_LEG_CALCULATOR = new ISDACompliantPremiumLegCalculator();
  private static final ISDACompliantLegCalculator CONTINGENT_LEG_CALCULATOR = new ISDACompliantContingentLegCalculator();

  public double getPresentValue(final CreditInstrumentDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data, final ZonedDateTime valuationDate,
      final PriceType priceType) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(valuationDate, "valuation date");
    ArgumentChecker.notNull(priceType, "price type");
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
    public Double visitStandardVanillaCDS(final StandardVanillaCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return visitStandardCDS(cds, data);
    }

    @Override
    public Double visitStandardFixedRecoveryCDS(final StandardFixedRecoveryCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return visitStandardCDS(cds, data);
    }

    @Override
    public Double visitStandardForwardStartingCDS(final StandardForwardStartingCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return visitStandardCDS(cds, data);
    }

    @Override
    public Double visitStandardMuniCDS(final StandardMuniCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return visitStandardCDS(cds, data);
    }

    @Override
    public Double visitStandardQuantoCDS(final StandardQuantoCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return visitStandardCDS(cds, data);
    }

    @Override
    public Double visitStandardRecoveryLockCDS(final StandardRecoveryLockCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return visitStandardCDS(cds, data);
    }

    @Override
    public Double visitStandardSovereignCDS(final StandardSovereignCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return visitStandardCDS(cds, data);
    }

    @Override
    public Double visitStandardCollateralizedVanillaCDS(final StandardCollateralizedVanillaCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return visitStandardCDS(cds, data);
    }

    @Override
    public Double visitLegacyVanillaCDS(final LegacyVanillaCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return getLegacyCDSPresentValue(cds, data);
    }

    @Override
    public Double visitLegacyFixedRecoveryCDS(final LegacyFixedRecoveryCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return getLegacyCDSPresentValue(cds, data);
    }

    @Override
    public Double visitLegacyForwardStartingCDS(final LegacyForwardStartingCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return getLegacyCDSPresentValue(cds, data);
    }

    @Override
    public Double visitLegacyMuniCDS(final LegacyMuniCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return getLegacyCDSPresentValue(cds, data);
    }

    @Override
    public Double visitLegacyQuantoCDS(final LegacyQuantoCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return getLegacyCDSPresentValue(cds, data);
    }

    @Override
    public Double visitLegacyRecoveryLockCDS(final LegacyRecoveryLockCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return getLegacyCDSPresentValue(cds, data);
    }

    @Override
    public Double visitLegacySovereignCDS(final LegacySovereignCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return getLegacyCDSPresentValue(cds, data);
    }

    @Override
    public Double visitLegacyCollateralizedVanillaCDS(final LegacyCollateralizedVanillaCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      return getLegacyCDSPresentValue(cds, data);
    }

    @SuppressWarnings("synthetic-access")
    private Double getLegacyCDSPresentValue(final LegacyCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      // Calculate the value of the premium leg (including accrued if required)
      final double presentValuePremiumLeg = PREMIUM_LEG_CALCULATOR.calculateLeg(_valuationDate, cds, data, _priceType);
      // Calculate the value of the contingent leg
      final double presentValueContingentLeg = CONTINGENT_LEG_CALCULATOR.calculateLeg(_valuationDate, cds, data, _priceType);
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

    @SuppressWarnings("synthetic-access")
    private Double visitStandardCDS(final StandardCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider data) {
      // Calculate the value of the premium leg (including accrued if required)
      final double presentValuePremiumLeg = PREMIUM_LEG_CALCULATOR.calculateLeg(_valuationDate, cds, data, _priceType);
      // Calculate the value of the contingent leg
      final double presentValueContingentLeg = CONTINGENT_LEG_CALCULATOR.calculateLeg(_valuationDate, cds, data, _priceType);
      // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
      double presentValue = -(cds.getQuotedSpread() / 10000.0) * presentValuePremiumLeg + presentValueContingentLeg;

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
