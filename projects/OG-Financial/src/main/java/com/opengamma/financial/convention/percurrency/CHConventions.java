/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.percurrency;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InMemoryConventionMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * The conventions for CHF.
 * FIXME: This is a temporary in-code convention master. This should be moved to database before going to production.
 */
public class CHConventions {
  
  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final ExternalId CH = ExternalSchemes.financialRegionId("CH");
  private static final ExternalId CHGB = ExternalSchemes.financialRegionId("CH+GB");

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionMaster conventionMaster) {
    final String liborConventionName = getConventionName(Currency.CHF, LIBOR);
    final Convention liborIndex = new IborIndexConvention(liborConventionName, getIds(Currency.CHF, LIBOR), ACT_360, MODIFIED_FOLLOWING, 2, true, Currency.CHF,
        LocalTime.of(11, 00), "CH", CHGB, CH, "");
    
    // Convention add
    conventionMaster.add(liborIndex);
  }

}
