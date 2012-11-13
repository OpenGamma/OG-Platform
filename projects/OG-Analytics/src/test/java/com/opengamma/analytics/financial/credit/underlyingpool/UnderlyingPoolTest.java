/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.obligormodel.CreditRating;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligormodel.Region;
import com.opengamma.analytics.financial.credit.obligormodel.Sector;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;

/**
 * Tests to verify the correct construction of an underlying pool object 
 */
public class UnderlyingPoolTest {

  // ----------------------------------------------------------------------------------

  private static final int numberOfObligors = 3;

  private static final double[] coupons = new double[numberOfObligors];
  private static final double[] recoveryRates = new double[numberOfObligors];
  private static final double[] obligorWeights = new double[numberOfObligors];

  private static final Obligor[] obligors = new Obligor[numberOfObligors];

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

    for (int i = 0; i < numberOfObligors; i++) {

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

      obligors[i] = obligor;

      coupons[i] = 100.0;
      recoveryRates[i] = 0.40;
      obligorWeights[i] = 1.0 / numberOfObligors;
    }
  }

  private final UnderlyingPool constructPool() {

    initialiseObligorsInPool();

    UnderlyingPool underlyingPool = new UnderlyingPool(obligors, coupons, recoveryRates, obligorWeights);

    return underlyingPool;
  }

  // ----------------------------------------------------------------------------------

  @Test
  public void testUnderlyingPoolConstruction() {

    UnderlyingPool bespokePool = constructPool();
  }

  // ----------------------------------------------------------------------------------
}
