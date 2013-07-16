/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.percurrency;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.DEPOSIT_ON;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.EURODOLLAR_FUTURE;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.INFLATION_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IRS_FIXED_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OIS_FIXED_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OIS_ON_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.PRICE_INDEX;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.IMMFutureAndFutureOptionQuarterlyExpiryCalculator;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InMemoryConventionMaster;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class USConventions {
  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final DayCount THIRTY_360 = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");
  /** OIS X-Ccy USD/JPY ON leg convention string **/
  public static final String OIS_USD_JPY_ON_LEG = "USD Overnight USD/JPY XCcy Leg";

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionMaster conventionMaster) {
    final String tenorString = "3M";
    final String depositConventionName = getConventionName(Currency.USD, DEPOSIT);
    final String depositONConventionName = getConventionName(Currency.USD, DEPOSIT_ON);
    final String overnightConventionName = getConventionName(Currency.USD, OVERNIGHT);
    final String liborConventionName = getConventionName(Currency.USD, LIBOR);
    final String eurodollarFutureConventionName = EURODOLLAR_FUTURE;
    final String inflationConventionName = getConventionName(Currency.USD, INFLATION_LEG);
    final String priceIndexName = getConventionName(Currency.USD, PRICE_INDEX);
    final ExternalId libor3mConventionId = ExternalId.of(SCHEME_NAME, liborConventionName);
    final ExternalId overnightConventionId = ExternalId.of(SCHEME_NAME, overnightConventionName);
    final ExternalId priceIndexId = ExternalId.of(SCHEME_NAME, priceIndexName);
    final Convention liborConvention = new IborIndexConvention(liborConventionName, getIds(Currency.USD, LIBOR), ACT_360, MODIFIED_FOLLOWING, 2, false, Currency.USD,
        LocalTime.of(11, 00), "US", NYLON, US, "");
    final Convention overnightConvention = new OvernightIndexConvention(overnightConventionName, getIds(Currency.USD, OVERNIGHT), ACT_360, 1, Currency.USD, US);
    // Deposit
    final DepositConvention depositConvention = new DepositConvention(depositConventionName, getIds(Currency.USD, DEPOSIT), ACT_360, MODIFIED_FOLLOWING, 2, true, Currency.USD, US);
    final DepositConvention depositONConvention = new DepositConvention(depositONConventionName, getIds(Currency.USD, DEPOSIT_ON), ACT_360, FOLLOWING, 0, false, Currency.USD, US);
    // OIS legs
    final String oisFixedLegConventionName = getConventionName(Currency.USD, OIS_FIXED_LEG);
    final String oisONLegConventionName = getConventionName(Currency.USD, OIS_ON_LEG);
    final Convention oisFixedLegConvention = new SwapFixedLegConvention(oisFixedLegConventionName, getIds(Currency.USD, OIS_FIXED_LEG),
        Tenor.ONE_YEAR, ACT_360, MODIFIED_FOLLOWING, Currency.USD, US, 2, true, StubType.SHORT_START, false);
    final Convention oisONLegConvention = new OISLegConvention(oisONLegConventionName, getIds(Currency.USD, OIS_ON_LEG), overnightConventionId,
        Tenor.ONE_YEAR, 2, MODIFIED_FOLLOWING, 2, false, StubType.NONE, false);
    // Ibor swap legs
    final String irsFixedLegConventionName = getConventionName(Currency.USD, IRS_FIXED_LEG);
    final String irsIborLegConventionName = getConventionName(Currency.USD, tenorString, IRS_IBOR_LEG);
    final Convention irsFixedLegConvention = new SwapFixedLegConvention(irsFixedLegConventionName, getIds(Currency.USD, IRS_FIXED_LEG),
        Tenor.SIX_MONTHS, THIRTY_360, MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, true, StubType.SHORT_START, false);
    final Convention irsIborLegConvention = new VanillaIborLegConvention(irsIborLegConventionName, getIds(Currency.USD, tenorString, IRS_IBOR_LEG),
        libor3mConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, true, StubType.NONE, false);
    // Futures
    final Convention edFutureConvention = new InterestRateFutureConvention(eurodollarFutureConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, EURODOLLAR_FUTURE)),
        ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME), US, libor3mConventionId);
    // Inflation
    final PriceIndexConvention priceIndexConvention = new PriceIndexConvention(priceIndexName, getIds(Currency.USD, PRICE_INDEX), Currency.USD, US,
        ExternalSchemes.bloombergTickerSecurityId("CPURNSA Index"));
    final Convention inflationConvention = new InflationLegConvention(inflationConventionName, getIds(Currency.USD, INFLATION_LEG), MODIFIED_FOLLOWING, ACT_360, false, 3, 2,
        priceIndexId);
    // X-Ccy OIS
    final Convention oisXCcyJPYLegConvention = new OISLegConvention(OIS_USD_JPY_ON_LEG, getIds(OIS_USD_JPY_ON_LEG), overnightConventionId,
        Tenor.THREE_MONTHS, 2, MODIFIED_FOLLOWING, 2, true, StubType.NONE, false);
    conventionMaster.add(oisXCcyJPYLegConvention);
    // Convention add
    conventionMaster.add(depositConvention);
    conventionMaster.add(depositONConvention);
    conventionMaster.add(liborConvention);
    conventionMaster.add(overnightConvention);
    conventionMaster.add(irsIborLegConvention);
    conventionMaster.add(oisONLegConvention);
    conventionMaster.add(irsFixedLegConvention);
    conventionMaster.add(oisFixedLegConvention);
    conventionMaster.add(edFutureConvention);
    conventionMaster.add(priceIndexConvention);
    conventionMaster.add(inflationConvention);
  }

}
