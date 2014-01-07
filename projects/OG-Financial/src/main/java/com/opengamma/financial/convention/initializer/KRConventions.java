/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;

import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getIds;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.money.Currency;

/**
 * The conventions for South Korea.
 */
public class KRConventions extends ConventionMasterInitializer {

  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new KRConventions();

  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  private static final ExternalId KR = ExternalSchemes.financialRegionId("KR");

  /**
   * Restricted constructor.
   */
  protected KRConventions() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(final ConventionMaster master) {
    addDepositConvention(master);
  }

  protected void addDepositConvention(final ConventionMaster master) {
    final String depositConventionName = getConventionName(Currency.of("KRW"), DEPOSIT);
    final DepositConvention depositConvention = new DepositConvention(
        depositConventionName, getIds(Currency.of("KRW"), DEPOSIT), ACT_360, FOLLOWING, 2, false, Currency.of("KRW"), KR);
    addConvention(master, depositConvention);
  }

}
