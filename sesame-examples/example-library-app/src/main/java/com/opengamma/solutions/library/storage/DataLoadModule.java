/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.storage;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Provide the DatabaseRestore utility to populateMulticurveData data in the various masters
 */
public class DataLoadModule extends AbstractModule {

  private final String _path;

  /**
   * Initialise the DataLoadModule
   * @param path the full path to the data resources
   */
  public DataLoadModule(String path) {
    _path = ArgumentChecker.notNull(path, "path");
  }

  @Provides
  @Singleton
  public DataLoader createDatabaseRestore(SecurityMaster securityMaster,
                                               ConfigMaster configMaster,
                                               HolidayMaster holidayMaster,
                                               MarketDataSnapshotMaster marketDataSnapshotMaster,
                                               ConventionMaster conventionMaster) {

    return new DataLoader(_path,
                          securityMaster,
                          configMaster,
                          holidayMaster,
                          marketDataSnapshotMaster,
                          conventionMaster);


  }

  @Override
  protected void configure() {
    //Nothing to do
  }
}
