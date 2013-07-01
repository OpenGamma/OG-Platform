/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.percurrency;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.EURODOLLAR_FUTURE;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.FIXED_SWAP_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OIS_SWAP_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.VANILLA_IBOR_LEG;
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
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
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
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final DayCount THIRTY_360 = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionMaster conventionMaster) {
    final String fixedSwapLegConventionName = getConventionName(Currency.USD, FIXED_SWAP_LEG);
    final String tenorString = "3M";
    final String depositConventionName = getConventionName(Currency.USD, DEPOSIT);
    final String overnightConventionName = getConventionName(Currency.USD, OVERNIGHT);
    final String liborConventionName = getConventionName(Currency.USD, LIBOR);
    final String vanillaIborLegConventionName = getConventionName(Currency.USD, tenorString, VANILLA_IBOR_LEG);
    final String oisLegConventionName = getConventionName(Currency.USD, OIS_SWAP_LEG);
    final String eurodollarFutureConventionName = EURODOLLAR_FUTURE;
    final ExternalId libor3mConventionId = ExternalId.of(SCHEME_NAME, liborConventionName);
    final ExternalId overnightConventionId = ExternalId.of(SCHEME_NAME, overnightConventionName);
    final DepositConvention depositConvention = new DepositConvention(depositConventionName, getIds(Currency.USD, DEPOSIT), ACT_360, MODIFIED_FOLLOWING, 0, false, Currency.USD, US);
    final Convention liborConvention = new IborIndexConvention(liborConventionName, getIds(Currency.USD, LIBOR), ACT_360, MODIFIED_FOLLOWING, 2, false, Currency.USD,
        LocalTime.of(11, 00), NYLON, US, "");
    final Convention overnightConvention = new OvernightIndexConvention(overnightConventionName, getIds(Currency.USD, OVERNIGHT), ACT_360, 1, Currency.USD, US);
    final Convention fixedLegConvention = new SwapFixedLegConvention(fixedSwapLegConventionName, getIds(Currency.USD, FIXED_SWAP_LEG),
        Tenor.THREE_MONTHS, THIRTY_360, MODIFIED_FOLLOWING, 2, false, Currency.USD, NYLON, StubType.NONE);
    final Convention vanillaIborLegConvention = new VanillaIborLegConvention(vanillaIborLegConventionName, getIds(Currency.USD, tenorString, VANILLA_IBOR_LEG),
        libor3mConventionId, true, StubType.NONE, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS);
    final Convention overnightLegConvention = new OISLegConvention(oisLegConventionName, getIds(Currency.USD, OIS_SWAP_LEG), overnightConventionId,
        Tenor.THREE_MONTHS, 1, 2, MODIFIED_FOLLOWING, false);
    final Convention edFutureConvention = new InterestRateFutureConvention(eurodollarFutureConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, EURODOLLAR_FUTURE)),
        ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME), US, libor3mConventionId);
    conventionMaster.add(depositConvention);
    conventionMaster.add(liborConvention);
    conventionMaster.add(overnightConvention);
    conventionMaster.add(vanillaIborLegConvention);
    conventionMaster.add(overnightLegConvention);
    conventionMaster.add(fixedLegConvention);
    conventionMaster.add(edFutureConvention);
  }

}
