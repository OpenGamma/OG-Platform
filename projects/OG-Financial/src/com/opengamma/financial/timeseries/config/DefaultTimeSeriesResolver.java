/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BondSecurityVisitor;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cash.CashSecurityVisitor;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquitySecurityVisitor;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.FRASecurityVisitor;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.BondOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FutureOptionSecurity;
import com.opengamma.financial.security.option.OptionOptionSecurity;
import com.opengamma.financial.security.option.OptionSecurityVisitor;
import com.opengamma.financial.security.option.SwapOptionSecurity;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurityVisitor;
import com.opengamma.financial.timeseries.TimeSeriesMetaData;
import com.opengamma.financial.timeseries.TimeSeriesMetaDataResolver;
import com.opengamma.financial.timeseries.exchange.Exchange;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A configurable implementation of TimeSeriesMetaDataResolver
 * <p>
 * Resolves the timeseries metadata based on security type
 */
public class DefaultTimeSeriesResolver implements TimeSeriesMetaDataResolver {
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultTimeSeriesResolver.class);
  private static final String[][] NON_EXCHANGE_DATA_PROVIDER_ARRAY = new String[][] {
    {"CMPL", "LONDON_CLOSE"}, {"CMPT", "TOKYO_CLOSE"}, {"CMPN", "NEWYORK_CLOSE"}
  };
  private static final Map<String, String> NON_EXCHANGE_DATA_MAP = buildNonExchangeDataMap();

  private static final String[][] BLOOMBERG_EXCHANGE_CODE_WITH_CITY = new String[][] {
    {"UA", "NEWYORK_CLOSE"},
    {"UN", "NEWYORK_CLOSE"},
    {"UW", "NEWYORK_CLOSE"},
    {"UQ", "NEWYORK_CLOSE"},
    {"UR", "NEWYORK_CLOSE"},
    {"UT", "NEWYORK_CLOSE"},
    {"UV", "NEWYORK_CLOSE"},
    {"UD", "WASHINGTON_CLOSE"},
    {"UB", "BOSTON_CLOSE"},
    {"UM", "CHICAGO_CLOSE"},
    {"UC", "CHICAGO_CLOSE"},
    {"UP", "SANFRANCISCO_CLOSE"},
    {"UX", "PHILADELPHIA_CLOSE"},
    {"UO", "CHICAGO_CLOSE"},
    {"UF", "KANSAS_CLOSE"},
    {"PK", "NEWYORK_CLOSE"},
    {"LN", "LONDON_CLOSE"},
      // Review 2010-10-06 Andrew -- Are these LONODNs mis-spelt? If correct, can we have a comment or reference to a doc so no-one makes the same mistake I have of thinking they need changing!
    {"LI", "LONODN_CLOSE"},
    {"PZ", "LONODN_CLOSE"},
    {"TQ", "LONODN_CLOSE"},
    {"NQ", "LONDON_CLOSE"},
    {"BQ", "LONODN_CLOSE"},
    {"EB", "LONODN_CLOSE"},
    {"PQ", "LONODN_CLOSE"},
    {"NR", "LONODN_CLOSE"},
    {"PX", "LISBOA_CLOSE"},
    {"BY", "STOCKHOLM_CLOSE"},
  };
  
  private static final Map<String, String> BLOOMBERG_EXCHANGE_CODE_WITH_CITY_MAP = buildBloombergExchangeCodeMap();
  
  /**
   * DataProvider Prefix for Exchange Traded Security
   */
  public static final String EXCH_PREFIX = "EXCH_";
  
  private final SecuritySource _secSource;
  private final ExchangeDataProvider _exchangeDataProvider;
  private final ConfigSource _configSource;
  private Map<String, TimeSeriesMetaDataConfiguration> _timeSeriesDefinitionMap = new ConcurrentHashMap<String, TimeSeriesMetaDataConfiguration>();
  
  public DefaultTimeSeriesResolver(SecuritySource secSource, ExchangeDataProvider exchangeDataProvider, ConfigSource configSoure) {
    ArgumentChecker.notNull(secSource, "security source");
    ArgumentChecker.notNull(exchangeDataProvider, "exchangeDataProvider");
    ArgumentChecker.notNull(configSoure, "configSoure");
    _secSource = secSource;
    _exchangeDataProvider =  exchangeDataProvider;
    _configSource = configSoure;
  }

  @Override
  public TimeSeriesMetaData getDefaultMetaData(final IdentifierBundle identifiers) {
    TimeSeriesMetaData result = new TimeSeriesMetaData();
    final Security security = _secSource.getSecurity(identifiers);
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      final String securityType = finSec.getSecurityType();
      result = new TimeSeriesMetaData();
      result.setDataField(getDefaultDataField(securityType));
      String dataProvider = finSec.accept(new FinancialSecurityVisitorAdapter<String>(new BondSecurityVisitor<String>() {
        @Override
        public String visitCorporateBondSecurity(CorporateBondSecurity security) {
          return getDefaultDataProvider(security.getSecurityType());
        }

        @Override
        public String visitGovernmentBondSecurity(GovernmentBondSecurity security) {
          return getDefaultDataProvider(security.getSecurityType());
        }

        @Override
        public String visitMunicipalBondSecurity(MunicipalBondSecurity security) {
          return getDefaultDataProvider(security.getSecurityType());
        }
      }, new CashSecurityVisitor<String>() {

        @Override
        public String visitCashSecurity(CashSecurity security) {
          return getDefaultDataProvider(security.getSecurityType());
        }

      }, new EquitySecurityVisitor<String>() {

        @Override
        public String visitEquitySecurity(EquitySecurity security) {
          return EXCH_PREFIX + security.getExchangeCode();
        }
      }, new FRASecurityVisitor<String>() {

        @Override
        public String visitFRASecurity(FRASecurity security) {
          return getDefaultDataProvider(security.getSecurityType());
        }
      }, new FutureSecurityVisitor<String>() {

        @Override
        public String visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
          return EXCH_PREFIX + security.getTradingExchange();
        }

        @Override
        public String visitBondFutureSecurity(BondFutureSecurity security) {
          return EXCH_PREFIX + security.getTradingExchange();
        }

        @Override
        public String visitEnergyFutureSecurity(EnergyFutureSecurity security) {
          return EXCH_PREFIX + security.getTradingExchange();
        }

        @Override
        public String visitFXFutureSecurity(FXFutureSecurity security) {
          return EXCH_PREFIX + security.getTradingExchange();
        }

        @Override
        public String visitIndexFutureSecurity(IndexFutureSecurity security) {
          return EXCH_PREFIX + security.getTradingExchange();
        }

        @Override
        public String visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
          return EXCH_PREFIX + security.getTradingExchange();
        }

        @Override
        public String visitMetalFutureSecurity(MetalFutureSecurity security) {
          return EXCH_PREFIX + security.getTradingExchange();
        }

        @Override
        public String visitStockFutureSecurity(StockFutureSecurity security) {
          return EXCH_PREFIX + security.getTradingExchange();
        }
      }, new OptionSecurityVisitor<String>() {

        @Override
        public String visitBondOptionSecurity(BondOptionSecurity security) {
          return getDefaultDataProvider(security.getSecurityType());
        }

        @Override
        public String visitEquityOptionSecurity(EquityOptionSecurity security) {
          return EXCH_PREFIX + security.getExchange();
        }

        @Override
        public String visitFutureOptionSecurity(FutureOptionSecurity security) {
          return EXCH_PREFIX + security.getExchange();
        }

        @Override
        public String visitFXOptionSecurity(FXOptionSecurity security) {
          return getDefaultDataProvider(security.getSecurityType());
        }

        @Override
        public String visitOptionOptionSecurity(OptionOptionSecurity security) {
          return getDefaultDataProvider(security.getSecurityType());
        }

        @Override
        public String visitSwapOptionSecurity(SwapOptionSecurity security) {
          return getDefaultDataProvider(security.getSecurityType());
        }
      }, new SwapSecurityVisitor<String>() {

        @Override
        public String visitForwardSwapSecurity(ForwardSwapSecurity security) {
          return getDefaultDataProvider(security.getSecurityType());
        }

        @Override
        public String visitSwapSecurity(SwapSecurity security) {
          return getDefaultDataProvider(security.getSecurityType());
        }
      }));
      result.setDataProvider(dataProvider);
      result.setDataSource(getDefaultDataSource(securityType));
      result.setIdentifiers(identifiers);
      String defaultObservationTime = null;
      if (dataProvider.startsWith(EXCH_PREFIX)) {
        String exchangeCode = dataProvider.substring(EXCH_PREFIX.length());
        if (exchangeCode != null) {
          Exchange exchange = _exchangeDataProvider.getExchange(exchangeCode);
          if (exchange != null) {
            String city = exchange.getCity();
            if (city != null) {
              defaultObservationTime = StringUtils.remove(exchange.getCity().trim(), " ") + "_CLOSE";
            }
          } else {
            defaultObservationTime = BLOOMBERG_EXCHANGE_CODE_WITH_CITY_MAP.get(dataProvider);
          }
        } 
        if (defaultObservationTime == null) {
          s_logger.warn("default observation time can not be null for {}", identifiers);
          throw new  OpenGammaRuntimeException("TimeSeries configuration error for " + identifiers);
        }
      } else {
        defaultObservationTime = NON_EXCHANGE_DATA_MAP.get(dataProvider);
        if (defaultObservationTime == null) {
          s_logger.warn("Default observation can not be null for {}", identifiers);
          throw new  OpenGammaRuntimeException("TimeSeries configuration error for " + identifiers);
        }
      }
      result.setObservationTime(defaultObservationTime);
    } else {
      s_logger.warn("{} not a financial security", identifiers);
      throw new OpenGammaRuntimeException(identifiers + " not a financial security");
    }
    return result;
  }
 
  private String getDefaultDataProvider(String securityType) {
    TimeSeriesMetaDataConfiguration metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDefaultDataProvider();
  }

  private TimeSeriesMetaDataConfiguration getMetaDataBySecurityType(String securityType) {
    //get latest config document
    TimeSeriesMetaDataConfiguration definition = _timeSeriesDefinitionMap.get(securityType);
    if (definition == null) {
      definition = _configSource.getLatestByName(TimeSeriesMetaDataConfiguration.class, securityType);
      if (definition == null) {
        s_logger.warn("Unable to look up config document for securityType {}", securityType);
        throw new OpenGammaRuntimeException("TimeSeriesMetaData configration error");
      }
      _timeSeriesDefinitionMap.put(securityType, definition);
    }
    return definition;
  }

  private String getDefaultDataField(String securityType) {
    TimeSeriesMetaDataConfiguration metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDefaultDataField();
  }

  private String getDefaultDataSource(final String securityType) {
    TimeSeriesMetaDataConfiguration metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDefaultDataSource();
  }
  
  private Set<String> getAvailableDataSources(String securityType) {
    TimeSeriesMetaDataConfiguration metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDataSources();
  }

  private Set<String> getAvailableDataFields(String securityType) {
    TimeSeriesMetaDataConfiguration metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDataFields();
  }
  
  private Set<String> getAvailableDataProviders(String securityType) {
    TimeSeriesMetaDataConfiguration metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDataProviders();
  }

  @Override
  public Collection<TimeSeriesMetaData> getAvailableMetaData(IdentifierBundle identifierBundle) {
    List<TimeSeriesMetaData> result = new ArrayList<TimeSeriesMetaData>();
    final Security security = _secSource.getSecurity(identifierBundle);
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      final String securityType = finSec.getSecurityType();
      Set<String> dataFields = getAvailableDataFields(securityType);
      Set<String> dataSources = getAvailableDataSources(securityType);
      Set<String> dataProviders = finSec.accept(new FinancialSecurityVisitorAdapter<Set<String>>(new BondSecurityVisitor<Set<String>>() {

        @Override
        public Set<String> visitCorporateBondSecurity(CorporateBondSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitGovernmentBondSecurity(GovernmentBondSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitMunicipalBondSecurity(MunicipalBondSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }
      }, new CashSecurityVisitor<Set<String>>() {

        @Override
        public Set<String> visitCashSecurity(CashSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }
      }, new EquitySecurityVisitor<Set<String>>() {

        @Override
        public Set<String> visitEquitySecurity(EquitySecurity security) {
          Set<String> result = Sets.newHashSet(EXCH_PREFIX + security.getExchangeCode());
          return result;
        }
      }, new FRASecurityVisitor<Set<String>>() {

        @Override
        public Set<String> visitFRASecurity(FRASecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }
      }, new FutureSecurityVisitor<Set<String>>() {

        @Override
        public Set<String> visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitBondFutureSecurity(BondFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitEnergyFutureSecurity(EnergyFutureSecurity security) {
          return null;
        }

        @Override
        public Set<String> visitFXFutureSecurity(FXFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitIndexFutureSecurity(IndexFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitMetalFutureSecurity(MetalFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitStockFutureSecurity(StockFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }
      }, new OptionSecurityVisitor<Set<String>>() {

        @Override
        public Set<String> visitBondOptionSecurity(BondOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitEquityOptionSecurity(EquityOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitFXOptionSecurity(FXOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitFutureOptionSecurity(FutureOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitOptionOptionSecurity(OptionOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitSwapOptionSecurity(SwapOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }
      }, new SwapSecurityVisitor<Set<String>>() {

        @Override
        public Set<String> visitForwardSwapSecurity(ForwardSwapSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public Set<String> visitSwapSecurity(SwapSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }
      }));
      for (String dataSource : dataSources) {
        for (String dataField : dataFields) {
          for (String dataProvider : dataProviders) {
            TimeSeriesMetaData timeSeriesMetaData = new TimeSeriesMetaData();
            timeSeriesMetaData.setDataSource(dataSource);
            timeSeriesMetaData.setDataField(dataField);
            timeSeriesMetaData.setDataProvider(dataProvider);
            String defaultObservationTime = null;
            if (dataProvider.startsWith(EXCH_PREFIX)) {
              String exchangeCode = dataProvider.substring(EXCH_PREFIX.length());
              if (exchangeCode != null) {
                Exchange exchange = _exchangeDataProvider.getExchange(exchangeCode);
                if (exchange != null) {
                  String city = exchange.getCity();
                  if (city != null) {
                    defaultObservationTime = StringUtils.remove(exchange.getCity().trim(), " ") + "_CLOSE";
                  }
                } else {
                  defaultObservationTime = BLOOMBERG_EXCHANGE_CODE_WITH_CITY_MAP.get(dataProvider);
                }
              } 
              if (defaultObservationTime == null) {
                s_logger.warn("default observation time can not be null for {}", identifierBundle);
                throw new  OpenGammaRuntimeException("TimeSeries configuration error for " + identifierBundle);
              }
            } else {
              defaultObservationTime = NON_EXCHANGE_DATA_MAP.get(dataProvider);
              if (defaultObservationTime == null) {
                s_logger.warn("Default observation can not be null for {}", identifierBundle);
                throw new  OpenGammaRuntimeException("TimeSeries configuration error for " + identifierBundle);
              }
            }
            timeSeriesMetaData.setObservationTime(defaultObservationTime);
            result.add(timeSeriesMetaData);
          }
        }
      }
      
    } else {
      s_logger.warn("{} not a financial security", identifierBundle);
      throw new OpenGammaRuntimeException(identifierBundle + " not a financial security");
    }
    
    return Collections.unmodifiableCollection(result);
  }
  
  /**
   * @return
   */
  @SuppressWarnings("unchecked")
  private static Map<String, String> buildBloombergExchangeCodeMap() {
    return ArrayUtils.toMap(BLOOMBERG_EXCHANGE_CODE_WITH_CITY);
  }
  
  @SuppressWarnings("unchecked")
  private static Map<String, String> buildNonExchangeDataMap() {
    return ArrayUtils.toMap(NON_EXCHANGE_DATA_PROVIDER_ARRAY);
  }
  
}

