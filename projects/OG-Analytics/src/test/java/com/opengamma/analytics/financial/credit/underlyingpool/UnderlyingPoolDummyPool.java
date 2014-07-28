/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool;

import static com.google.common.collect.Sets.newHashSet;

import com.opengamma.analytics.financial.credit.CreditSpreadTenors;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.util.money.Currency;

/**
 * Dummy pool of obligors used for testing purposes
 */
public class UnderlyingPoolDummyPool {

  //--------------------------------------------------------------------------------------------------------------------------------------------------

  // TODO :

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Define the composition of the underlying pool

  private static final String poolName = "Test_1";

  private static final int numberOfObligors = 5;
  private static final int numberOfTenors = 4;

  private static final LegalEntity[] obligors = new LegalEntity[numberOfObligors];

  private static final double[] notionals = new double[numberOfObligors];
  private static final double[] coupons = new double[numberOfObligors];
  private static final double[] recoveryRates = new double[numberOfObligors];
  private static final double[] obligorWeights = new double[numberOfObligors];

  private static final Currency[] currency = new Currency[numberOfObligors];
  private static final DebtSeniority[] debtSeniority = new DebtSeniority[numberOfObligors];
  private static final RestructuringClause[] restructuringClause = new RestructuringClause[numberOfObligors];

  private static final double[] obligorNotionals = {10000000.0, 10000000.0, 10000000.0, 5000000.0, 12000000.0 };
  private static final double[] obligorCoupons = {100.0, 100.0, 100.0, 500.0, 25.0 };
  private static final double[] obligorRecoveryRates = {0.36, 0.40, 0.25, 0.76, 0.15 };
  private static final double[] obligorIndexWeights = {1.0 / numberOfObligors, 1.0 / numberOfObligors, 1.0 / numberOfObligors, 1.0 / numberOfObligors, 1.0 / numberOfObligors };

  private static final Currency[] obligorCurrencies = {Currency.USD, Currency.USD, Currency.EUR, Currency.EUR, Currency.JPY };
  private static final DebtSeniority[] obligorDebtSeniorities = {DebtSeniority.SENIOR, DebtSeniority.SENIOR, DebtSeniority.SENIOR, DebtSeniority.SUBORDINATED, DebtSeniority.SUBORDINATED };
  private static final RestructuringClause[] obligorRestructuringClauses = {RestructuringClause.XR, 
                                                                            RestructuringClause.XR, 
                                                                            RestructuringClause.MR, 
                                                                            RestructuringClause.MR,
                                                                            RestructuringClause.MM };

  private static final CreditSpreadTenors[] obligorCreditSpreadTenors = {CreditSpreadTenors._3Y, CreditSpreadTenors._5Y, CreditSpreadTenors._7Y, CreditSpreadTenors._10Y };

  private static final String[] obligorTickers = {"MSFT", "IBM", "BT", "BARC", "NOM" };
  private static final String[] obligorShortName = {"Microsoft", "International Business Machines", "British Telecom", "Barclays", "Nomura" };
  private static final String[] obligorREDCode = {"ABC123", "XYZ321", "123ABC", "XXX999", "AAA111" };

  private static final CreditRating[] obligorCompositeRating = {CreditRating.AA, CreditRating.AA, CreditRating.AA, CreditRating.AA, CreditRating.AA };
  private static final CreditRating[] obligorImpliedRating = {CreditRating.AA, CreditRating.AA, CreditRating.AA, CreditRating.AA, CreditRating.AA };
  private static final CreditRatingMoodys[] obligorCreditRatingMoodys = {CreditRatingMoodys.AA, CreditRatingMoodys.AA, CreditRatingMoodys.AA, CreditRatingMoodys.AA, CreditRatingMoodys.AA };
  private static final CreditRatingStandardAndPoors[] obligorCreditRatingStandardAndPoors = {CreditRatingStandardAndPoors.AA, CreditRatingStandardAndPoors.AA, CreditRatingStandardAndPoors.AA,
    CreditRatingStandardAndPoors.AA, CreditRatingStandardAndPoors.AA };
  private static final CreditRatingFitch[] obligorCreditRatingFitch = {CreditRatingFitch.AA, CreditRatingFitch.AA, CreditRatingFitch.AA, CreditRatingFitch.AA, CreditRatingFitch.AA };

  private static final boolean[] obligorHasDefaulted = {false, false, false, false, false };

  private static final Sector[] obligorSector = {Sector.INDUSTRIALS, Sector.INDUSTRIALS, Sector.INDUSTRIALS, Sector.FINANCIALS, Sector.FINANCIALS };
  private static final Region[] obligorRegion = {Region.NORTHAMERICA, Region.NORTHAMERICA, Region.EUROPE, Region.EUROPE, Region.ASIA };
  private static final String[] obligorCountry = {"United States", "United States", "United Kingdom", "United Kingdom", "Japan" };

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  //Initialise the obligors in the pool
  private static void initialiseObligorsInPool() {

    // Loop over each of the obligors in the pool
    for (int i = 0; i < numberOfObligors; i++) {

      // Build obligor i

      final LegalEntity obligor = new LegalEntity(
          obligorTickers[i],
          obligorShortName[i],
          newHashSet(obligorCompositeRating[i].toCreditRating(), obligorCreditRatingMoodys[i].toCreditRating(), obligorCreditRatingFitch[i].toCreditRating(),
              obligorCreditRatingStandardAndPoors[i].toCreditRating(), obligorImpliedRating[i].toCreditRating()),
          obligorSector[i].toSector(),
          obligorRegion[i].toRegion(),
          obligorHasDefaulted[i]);

      // Assign obligor i
      obligors[i] = obligor;

      // Assign the currency of obligor i
      currency[i] = obligorCurrencies[i];

      // Assign the debt seniority of obligor i
      debtSeniority[i] = obligorDebtSeniorities[i];

      // Assign the restructuring clause of obligor i
      restructuringClause[i] = obligorRestructuringClauses[i];

      // Assign the notional amount for obligor i
      notionals[i] = obligorNotionals[i];

      // Assign the coupon for obligor i
      coupons[i] = obligorCoupons[i];

      // Assign the recovery rate for obligor i
      recoveryRates[i] = obligorRecoveryRates[i];

      // Assign the weight of obligor i in the index
      obligorWeights[i] = obligorIndexWeights[i];
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Assign the credit spread tenors
  public static final CreditSpreadTenors[] assignCreditSpreadTenors() {

    final CreditSpreadTenors[] creditSpreadTenors = new CreditSpreadTenors[numberOfTenors];

    for (int m = 0; m < numberOfTenors; m++) {
      creditSpreadTenors[m] = obligorCreditSpreadTenors[m];
    }

    return creditSpreadTenors;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Assign the credit spread term structures for each obligor in the underlying pool
  public static final double[][] assignCreditSpreadTermStructures() {

    final double[][] spreadTermStructures = new double[numberOfObligors][numberOfTenors];

    for (int i = 0; i < numberOfObligors; i++) {
      for (int j = 0; j < numberOfTenors; j++) {
        spreadTermStructures[i][j] = (j + 1) * 100.0 + (i + 1) * 100.0;
      }
    }

    return spreadTermStructures;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Build the underlying pool
  public static final UnderlyingPool constructPool() {

    // Initialise the obligors in the pool
    initialiseObligorsInPool();

    // Call the UnderlyingPool constructor
    final UnderlyingPool underlyingPool = new UnderlyingPool(
        poolName,
        obligors,
        currency,
        debtSeniority,
        restructuringClause,
        notionals,
        coupons,
        recoveryRates,
        obligorWeights);

    return underlyingPool;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------
}
