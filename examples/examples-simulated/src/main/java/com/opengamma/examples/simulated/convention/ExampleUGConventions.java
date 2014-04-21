/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.convention;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.initializer.ConventionMasterInitializer;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Ugandan bond convention.
 */
public class ExampleUGConventions extends ConventionMasterInitializer {
  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new ExampleUGConventions();
  /** The currency */
  private static final Currency CURRENCY = Currency.of("UGX");

  /**
   * Restricted constructor.
   */
  protected ExampleUGConventions() {
  }

  @Override
  public void init(final ConventionMaster master) {
    final ExternalId countryId = ExternalSchemes.countryRegionId(Country.of("UG"));
    final ExternalId currencyId = ExternalSchemes.currencyRegionId(CURRENCY);
    final ExternalIdBundle conventionIds = ExternalIdBundle.of(countryId, currencyId);
    final String bondConventionName = "Uganda Government Bond";
    final BondConvention bondConvention = new BondConvention(bondConventionName, conventionIds,
        0, 0, BusinessDayConventions.MODIFIED_FOLLOWING, false, true);

    addConvention(master, bondConvention);
  }
}
