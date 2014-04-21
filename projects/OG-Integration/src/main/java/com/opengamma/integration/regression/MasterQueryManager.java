/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.List;

import com.google.common.base.Function;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;

/**
 * Helper class which holds a set of queries for use in the {@link DatabaseDump} utility.
 */
final class MasterQueryManager {

  private final Function<SecurityMaster, ? extends Iterable<SecurityDocument>> _securityQuery;
  private final Function<PositionMaster, ? extends Iterable<PositionDocument>> _positionQuery;
  private final Function<PortfolioMaster, ? extends Iterable<PortfolioDocument>> _portfolioQuery;
  private final Function<ConfigMaster, ? extends Iterable<ConfigDocument>> _configQuery;
  private final Function<HistoricalTimeSeriesMaster, ? extends Iterable<HistoricalTimeSeriesInfoDocument>> _htsQuery;
  private final Function<HolidayMaster, ? extends Iterable<HolidayDocument>> _holidayQuery;
  private final Function<ExchangeMaster, ? extends Iterable<ExchangeDocument>> _exchangeQuery;
  private final Function<MarketDataSnapshotMaster, ? extends Iterable<MarketDataSnapshotDocument>> _marketDataSnapshotQuery;
  private final Function<LegalEntityMaster, ? extends Iterable<LegalEntityDocument>> _legalEntityQuery;
  private final Function<ConventionMaster, ? extends Iterable<ConventionDocument>> _conventionQuery;

  public MasterQueryManager(Function<SecurityMaster, ? extends Iterable<SecurityDocument>> securityQuery, Function<PositionMaster, ? extends Iterable<PositionDocument>> positionQuery,
      Function<PortfolioMaster, ? extends Iterable<PortfolioDocument>> portfolioQuery, Function<ConfigMaster, ? extends Iterable<ConfigDocument>> configQuery,
      Function<HistoricalTimeSeriesMaster, ? extends Iterable<HistoricalTimeSeriesInfoDocument>> htsQuery, Function<HolidayMaster, ? extends Iterable<HolidayDocument>> holidayQuery,
      Function<ExchangeMaster, ? extends Iterable<ExchangeDocument>> exchangeQuery, Function<MarketDataSnapshotMaster, ? extends Iterable<MarketDataSnapshotDocument>> marketDataSnapshotQuery,
      Function<LegalEntityMaster, ? extends Iterable<LegalEntityDocument>> legalEntityQuery,
      Function<ConventionMaster, ? extends Iterable<ConventionDocument>> conventionQuery) {
    super();
    _securityQuery = securityQuery;
    _positionQuery = positionQuery;
    _portfolioQuery = portfolioQuery;
    _configQuery = configQuery;
    _htsQuery = htsQuery;
    _holidayQuery = holidayQuery;
    _exchangeQuery = exchangeQuery;
    _marketDataSnapshotQuery = marketDataSnapshotQuery;
    _legalEntityQuery = legalEntityQuery;
    _conventionQuery = conventionQuery;
  }

  public Function<SecurityMaster, ? extends Iterable<SecurityDocument>> getSecurityQuery() {
    return _securityQuery;
  }

  public Function<PositionMaster, ? extends Iterable<PositionDocument>> getPositionQuery() {
    return _positionQuery;
  }

  public Function<PortfolioMaster, ? extends Iterable<PortfolioDocument>> getPortfolioQuery() {
    return _portfolioQuery;
  }

  public Function<ConfigMaster, ? extends Iterable<ConfigDocument>> getConfigQuery() {
    return _configQuery;
  }

  public Function<HistoricalTimeSeriesMaster, ? extends Iterable<HistoricalTimeSeriesInfoDocument>> getHtsQuery() {
    return _htsQuery;
  }

  public Function<HolidayMaster, ? extends Iterable<HolidayDocument>> getHolidayQuery() {
    return _holidayQuery;
  }

  public Function<ExchangeMaster, ? extends Iterable<ExchangeDocument>> getExchangeQuery() {
    return _exchangeQuery;
  }

  public Function<MarketDataSnapshotMaster, ? extends Iterable<MarketDataSnapshotDocument>> getMarketDataSnapshotQuery() {
    return _marketDataSnapshotQuery;
  }

  public Function<LegalEntityMaster, ? extends Iterable<LegalEntityDocument>> getLegalEntityQuery() {
    return _legalEntityQuery;
  }

  public Function<ConventionMaster, ? extends Iterable<ConventionDocument>> getConventionQuery() {
    return _conventionQuery;
  }

  public static MasterQueryManager queryAll() {
    return new MasterQueryManager(new SecurityQueryAll(), 
                                  new PositionQueryAll(), 
                                  new PortfolioQueryAll(), 
                                  new ConfigQueryAll(),
                                  new HtsQueryAll(),
                                  new HolidayQueryAll(),
                                  new ExchangeQueryAll(),
                                  new MarketDataSnapshotQueryAll(),
                                  new OrgQueryAll(),
                                  new ConventionQueryAll());
  }
  
  //no getAll() on AbstractMaster so have to write out for each one:
  
  private static class SecurityQueryAll implements Function<SecurityMaster, List<SecurityDocument>> {

    @Override
    public List<SecurityDocument> apply(SecurityMaster input) {
      return input.search(new SecuritySearchRequest()).getDocuments();
    }
    
  }

  private static class PositionQueryAll implements Function<PositionMaster, List<PositionDocument>> {

    @Override
    public List<PositionDocument> apply(PositionMaster input) {
      return input.search(new PositionSearchRequest()).getDocuments();
    }

  }

  private static class PortfolioQueryAll implements Function<PortfolioMaster, List<PortfolioDocument>> {

    @Override
    public List<PortfolioDocument> apply(PortfolioMaster input) {
      return input.search(new PortfolioSearchRequest()).getDocuments();
    }

  }

  private static class ConfigQueryAll implements Function<ConfigMaster, List<ConfigDocument>> {

    @Override
    public List<ConfigDocument> apply(ConfigMaster input) {
      return input.search(new ConfigSearchRequest<>()).getDocuments();
    }

  }

  private static class HtsQueryAll implements Function<HistoricalTimeSeriesMaster, List<HistoricalTimeSeriesInfoDocument>> {

    @Override
    public List<HistoricalTimeSeriesInfoDocument> apply(HistoricalTimeSeriesMaster input) {
      return input.search(new HistoricalTimeSeriesInfoSearchRequest()).getDocuments();
    }

  }

  private static class HolidayQueryAll implements Function<HolidayMaster, List<HolidayDocument>> {

    @Override
    public List<HolidayDocument> apply(HolidayMaster input) {
      return input.search(new HolidaySearchRequest()).getDocuments();
    }

  }

  private static class ExchangeQueryAll implements Function<ExchangeMaster, List<ExchangeDocument>> {

    @Override
    public List<ExchangeDocument> apply(ExchangeMaster input) {
      return input.search(new ExchangeSearchRequest()).getDocuments();
    }

    
  }

  private static class MarketDataSnapshotQueryAll implements Function<MarketDataSnapshotMaster, List<MarketDataSnapshotDocument>> {

    @Override
    public List<MarketDataSnapshotDocument> apply(MarketDataSnapshotMaster input) {
      return input.search(new MarketDataSnapshotSearchRequest()).getDocuments();
    }

    
  }

  private static class OrgQueryAll implements Function<LegalEntityMaster, List<LegalEntityDocument>> {

    @Override
    public List<LegalEntityDocument> apply(LegalEntityMaster input) {
      return input.search(new LegalEntitySearchRequest()).getDocuments();
    }

    
  }
  
  private static class ConventionQueryAll implements Function<ConventionMaster, List<ConventionDocument>> {

    @Override
    public List<ConventionDocument> apply(ConventionMaster input) {
      return input.search(new ConventionSearchRequest()).getDocuments();
    }

    
  }
  
  

}
