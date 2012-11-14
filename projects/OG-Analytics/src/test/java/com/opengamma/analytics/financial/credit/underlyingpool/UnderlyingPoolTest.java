/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.CreditSpreadTenors;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRating;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligormodel.Region;
import com.opengamma.analytics.financial.credit.obligormodel.Sector;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.util.money.Currency;

/**
 * Tests to verify the correct construction of an underlying pool object 
 */
public class UnderlyingPoolTest {

  // ----------------------------------------------------------------------------------

  // TODO : Add the obligor credit spread term structures

  // ----------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

  // ----------------------------------------------------------------------------------

  // Define the composition of the underlying pool

  private static final int numberOfObligors = 3;
  private static final int numberOfTenors = 4;

  private static final Obligor[] obligors = new Obligor[numberOfObligors];

  private static final double[] notionals = new double[numberOfObligors];
  private static final double[] coupons = new double[numberOfObligors];
  private static final double[] recoveryRates = new double[numberOfObligors];
  private static final double[] obligorWeights = new double[numberOfObligors];

  private static final Currency[] currency = new Currency[numberOfObligors];
  private static final DebtSeniority[] debtSeniority = new DebtSeniority[numberOfObligors];
  private static final RestructuringClause[] restructuringClause = new RestructuringClause[numberOfObligors];

  private static final CreditSpreadTenors[] creditSpreadTenors = new CreditSpreadTenors[numberOfTenors];
  private static final double[][] spreadTermStructures = new double[numberOfObligors][numberOfTenors];

  private static final YieldCurve[] yieldCurve = new YieldCurve[numberOfObligors];

  private static final double[] obligorNotionals = {10000000.0, 10000000.0, 10000000.0 };
  private static final double[] obligorCoupons = {100.0, 100.0, 100.0 };
  private static final double[] obligorRecoveryRates = {0.40, 0.40, 0.40 };
  private static final double[] obligorIndexWeights = {1.0 / numberOfObligors, 1.0 / numberOfObligors, 1.0 / numberOfObligors };

  private static final Currency[] obligorCurrencies = {Currency.USD, Currency.USD, Currency.EUR };
  private static final DebtSeniority[] obligorDebtSeniorities = {DebtSeniority.SENIOR, DebtSeniority.SENIOR, DebtSeniority.SENIOR };
  private static final RestructuringClause[] obligorRestructuringClauses = {RestructuringClause.NORE, RestructuringClause.NORE, RestructuringClause.MODRE };

  private static final CreditSpreadTenors[] obligorCreditSpreadTenors = {CreditSpreadTenors._3Y, CreditSpreadTenors._5Y, CreditSpreadTenors._7Y, CreditSpreadTenors._10Y };

  private static final YieldCurve[] obligorYieldCurves = {null, null, null };

  private static final String[] obligorTickers = {"MSFT", "IBM", "BT" };
  private static final String[] obligorShortName = {"Microsoft", "International Business Machine", "British Telecom" };
  private static final String[] obligorREDCode = {"ABC123", "XYZ321", "123ABC" };

  private static final CreditRating[] obligorCompositeRating = {CreditRating.AA, CreditRating.AA, CreditRating.AA };
  private static final CreditRating[] obligorImpliedRating = {CreditRating.AA, CreditRating.AA, CreditRating.AA };
  private static final CreditRatingMoodys[] obligorCreditRatingMoodys = {CreditRatingMoodys.AA, CreditRatingMoodys.AA, CreditRatingMoodys.AA };
  private static final CreditRatingStandardAndPoors[] obligorCreditRatingStandardAndPoors = {CreditRatingStandardAndPoors.AA, CreditRatingStandardAndPoors.AA, CreditRatingStandardAndPoors.AA };
  private static final CreditRatingFitch[] obligorCreditRatingFitch = {CreditRatingFitch.AA, CreditRatingFitch.AA, CreditRatingFitch.AA };

  private static final boolean[] obligorHasDefaulted = {false, false, false };

  private static final Sector[] obligorSector = {Sector.INDUSTRIALS, Sector.INDUSTRIALS, Sector.INDUSTRIALS };
  private static final Region[] obligorRegion = {Region.NORTHAMERICA, Region.NORTHAMERICA, Region.EUROPE };
  private static final String[] obligorCountry = {"United States", "United States", "United Kingdom" };

  // ----------------------------------------------------------------------------------

  // Initialise the obligors in the pool
  private void initialiseObligorsInPool() {

    // Loop over each of the obligors in the pool
    for (int i = 0; i < numberOfObligors; i++) {

      // Build obligor i
      final Obligor obligor = new Obligor(
          obligorTickers[i],
          obligorShortName[i],
          obligorREDCode[i],
          obligorCompositeRating[i],
          obligorImpliedRating[i],
          obligorCreditRatingMoodys[i],
          obligorCreditRatingStandardAndPoors[i],
          obligorCreditRatingFitch[i],
          obligorHasDefaulted[i],
          obligorSector[i],
          obligorRegion[i],
          obligorCountry[i]);

      // Assign obligor i
      obligors[i] = obligor;

      // Assign the currency of obligor i
      currency[i] = obligorCurrencies[i];

      // Assign the debt seniority of obligor i
      debtSeniority[i] = obligorDebtSeniorities[i];

      // Assign the restructuring clause of obligor i
      restructuringClause[i] = obligorRestructuringClauses[i];

      // Assign the term structure of credit spreads for obligor i
      for (int j = 0; j < numberOfTenors; j++) {
        spreadTermStructures[i][j] = i * j;
      }

      // Assign the notional amount for obligor i
      notionals[i] = obligorNotionals[i];

      // Assign the coupon for obligor i
      coupons[i] = obligorCoupons[i];

      // Assign the recovery rate for obligor i
      recoveryRates[i] = obligorRecoveryRates[i];

      // Assign the weight of obligor i in the index
      obligorWeights[i] = obligorIndexWeights[i];

      yieldCurve[i] = obligorYieldCurves[i];
    }

    // Assign the credit spread tenors
    for (int j = 0; j < numberOfTenors; j++) {
      creditSpreadTenors[j] = obligorCreditSpreadTenors[j];
    }
  }

  //--------------------------------------------------------------------------------------------------------------------------------------------------

  // Build the underlying pool
  private final UnderlyingPool constructPool() {

    // Initialise the obligors in the pool
    initialiseObligorsInPool();

    // Call the pool constructor
    UnderlyingPool underlyingPool = new UnderlyingPool(
        obligors,
        currency,
        debtSeniority,
        restructuringClause,
        creditSpreadTenors,
        spreadTermStructures,
        notionals,
        coupons,
        recoveryRates,
        obligorWeights,
        yieldCurve);

    return underlyingPool;
  }

  // ----------------------------------------------------------------------------------

  // Test the construction of an underlying pool from user input data

  @Test
  public void testUnderlyingPoolConstruction() {

    UnderlyingPool dummyPool = constructPool();

    int n = dummyPool.getNumberOfObligors();

    if (outputResults) {
      System.out.println("Num of obligors in pool = " + n);

      for (int j = 0; j < numberOfTenors; j++) {
        System.out.print(dummyPool.getCreditSpreadTenors()[j] + "\t");
      }
      System.out.println();

      for (int i = 0; i < n; i++) {
        System.out.print("Obligor i = " + i + "\t" +
            dummyPool.getObligors()[i].getObligorTicker() + "\t" +
            dummyPool.getObligorNotionals()[i] + "\t" +
            dummyPool.getObligorWeights()[i] + "\t" +
            dummyPool.getCurrency()[i] + "\t" +
            dummyPool.getDebtSeniority()[i] + "\t" +
            dummyPool.getRestructuringClause()[i] + "\t");

        for (int j = 0; j < numberOfTenors; j++) {
          System.out.print(dummyPool.getSpreadTermStructures()[i][j] + "\t");
        }

        System.out.print(dummyPool.getRecoveryRates()[i] + "\t" + dummyPool.getYieldCurves()[i]);

        System.out.println();
      }
    }
  }
  // ----------------------------------------------------------------------------------
}
