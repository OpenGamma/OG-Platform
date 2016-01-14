/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.model.interestrate.definition.InflationYearOnYearCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationYearOnYearParameters;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProvider;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 *  Tests related to the calibration engine for inflation year on year cap/floor calibration.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorYearOnYearCalibrationObjectiveTest {

  //Cap/floor description
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 9);
  private static final double NOTIONAL = 10000; //100m
  private static final double[] STRIKES = {-.01, .00, .01, .02, .03, .04 };
  private static final boolean IS_CAP = true;
  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[MARKET.getPriceIndexes().size()]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final Period COUPON_PAYMENT_TENOR = Period.ofYears(1);
  private static final int MONTH_LAG = 3;
  private static final ZonedDateTime LAST_KNOWN_FIXING_DATE = DateUtils.getUTCDate(2008, 7, 01);

  private static final InterpolatedDoublesSurface BLACK_SURF = BlackDataSets.createBlackSurfaceExpiryStrikeRate();
  private static final BlackSmileCapInflationYearOnYearParameters BLACK_PARAM = new BlackSmileCapInflationYearOnYearParameters(BLACK_SURF, PRICE_INDEX_EUR);
  private static final BlackSmileCapInflationYearOnYearProviderDiscount BLACK_INFLATION = new BlackSmileCapInflationYearOnYearProviderDiscount(MARKET.getInflationProvider(), BLACK_PARAM);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 7);

  private static final CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod METHOD = CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod.getInstance();

  private static double[] expiryTimes = new double[30];
  private static final double[] strikes = {-.01, .00, .01, .02, .03, .04 };
  private static final double[][] volatilities = { {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 } };

  private static final int[] availabelTenor = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30 };
  AnnuityCapFloorInflationYearOnYearMonthlyDefinition[][] CAP_DEFINITIONS = new AnnuityCapFloorInflationYearOnYearMonthlyDefinition[6][availabelTenor.length];
  Annuity<? extends Payment>[][] CAPS = new Annuity<?>[strikes.length][availabelTenor.length];
  double[][][] marketPrices = new double[strikes.length][availabelTenor.length][expiryTimes.length];

  @Test
  /**
  *  Tests the correctness of inflation year on year cap/floor calibration to market prices.
  *  Calibration basket: CAPLET i y, for i=1 to 30.
  */
  public void calibrationWithAllInstrruments() {

    for (int loop1 = 0; loop1 < strikes.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        final Period tenor = Period.ofYears(availabelTenor[loop2]);

        CAP_DEFINITIONS[loop1][loop2] = AnnuityCapFloorInflationYearOnYearMonthlyDefinition.from(PRICE_INDEX_EUR, SETTLEMENT_DATE, NOTIONAL,
            tenor, COUPON_PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, MONTH_LAG, MONTH_LAG, LAST_KNOWN_FIXING_DATE, strikes[loop1], IS_CAP);
        CAPS[loop1][loop2] = CAP_DEFINITIONS[loop1][loop2].toDerivative(REFERENCE_DATE);
      }
    }
    for (int loopexp = 0; loopexp < CAPS[0][availabelTenor.length - 1].getNumberOfPayments(); loopexp++) {
      final CapFloorInflationYearOnYearMonthly cap = (CapFloorInflationYearOnYearMonthly) CAPS[0][availabelTenor.length - 1].getNthPayment(loopexp);
      expiryTimes[loopexp] = cap.getReferenceEndTime();
    }
    final InflationYearOnYearCapFloorParameters parameters = new InflationYearOnYearCapFloorParameters(expiryTimes, strikes, volatilities, PRICE_INDEX_EUR);
    final SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective objective = new SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective(parameters, CUR);
    final SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine<InflationProviderInterface> calibrationEngine = new SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine<>(
        objective);
    for (int loop1 = 0; loop1 < strikes.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        for (int loop3 = 0; loop3 < CAPS[loop1][loop2].getNumberOfPayments(); loop3++) {
          marketPrices[loop1][loop2][loop3] = METHOD.presentValue(CAPS[loop1][loop2].getNthPayment(loop3), BLACK_INFLATION).getAmount(CUR);
        }
      }
    }

    for (int loop1 = 0; loop1 < strikes.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        for (int loop3 = 0; loop3 < CAPS[loop1][loop2].getNumberOfPayments(); loop3++) {
          calibrationEngine.addInstrument(CAPS[loop1][loop2].getNthPayment(loop3), marketPrices[loop1][loop2][loop3]);
        }
      }
    }
    calibrationEngine.calibrate(MARKET.getInflationProvider());
    final MultipleCurrencyAmount[][][] pvCapYearOnYear = new MultipleCurrencyAmount[STRIKES.length][availabelTenor.length][30];
    for (int loop1 = 0; loop1 < strikes.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        for (int loop3 = 0; loop3 < CAPS[loop1][loop2].getNumberOfPayments(); loop3++) {
          final Interpolator2D interpolator = objective.getInflationCapYearOnYearProvider().getBlackParameters().getVolatilitySurface().getInterpolator();
          final BlackSmileCapInflationYearOnYearParameters CalibratedBlackSmileCapInflationYearOnYearParameters = new BlackSmileCapInflationYearOnYearParameters(
              objective.getInflationCapYearOnYearParameters(), interpolator);
          final BlackSmileCapInflationYearOnYearProvider CalibratedBlackSmileCapInflationYearOnYearProvider = new BlackSmileCapInflationYearOnYearProvider(objective
              .getInflationCapYearOnYearProvider().getInflationProvider(),
              CalibratedBlackSmileCapInflationYearOnYearParameters);
          pvCapYearOnYear[loop1][loop2][loop3] = METHOD.presentValue(CAPS[loop1][loop2].getNthPayment(loop3), CalibratedBlackSmileCapInflationYearOnYearProvider);
          assertEquals("Inflaiton year on year calibration: cap/floor " + loop1, pvCapYearOnYear[loop1][loop2][loop3].getAmount(CUR), marketPrices[loop1][loop2][loop3], 1E-2);
        }
      }
    }
  }

  private static double[] expiryTimes_AVAILABLE = new double[availabelTenor.length];
  private static final double[] strikes_AVAILABLE = {-.01, .00, .01, .02, .03, .04 };
  private static final double[][] volatilities_AVAILABLE = { {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 } };
  AnnuityCapFloorInflationYearOnYearMonthlyDefinition[][] CAP_DEFINITIONS_AVAILABLE = new AnnuityCapFloorInflationYearOnYearMonthlyDefinition[6][availabelTenor.length];
  Annuity<? extends Payment>[][] CAPS_AVAILABLE = new Annuity<?>[strikes_AVAILABLE.length][availabelTenor.length];
  double[][] marketPrices_AVAILABLE = new double[strikes_AVAILABLE.length][availabelTenor.length];

  @Test
  /**
   * Tests the correctness of inflation year on year cap/floor calibration to market prices.
   * Calibration basket: CAP 1y, 2y, 3y, 4y, 5y, 6y, 7y, 8y, 9y, 10y,12y, 15y, 20y, 25y, 30y
   */
  public void calibrationWithOnlyAvailableMarketData() {

    for (int loop1 = 0; loop1 < strikes_AVAILABLE.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        final Period tenor = Period.ofYears(availabelTenor[loop2]);

        CAP_DEFINITIONS_AVAILABLE[loop1][loop2] = AnnuityCapFloorInflationYearOnYearMonthlyDefinition.from(PRICE_INDEX_EUR, SETTLEMENT_DATE, NOTIONAL,
            tenor, COUPON_PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, MONTH_LAG, MONTH_LAG, LAST_KNOWN_FIXING_DATE, strikes_AVAILABLE[loop1], IS_CAP);
        CAPS_AVAILABLE[loop1][loop2] = CAP_DEFINITIONS_AVAILABLE[loop1][loop2].toDerivative(REFERENCE_DATE);
      }
    }
    for (int loopexp = 0; loopexp < availabelTenor.length; loopexp++) {
      final CapFloorInflationYearOnYearMonthly cap = (CapFloorInflationYearOnYearMonthly) CAPS_AVAILABLE[0][loopexp].getNthPayment(CAPS_AVAILABLE[0][loopexp].getNumberOfPayments() - 1);
      expiryTimes_AVAILABLE[loopexp] = cap.getReferenceEndTime();
    }
    final InflationYearOnYearCapFloorParameters parameters = new InflationYearOnYearCapFloorParameters(expiryTimes_AVAILABLE, strikes_AVAILABLE, volatilities_AVAILABLE, PRICE_INDEX_EUR);
    final SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective objective = new SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective(parameters, CUR);
    final SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine<InflationProviderDiscount> calibrationEngine = new SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine<>(
        objective);

    for (int loop1 = 0; loop1 < strikes_AVAILABLE.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        for (int loop3 = 0; loop3 < availabelTenor[loop2]; loop3++) {
          marketPrices_AVAILABLE[loop1][loop2] = marketPrices_AVAILABLE[loop1][loop2] + METHOD.presentValue(CAPS_AVAILABLE[loop1][loop2].getNthPayment(loop3), BLACK_INFLATION).getAmount(CUR);
        }
      }
    }

    for (int loop1 = 0; loop1 < strikes.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {

        calibrationEngine.addInstrument(CAPS_AVAILABLE[loop1][loop2], marketPrices_AVAILABLE[loop1][loop2]);

      }
    }
    calibrationEngine.calibrate(MARKET.getInflationProvider());
    final MultipleCurrencyAmount[][] pvCapYearOnYear = new MultipleCurrencyAmount[STRIKES.length][availabelTenor.length];
    for (int loop1 = 0; loop1 < strikes.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {

        final Interpolator2D interpolator = objective.getInflationCapYearOnYearProvider().getBlackParameters().getVolatilitySurface().getInterpolator();
        final BlackSmileCapInflationYearOnYearParameters CalibratedBlackSmileCapInflationYearOnYearParameters = new BlackSmileCapInflationYearOnYearParameters(
            objective.getInflationCapYearOnYearParameters(), interpolator);
        final BlackSmileCapInflationYearOnYearProvider CalibratedBlackSmileCapInflationYearOnYearProvider = new BlackSmileCapInflationYearOnYearProvider(objective
            .getInflationCapYearOnYearProvider().getInflationProvider(),
            CalibratedBlackSmileCapInflationYearOnYearParameters);
        pvCapYearOnYear[loop1][loop2] = METHOD.presentValue(CAPS_AVAILABLE[loop1][loop2].getNthPayment(0), CalibratedBlackSmileCapInflationYearOnYearProvider);
        for (int loop3 = 1; loop3 < CAPS_AVAILABLE[loop1][loop2].getNumberOfPayments(); loop3++) {
          pvCapYearOnYear[loop1][loop2] = pvCapYearOnYear[loop1][loop2]
              .plus(METHOD.presentValue(CAPS_AVAILABLE[loop1][loop2].getNthPayment(loop3), CalibratedBlackSmileCapInflationYearOnYearProvider));
        }
        assertEquals("Inflaiton year on year calibration: cap/floor " + loop1, pvCapYearOnYear[loop1][loop2].getAmount(CUR), marketPrices_AVAILABLE[loop1][loop2], 1E-2);

      }
    }
  }

  @Test
  /**
   * Tests the correctness of inflation year on year cap/floor calibration to market prices.
   * Calibration basket: CAP 1y, 2y, 3y, 4y, 5y, 6y, 7y, 8y, 9y, 10y,12y, 15y, 20y, 25y, 30y for strikes 2%, 3%, 4%
   *            and      Floor 1y, 2y, 3y, 4y, 5y, 6y, 7y, 8y, 9y, 10y,12y, 15y, 20y, 25y, 30y for strikes -1%, 0%, 1%
   */
  public void calibrationWithOnlyAvailableMarketDataCapAndFloor() {

    for (int loop1 = 0; loop1 < strikes_AVAILABLE.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        final Period tenor = Period.ofYears(availabelTenor[loop2]);
        boolean isCap = true;
        if (loop1 == 0 || loop1 == 1 || loop1 == 2) {
          isCap = false;
        }
        CAP_DEFINITIONS_AVAILABLE[loop1][loop2] = AnnuityCapFloorInflationYearOnYearMonthlyDefinition.from(PRICE_INDEX_EUR, SETTLEMENT_DATE, NOTIONAL,
            tenor, COUPON_PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, MONTH_LAG, MONTH_LAG, LAST_KNOWN_FIXING_DATE, strikes_AVAILABLE[loop1], isCap);
        CAPS_AVAILABLE[loop1][loop2] = CAP_DEFINITIONS_AVAILABLE[loop1][loop2].toDerivative(REFERENCE_DATE);
      }
    }
    for (int loopexp = 0; loopexp < availabelTenor.length; loopexp++) {
      final CapFloorInflationYearOnYearMonthly cap = (CapFloorInflationYearOnYearMonthly) CAPS_AVAILABLE[0][loopexp].getNthPayment(CAPS_AVAILABLE[0][loopexp].getNumberOfPayments() - 1);
      expiryTimes_AVAILABLE[loopexp] = cap.getReferenceEndTime();
    }
    final InflationYearOnYearCapFloorParameters parameters = new InflationYearOnYearCapFloorParameters(expiryTimes_AVAILABLE, strikes_AVAILABLE, volatilities_AVAILABLE, PRICE_INDEX_EUR);
    final SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective objective = new SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective(parameters, CUR);
    final SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine<InflationProviderDiscount> calibrationEngine = new SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine<>(
        objective);

    for (int loop1 = 0; loop1 < strikes_AVAILABLE.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        for (int loop3 = 0; loop3 < availabelTenor[loop2]; loop3++) {
          marketPrices_AVAILABLE[loop1][loop2] = marketPrices_AVAILABLE[loop1][loop2] + METHOD.presentValue(CAPS_AVAILABLE[loop1][loop2].getNthPayment(loop3), BLACK_INFLATION).getAmount(CUR);
        }
      }
    }

    for (int loop1 = 0; loop1 < strikes.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {

        calibrationEngine.addInstrument(CAPS_AVAILABLE[loop1][loop2], marketPrices_AVAILABLE[loop1][loop2]);

      }
    }
    calibrationEngine.calibrate(MARKET.getInflationProvider());
    final MultipleCurrencyAmount[][] pvCapYearOnYear = new MultipleCurrencyAmount[STRIKES.length][availabelTenor.length];
    for (int loop1 = 0; loop1 < strikes.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {

        final Interpolator2D interpolator = objective.getInflationCapYearOnYearProvider().getBlackParameters().getVolatilitySurface().getInterpolator();
        final BlackSmileCapInflationYearOnYearParameters CalibratedBlackSmileCapInflationYearOnYearParameters = new BlackSmileCapInflationYearOnYearParameters(
            objective.getInflationCapYearOnYearParameters(), interpolator);
        final BlackSmileCapInflationYearOnYearProvider CalibratedBlackSmileCapInflationYearOnYearProvider = new BlackSmileCapInflationYearOnYearProvider(objective
            .getInflationCapYearOnYearProvider().getInflationProvider(),
            CalibratedBlackSmileCapInflationYearOnYearParameters);
        pvCapYearOnYear[loop1][loop2] = METHOD.presentValue(CAPS_AVAILABLE[loop1][loop2].getNthPayment(0), CalibratedBlackSmileCapInflationYearOnYearProvider);
        for (int loop3 = 1; loop3 < CAPS_AVAILABLE[loop1][loop2].getNumberOfPayments(); loop3++) {
          pvCapYearOnYear[loop1][loop2] = pvCapYearOnYear[loop1][loop2]
              .plus(METHOD.presentValue(CAPS_AVAILABLE[loop1][loop2].getNthPayment(loop3), CalibratedBlackSmileCapInflationYearOnYearProvider));
        }
        assertEquals("Inflaiton year on year calibration: cap/floor " + loop1, pvCapYearOnYear[loop1][loop2].getAmount(CUR), marketPrices_AVAILABLE[loop1][loop2], 1E-2);

      }
    }
  }

  @Test(enabled = false)
  public void performance() {
    for (int loop1 = 0; loop1 < strikes_AVAILABLE.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        final Period tenor = Period.ofYears(availabelTenor[loop2]);

        CAP_DEFINITIONS_AVAILABLE[loop1][loop2] = AnnuityCapFloorInflationYearOnYearMonthlyDefinition.from(PRICE_INDEX_EUR, SETTLEMENT_DATE, NOTIONAL,
            tenor, COUPON_PAYMENT_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM, MONTH_LAG, MONTH_LAG, LAST_KNOWN_FIXING_DATE, strikes_AVAILABLE[loop1], IS_CAP);
        CAPS_AVAILABLE[loop1][loop2] = CAP_DEFINITIONS_AVAILABLE[loop1][loop2].toDerivative(REFERENCE_DATE);
      }
    }
    for (int loopexp = 0; loopexp < availabelTenor.length; loopexp++) {
      final CapFloorInflationYearOnYearMonthly cap = (CapFloorInflationYearOnYearMonthly) CAPS_AVAILABLE[0][loopexp].getNthPayment(CAPS_AVAILABLE[0][loopexp].getNumberOfPayments() - 1);
      expiryTimes_AVAILABLE[loopexp] = cap.getReferenceEndTime();
    }
    final InflationYearOnYearCapFloorParameters parameters = new InflationYearOnYearCapFloorParameters(expiryTimes_AVAILABLE, strikes_AVAILABLE, volatilities_AVAILABLE, PRICE_INDEX_EUR);
    final SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective objective = new SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective(parameters, CUR);
    final SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine<InflationProviderDiscount> calibrationEngine = new SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationEngine<>(
        objective);

    for (int loop1 = 0; loop1 < strikes_AVAILABLE.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        for (int loop3 = 0; loop3 < availabelTenor[loop2]; loop3++) {
          marketPrices_AVAILABLE[loop1][loop2] = marketPrices_AVAILABLE[loop1][loop2] + METHOD.presentValue(CAPS_AVAILABLE[loop1][loop2].getNthPayment(loop3), BLACK_INFLATION).getAmount(CUR);
        }
      }
    }

    for (int loop1 = 0; loop1 < strikes.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {

        calibrationEngine.addInstrument(CAPS_AVAILABLE[loop1][loop2], marketPrices_AVAILABLE[loop1][loop2]);

      }
    }

    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      calibrationEngine.calibrate(MARKET.getInflationProvider());
    }
    endTime = System.currentTimeMillis();
    System.out.println("CapFloorYearOnYearInterpolationCalibrationObjectiveTest - " + nbTest + " volatility matrix construction year on year cap/floor: " + (endTime - startTime) + " ms");
    // Performance note:volatility matrix construction year on year cap/floor: 28-Aug-13: On Dell Precision T1850 3.5 GHz Quad-Core Intel Xeon: 4999 ms for 100 sets.
  }

}
