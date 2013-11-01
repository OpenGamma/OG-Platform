/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.core.convention.Convention;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.percurrency.EUConventions;
import com.opengamma.financial.convention.percurrency.JPConventions;
import com.opengamma.financial.convention.percurrency.KRConventions;
import com.opengamma.financial.convention.percurrency.USConventions;
import com.opengamma.financial.convention.percurrency.ZAConventions;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionDocument;

/**
 * In-memory convention master.
 *
 * Versioning is *NOT* supported.
 * Attempting a version correction will throw an exception.
 * Only VersionCorrection.LATEST is supported for retrieval.
 */
public class InMemoryConventionMaster extends com.opengamma.master.convention.impl.InMemoryConventionMaster {

  /**
   * Initializes the conventions.
   */
  public InMemoryConventionMaster() {
    init();
  }

  /**
   * Initializes the convention master.
   */
  protected void init() {
    addFXConventions();
    EUConventions.addFixedIncomeInstrumentConventions(this);
    JPConventions.addFixedIncomeInstrumentConventions(this);
    USConventions.addFixedIncomeInstrumentConventions(this);
    ZAConventions.addFixedIncomeInstrumentConventions(this);
    KRConventions.addFixedIncomeInstrumentConventions(this);
  }

  private void addFXConventions() {
    final ExternalId us = ExternalSchemes.financialRegionId("US");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final FXSpotConvention usdCadSpot = new FXSpotConvention("USD/CAD FX Spot", ExternalIdBundle.of(ExternalId.of("CONVENTION", "USD/CAD FX Spot")), 1, us);
    final FXForwardAndSwapConvention usdCadForward = new FXForwardAndSwapConvention("USD/CAD FX Forward", ExternalIdBundle.of(ExternalId.of("CONVENTION", "USD/CAD FX Forward")),
        ExternalId.of("CONVENTION", "USD/CAD FX Spot"), following, false, us);
    final FXSpotConvention fxSpot = new FXSpotConvention("FX Spot", ExternalIdBundle.of(ExternalId.of("CONVENTION", "FX Spot")), 2, us);
    // TODO: Holiday should not be US only.
    final FXForwardAndSwapConvention fxForward = new FXForwardAndSwapConvention("FX Forward", ExternalIdBundle.of(ExternalId.of("CONVENTION", "FX Forward")),
        ExternalId.of("CONVENTION", "FX Spot"), following, false, us);
    add(usdCadSpot);
    add(usdCadForward);
    add(fxSpot);
    add(fxForward);
  }

  public void add(Convention convention) {
    add(new ConventionDocument((FinancialConvention) convention));
  }

}
