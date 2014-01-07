/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;


import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;

/**
 * The conventions for US FX.
 */
public class USFXConventions extends ConventionMasterInitializer {

  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new USFXConventions();

  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");

  /**
   * Restricted constructor.
   */
  protected USFXConventions() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(final ConventionMaster master) {
    addUsdCadFxSpotConvention(master);
    addUsdCadForwardConvention(master);
    addFxSpotConvention(master);
    addFxForwardConvention(master);
  }

  protected void addUsdCadFxSpotConvention(final ConventionMaster master) {
    final FXSpotConvention usdCadSpot = new FXSpotConvention(
        "USD/CAD FX Spot", ExternalIdBundle.of(ExternalId.of("CONVENTION", "USD/CAD FX Spot")), 1, US);
    addConvention(master, usdCadSpot);
  }

  protected void addUsdCadForwardConvention(final ConventionMaster master) {
    final FXForwardAndSwapConvention usdCadForward = new FXForwardAndSwapConvention(
        "USD/CAD FX Forward", ExternalIdBundle.of(ExternalId.of("CONVENTION", "USD/CAD FX Forward")),
        ExternalId.of("CONVENTION", "USD/CAD FX Spot"), FOLLOWING, false, US);
    addConvention(master, usdCadForward);
  }

  protected void addFxSpotConvention(final ConventionMaster master) {
    final FXSpotConvention fxSpot = new FXSpotConvention(
        "FX Spot", ExternalIdBundle.of(ExternalId.of("CONVENTION", "FX Spot")), 2, US);
    addConvention(master, fxSpot);
  }

  protected void addFxForwardConvention(final ConventionMaster master) {
    // TODO: Holiday should not be US only.
    final FXForwardAndSwapConvention fxForward = new FXForwardAndSwapConvention(
        "FX Forward", ExternalIdBundle.of(ExternalId.of("CONVENTION", "FX Forward")),
        ExternalId.of("CONVENTION", "FX Spot"), FOLLOWING, false, US);
    addConvention(master, fxForward);
  }

}
