/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.SecurityDocument;

/**
 * Helper class which holds a set of filters for use in the {@link DatabaseDump} utility.
 */
final class MasterFilterManager {

  private final Predicate<? super SecurityDocument> _securityFilter;
  private final Predicate<? super PositionDocument> _positionFilter;
  private final Predicate<? super PortfolioDocument> _portfolioFilter;
  private final Predicate<? super ConfigDocument> _configFilter;
  private final Predicate<? super HistoricalTimeSeriesInfoDocument> _htsFilter;
  private final Predicate<? super HolidayDocument> _holidayFilter;
  private final Predicate<? super ExchangeDocument> _exchangeFilter;
  private final Predicate<? super MarketDataSnapshotDocument> _marketDataSnapshotFilter;
  private final Predicate<? super OrganizationDocument> _organizationFilter;
  
  

  public MasterFilterManager(Predicate<? super SecurityDocument> securityFilter, Predicate<? super PositionDocument> positionFilter, Predicate<? super PortfolioDocument> portfolioFilter,
      Predicate<? super ConfigDocument> configFilter, Predicate<? super HistoricalTimeSeriesInfoDocument> htsFilter, Predicate<? super HolidayDocument> holidayFilter,
      Predicate<? super ExchangeDocument> exchangeFilter, Predicate<? super MarketDataSnapshotDocument> marketDataSnapshotFilter, Predicate<? super OrganizationDocument> organizationFilter) {
    _securityFilter = securityFilter;
    _positionFilter = positionFilter;
    _portfolioFilter = portfolioFilter;
    _configFilter = configFilter;
    _htsFilter = htsFilter;
    _holidayFilter = holidayFilter;
    _exchangeFilter = exchangeFilter;
    _marketDataSnapshotFilter = marketDataSnapshotFilter;
    _organizationFilter = organizationFilter;
  }



  public Predicate<? super SecurityDocument> getSecurityFilter() {
    return _securityFilter;
  }



  public Predicate<? super PositionDocument> getPositionFilter() {
    return _positionFilter;
  }



  public Predicate<? super PortfolioDocument> getPortfolioFilter() {
    return _portfolioFilter;
  }



  public Predicate<? super ConfigDocument> getConfigFilter() {
    return _configFilter;
  }



  public Predicate<? super HistoricalTimeSeriesInfoDocument> getHtsFilter() {
    return _htsFilter;
  }



  public Predicate<? super HolidayDocument> getHolidayFilter() {
    return _holidayFilter;
  }



  public Predicate<? super ExchangeDocument> getExchangeFilter() {
    return _exchangeFilter;
  }



  public Predicate<? super MarketDataSnapshotDocument> getMarketDataSnapshotFilter() {
    return _marketDataSnapshotFilter;
  }



  public Predicate<? super OrganizationDocument> getOrganizationFilter() {
    return _organizationFilter;
  }



  public static MasterFilterManager alwaysTrue() {
    Predicate<Object> alwaysTrue = Predicates.alwaysTrue();
    return new MasterFilterManager(alwaysTrue, alwaysTrue, alwaysTrue, alwaysTrue, alwaysTrue, alwaysTrue, alwaysTrue, alwaysTrue, alwaysTrue);
  }
  
  
}
