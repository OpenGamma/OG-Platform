/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigSearchRequest;
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
import com.opengamma.id.IdentifierBundle;
import com.opengamma.timeseries.TimeSeriesMetaData;
import com.opengamma.timeseries.TimeSeriesMetaDataResolver;
import com.opengamma.timeseries.exchange.Exchange;
import com.opengamma.timeseries.exchange.ExchangeDataProvider;
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
  protected static final Map<String, String> NON_EXCHANGE_DATA_MAP = buildNonExchangeDataMap();

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
  
  protected static final Map<String, String> BLOOMBERG_EXCHANGE_CODE_WITH_CITY_MAP = buildBloombergExchangeCodeMap();
  
  /**
   * DataProvider Prefix for Exchange Traded Security
   */
  public static final String EXCH_PREFIX = "EXCH_";
  
  private final SecuritySource _secSource;
  private final ExchangeDataProvider _exchangeDataProvider;
  private final ConfigSource _configSource;
  private Map<String, TimeSeriesMetaDataDefinition> _timeSeriesDefinitionMap = new ConcurrentHashMap<String, TimeSeriesMetaDataDefinition>();
  
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
          final String securityType = security.getSecurityType();
          return getDefaultDataProvider(securityType);
        }

        @Override
        public String visitGovernmentBondSecurity(GovernmentBondSecurity security) {
          return null;
        }

        @Override
        public String visitMunicipalBondSecurity(MunicipalBondSecurity security) {
          return null;
        }
      }, new CashSecurityVisitor<String>() {

        @Override
        public String visitCashSecurity(CashSecurity security) {
          return null;
        }

      }, new EquitySecurityVisitor<String>() {

        @Override
        public String visitEquitySecurity(EquitySecurity security) {
          s_logger.debug("default sec exchange={}", security.getExchangeCode());
          return EXCH_PREFIX + security.getExchangeCode();
        }
      }, new FRASecurityVisitor<String>() {

        @Override
        public String visitFRASecurity(FRASecurity security) {
          return null;
        }
      }, new FutureSecurityVisitor<String>() {

        @Override
        public String visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
          return null;
        }

        @Override
        public String visitBondFutureSecurity(BondFutureSecurity security) {
          return null;
        }

        @Override
        public String visitEnergyFutureSecurity(EnergyFutureSecurity security) {
          return null;
        }

        @Override
        public String visitFXFutureSecurity(FXFutureSecurity security) {
          return null;
        }

        @Override
        public String visitIndexFutureSecurity(IndexFutureSecurity security) {
          return null;
        }

        @Override
        public String visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
          return null;
        }

        @Override
        public String visitMetalFutureSecurity(MetalFutureSecurity security) {
          return null;
        }

        @Override
        public String visitStockFutureSecurity(StockFutureSecurity security) {
          return null;
        }
      }, new OptionSecurityVisitor<String>() {

        @Override
        public String visitBondOptionSecurity(BondOptionSecurity security) {
          return null;
        }

        @Override
        public String visitEquityOptionSecurity(EquityOptionSecurity security) {
          return null;
        }

        @Override
        public String visitFutureOptionSecurity(FutureOptionSecurity security) {
          return null;
        }

        @Override
        public String visitFXOptionSecurity(FXOptionSecurity security) {
          return null;
        }

        @Override
        public String visitOptionOptionSecurity(OptionOptionSecurity security) {
          return null;
        }

        @Override
        public String visitSwapOptionSecurity(SwapOptionSecurity security) {
          return null;
        }
      }, new SwapSecurityVisitor<String>() {

        @Override
        public String visitForwardSwapSecurity(ForwardSwapSecurity security) {
          return null;
        }

        @Override
        public String visitSwapSecurity(SwapSecurity security) {
          return null;
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
    TimeSeriesMetaDataDefinition metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDefaultDataProvider();
  }

  private TimeSeriesMetaDataDefinition getMetaDataBySecurityType(String securityType) {
    //get latest config document
    TimeSeriesMetaDataDefinition definition = _timeSeriesDefinitionMap.get(securityType);
    if (definition == null) {
      ConfigSearchRequest request = new ConfigSearchRequest();
      request.setName(securityType);
      List<TimeSeriesMetaDataDefinition> searchResult = _configSource.search(TimeSeriesMetaDataDefinition.class, request);
      //should return the lastest configuration
      if (searchResult.isEmpty()) {
        s_logger.warn("Unable to look up config document for securityType {}", securityType);
        throw new OpenGammaRuntimeException("TimeSeriesMetaData configration error");
      }
      definition = searchResult.get(0);
      _timeSeriesDefinitionMap.put(securityType, definition);
    }
    return definition;
  }

  private String getDefaultDataField(String securityType) {
    TimeSeriesMetaDataDefinition metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDefaultDataField();
  }

  private String getDefaultDataSource(final String securityType) {
    TimeSeriesMetaDataDefinition metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDefaultDataSource();
  }
  
  private List<String> getAvailableDataSources(String securityType) {
    TimeSeriesMetaDataDefinition metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDataSources();
  }

  private List<String> getAvailableDataFields(String securityType) {
    TimeSeriesMetaDataDefinition metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDataFields();
  }
  
  private List<String> getAvailableDataProviders(String securityType) {
    TimeSeriesMetaDataDefinition metaDataDefinition = getMetaDataBySecurityType(securityType);
    return metaDataDefinition.getDataProviders();
  }

  @Override
  public Collection<TimeSeriesMetaData> getAvailableMetaData(IdentifierBundle identifierBundle) {
    List<TimeSeriesMetaData> result = new ArrayList<TimeSeriesMetaData>();
    final Security security = _secSource.getSecurity(identifierBundle);
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      final String securityType = finSec.getSecurityType();
      List<String> dataFields = getAvailableDataFields(securityType);
      List<String> dataSources = getAvailableDataSources(securityType);
      List<String> dataProviders = finSec.accept(new FinancialSecurityVisitorAdapter<List<String>>(new BondSecurityVisitor<List<String>>() {

        @Override
        public List<String> visitCorporateBondSecurity(CorporateBondSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitGovernmentBondSecurity(GovernmentBondSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitMunicipalBondSecurity(MunicipalBondSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }
      }, new CashSecurityVisitor<List<String>>() {

        @Override
        public List<String> visitCashSecurity(CashSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }
      }, new EquitySecurityVisitor<List<String>>() {

        @Override
        public List<String> visitEquitySecurity(EquitySecurity security) {
          List<String> result = Lists.newArrayList(EXCH_PREFIX + security.getExchangeCode());
          return result;
        }
      }, new FRASecurityVisitor<List<String>>() {

        @Override
        public List<String> visitFRASecurity(FRASecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }
      }, new FutureSecurityVisitor<List<String>>() {

        @Override
        public List<String> visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitBondFutureSecurity(BondFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitEnergyFutureSecurity(EnergyFutureSecurity security) {
          return null;
        }

        @Override
        public List<String> visitFXFutureSecurity(FXFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitIndexFutureSecurity(IndexFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitMetalFutureSecurity(MetalFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitStockFutureSecurity(StockFutureSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }
      }, new OptionSecurityVisitor<List<String>>() {

        @Override
        public List<String> visitBondOptionSecurity(BondOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitEquityOptionSecurity(EquityOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitFXOptionSecurity(FXOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitFutureOptionSecurity(FutureOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitOptionOptionSecurity(OptionOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitSwapOptionSecurity(SwapOptionSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }
      }, new SwapSecurityVisitor<List<String>>() {

        @Override
        public List<String> visitForwardSwapSecurity(ForwardSwapSecurity security) {
          final String securityType = security.getSecurityType();
          return getAvailableDataProviders(securityType);
        }

        @Override
        public List<String> visitSwapSecurity(SwapSecurity security) {
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

