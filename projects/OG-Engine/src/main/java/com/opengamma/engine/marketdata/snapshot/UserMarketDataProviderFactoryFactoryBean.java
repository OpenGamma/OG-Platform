/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link UserMarketDataProviderFactory}.
 */
public class UserMarketDataProviderFactoryFactoryBean extends SingletonFactoryBean<UserMarketDataProviderFactory> {

  private static final Logger s_logger = LoggerFactory.getLogger(UserMarketDataProviderFactoryFactoryBean.class);

  private MarketDataSnapshotSource _snapshotSource;

  /**
   * @deprecated see PLAT-1459
   */
  @Deprecated
  private MarketDataAvailabilityFilter _baseMarketDataAvailability;

  public MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }

  public void setSnapshotSource(final MarketDataSnapshotSource snapshotSource) {
    _snapshotSource = snapshotSource;
  }

  /**
   * @return the filter
   * @deprecated see PLAT-1459
   */
  @Deprecated
  public MarketDataAvailabilityFilter getBaseMarketDataAvailability() {
    s_logger.error("Call to deprecated method 'getBaseMarketDataAvailability' (PLAT-1459)");
    return _baseMarketDataAvailability;
  }

  /**
   * @param baseMarketDataAvailability  the filter
   * @deprecated see PLAT-1459
   */
  @Deprecated
  public void setBaseMarketDataAvailability(final MarketDataAvailabilityFilter baseMarketDataAvailability) {
    s_logger.error("Call to deprecated method 'setBaseMarketDataAvailability' (PLAT-1459)");
    _baseMarketDataAvailability = baseMarketDataAvailability;
  }

  @Override
  protected UserMarketDataProviderFactory createObject() {
    ArgumentChecker.notNullInjected(getSnapshotSource(), "snapshotSource");
    return new UserMarketDataProviderFactory(getSnapshotSource());
  }

}
