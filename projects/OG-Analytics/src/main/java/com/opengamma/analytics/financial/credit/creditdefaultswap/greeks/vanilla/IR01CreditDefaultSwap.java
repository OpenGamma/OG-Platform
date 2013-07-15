/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.InterestRateBumpType;
import com.opengamma.analytics.financial.credit.marketdatachecker.SpreadTermStructureDataChecker;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of IR01 for a vanilla Legacy CDS (parallel and bucketed bumps)
 */
public class IR01CreditDefaultSwap {
  private static final Logger s_logger = LoggerFactory.getLogger(IR01CreditDefaultSwap.class);

  //------------------------------------------------------------------------------------------------------------------------------------------

  private final double _tolerance = 1e-15;

  private static final DayCount ACT365 = new ActualThreeSixtyFive();

  private static final PresentValueCreditDefaultSwap PV_CALCULATOR = new PresentValueCreditDefaultSwap();

  private static final SpreadTermStructureDataChecker DATA_CHECKER = new SpreadTermStructureDataChecker();

  //-------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Further checks on efficacy of input arguments
  // TODO : Need to get the times[] calculation correct
  // TODO : Need to consider more sophisticated sensitivity calculations e.g. algorithmic differentiation

  // NOTE : We enforce rateBump > 0, therefore if the marketSpreads > 0 (an exception is thrown if this is not the case) then bumpedMarketSpreads > 0 by construction

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the IR01 by a parallel bump of each point on the yield curve

  public double getIR01ParallelShiftCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double interestRateBump,
      final InterestRateBumpType interestRateBumpType,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(interestRateBumpType, "Interest rate bump type");
    ArgumentChecker.notNull(priceType, "price type");

    ArgumentChecker.notNegative(interestRateBump, "Interest rate bump");

    // Check the efficacy of the input market data
    DATA_CHECKER.checkSpreadData(valuationDate, /*cds, */marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector to hold the bumped market spreads
    final int nRates = yieldCurve.getNumberOfCurvePoints();
    final Double[] interestRates = yieldCurve.getCurve().getYData();
    final double[] bumpedInterestRates = new double[nRates];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    final double bumpInBp = interestRateBump / 10000;
    // Calculate the bumped spreads
    switch (interestRateBumpType) {
      case ADDITIVE_PARALLEL:
        for (int m = 0; m < nRates; m++) {
          bumpedInterestRates[m] = interestRates[m] + bumpInBp;
        }
        break;
      case MULTIPLICATIVE_PARALLEL:
        for (int m = 0; m < nRates; m++) {
          bumpedInterestRates[m] = interestRates[m] * (1 + bumpInBp);
        }
        break;
      default:
        throw new IllegalArgumentException("Cannot support bumps of type " + interestRateBumpType);
    }

    final ISDADateCurve bumpedYieldCurve = new ISDADateCurve("Bumped", yieldCurve.getCurveDates(), yieldCurve.getTimePoints(), bumpedInterestRates, yieldCurve.getOffset());

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the unbumped CDS PV
    final double presentValue = PV_CALCULATOR.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // Calculate the bumped (up) CDS PV
    final double bumpedPresentValue = PV_CALCULATOR.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, bumpedYieldCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the parallel CS01
    return (bumpedPresentValue - presentValue) / interestRateBump;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the IR01 by bumping each point on the yield curve individually by interestRateBump (bump is same for all tenors)

  public double[] getIR01BucketedCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double interestRateBump,
      final InterestRateBumpType interestRateBumpType,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(interestRateBumpType, "interest rate bump type");

    ArgumentChecker.notNegative(interestRateBump, "interest rate bump");

    // Check the efficacy of the input market data
    DATA_CHECKER.checkSpreadData(valuationDate, /*cds, */marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector of bucketed CS01 sensitivities (per tenor)
    final int nRates = yieldCurve.getNumberOfCurvePoints();
    final double[] bucketedIR01 = new double[nRates];
    final double bumpInBp = interestRateBump / 10000;

    // Vector to hold the bumped market spreads
    final Double[] interestRates = yieldCurve.getCurve().getYData();
    final double[] bumpedInterestRates = new double[nRates];
    for (int i = 0; i < nRates; i++) {
      bumpedInterestRates[i] = interestRates[i];
    }
    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the unbumped CDS PV
    final double presentValue = PV_CALCULATOR.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Loop through and bump each of the spreads at each tenor
    ISDADateCurve bumpedYieldCurve;
    double bumpedPresentValue;
    for (int m = 0; m < nRates; m++) {
      switch (interestRateBumpType) {
        case ADDITIVE:
          bumpedInterestRates[m] += bumpInBp;
          try {
            bumpedYieldCurve = new ISDADateCurve("Bumped", marketTenors, yieldCurve.getTimePoints(), bumpedInterestRates, yieldCurve.getOffset());
          } catch (ArrayIndexOutOfBoundsException aioobe) {
            s_logger.error("AIOOBE", aioobe);
            throw aioobe;
          }
          bumpedPresentValue = PV_CALCULATOR.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, bumpedYieldCurve, priceType);
          bucketedIR01[m] = (bumpedPresentValue - presentValue) / interestRateBump;
          bumpedInterestRates[m] -= bumpInBp;
          break;
        case MULTIPLICATIVE:
          bumpedInterestRates[m] *= 1 + bumpInBp;
          bumpedYieldCurve = new ISDADateCurve("Bumped", marketTenors, yieldCurve.getTimePoints(), bumpedInterestRates, yieldCurve.getOffset());
          bumpedPresentValue = PV_CALCULATOR.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, bumpedYieldCurve, priceType);
          bucketedIR01[m] = (bumpedPresentValue - presentValue) / interestRateBump;
          bumpedInterestRates[m] /= 1 + bumpInBp;
          break;
        default:
          throw new IllegalArgumentException("Cannot handle bumps of type " + interestRateBumpType);
      }
    }
    // ----------------------------------------------------------------------------------------------------------------------------------------

    return bucketedIR01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

}
