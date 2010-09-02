/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.config;


import static com.opengamma.timeseries.config.TimeseriesMasterTestUtils.makeExpectedAAPLEquitySecurity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.config.db.MongoDBConfigMaster;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.config.MongoDBMasterConfigSource;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.MockSecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.SecurityTestCase;
import com.opengamma.financial.security.SecurityTestCaseMethods;
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
import com.opengamma.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.test.MongoDBTestUtils;

/**
 * 
 */
public class DefaultTimeSeriesResolverTest implements SecurityTestCaseMethods {
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultTimeSeriesResolverTest.class);
  private TimeSeriesMetaDataResolver _metaDataResolver;
  private MockSecuritySource _mockSecuritySource;
  private TimeSeriesMetaDataTestCase _testCase;
  private MongoDBConnectionSettings _mongoSettings;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    //use className as collection so do not set collectionName
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings(null, false);
    _mongoSettings = settings; 
    MockSecuritySource mockSecuritySource = new MockSecuritySource();
    _mockSecuritySource = mockSecuritySource;
    ExchangeDataProvider exchangeDataProvider = new DefaultExchangeDataProvider();
    ConfigSource configsource = setUpConfigSource();
    DefaultTimeSeriesResolver defaultResolver = new DefaultTimeSeriesResolver(_mockSecuritySource, exchangeDataProvider, configsource);
    _metaDataResolver = defaultResolver;
    _testCase = new TimeSeriesMetaDataTestCase();
  }

  /**
   * @return
   */
  private ConfigSource setUpConfigSource() {
    MongoDBConfigMaster<TimeSeriesMetaDataDefinition> tsMetaDataConfigMaster = new MongoDBConfigMaster<TimeSeriesMetaDataDefinition>(TimeSeriesMetaDataDefinition.class, _mongoSettings);
    //add tsmetadata configuration
    DefaultConfigDocument<TimeSeriesMetaDataDefinition> doc = new DefaultConfigDocument<TimeSeriesMetaDataDefinition>();
    //set up config for equity security
    TimeSeriesMetaDataDefinition definition = new TimeSeriesMetaDataDefinition("EQUITY", "BLOOMBERG", "PX_LAST", "EXCH");
    definition.addDataSources("BLOOMBERG");
    definition.addDataSources("REUTERS");
    definition.addDataFields("PX_LAST");
    definition.addDataFields("VOLUME");
    doc.setName("EQUITY");
    doc.setValue(definition);
    tsMetaDataConfigMaster.add(doc);
    //set up config for bond security
    doc = new DefaultConfigDocument<TimeSeriesMetaDataDefinition>();
    definition = new TimeSeriesMetaDataDefinition("BOND", "BLOOMBERG", "PX_LAST", "CMPL");
    definition.addDataSources("BLOOMBERG");
    definition.addDataSources("REUTERS");
    definition.addDataFields("PX_LAST");
    definition.addDataFields("VOLUME");
    definition.addDataProviders("CMPL");
    definition.addDataProviders("CMPN");
    definition.addDataProviders("CMPT");
    doc.setName("BOND");
    doc.setValue(definition);
    tsMetaDataConfigMaster.add(doc);
    
    MongoDBMasterConfigSource mongoDBMasterConfigSource = new MongoDBMasterConfigSource();
    mongoDBMasterConfigSource.addConfigMaster(TimeSeriesMetaDataDefinition.class, tsMetaDataConfigMaster);
    return mongoDBMasterConfigSource;
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    _metaDataResolver = null;
    _mockSecuritySource = null;
    _testCase = null;
    deleteConfigCollections();
  }
  
  private void deleteConfigCollections() throws Exception {
    Mongo mongo = new Mongo(_mongoSettings.getHost(), _mongoSettings.getPort());
    DB db = mongo.getDB(_mongoSettings.getDatabase());
    String collectionName =  TimeSeriesMetaDataDefinition.class.getSimpleName();
    DBCollection collection = db.getCollection(collectionName);
    collection.drop();
  }

  @Override
  @Test
  public void testAgricultureFutureSecurity() {
  }

  @Override
  @Test
  public void testBondFutureSecurity() {
  }

  @Override
  @Test
  public void testBondOptionSecurity() {
  }

  @Override
  @Test
  public void testCashSecurity() {
  }

  @Override
  @Test
  public void testCorporateBondSecurity() {
    _testCase.testCorporateBondSecurity();
  }

  @Override
  @Test
  public void testEnergyFutureSecurity() {
  }

  @Override
  @Test
  public void testEquityOptionSecurity() {
  }

  @Override
  @Test
  public void testEquitySecurity() {
    EquitySecurity equitySecurity = makeExpectedAAPLEquitySecurity();
    _mockSecuritySource.addSecurity(equitySecurity);
    IdentifierBundle identifierBundle = equitySecurity.getIdentifiers();
    s_logger.debug("sec exchange={} for ID={}", equitySecurity.getExchangeCode(), identifierBundle);
    TimeSeriesMetaData metaData = _metaDataResolver.getDefaultMetaData(identifierBundle);
    assertEquals("BLOOMBERG", metaData.getDataSource());
    assertEquals("PX_LAST", metaData.getDataField());
    String expectedDataProvider = DefaultTimeSeriesResolver.EXCH_PREFIX + equitySecurity.getExchangeCode();
    assertEquals(expectedDataProvider , metaData.getDataProvider());
    assertEquals("NEWYORK_CLOSE", metaData.getObservationTime());
    
    Set<String> expectedDataFields = Sets.newHashSet("PX_LAST", "VOLUME");
    Set<String> expectedDataSources = Sets.newHashSet("BLOOMBERG", "REUTERS");
    Collection<TimeSeriesMetaData> availableMetaData = _metaDataResolver.getAvailableMetaData(identifierBundle);
    assertNotNull(availableMetaData);
    
    assertTrue(availableMetaData.size() == 4);
    for (TimeSeriesMetaData timeSeriesMetaData : availableMetaData) {
      assertTrue(expectedDataFields.contains(timeSeriesMetaData.getDataField()));
      assertTrue(expectedDataSources.contains(timeSeriesMetaData.getDataSource()));
      assertEquals(expectedDataProvider , metaData.getDataProvider());
      assertEquals("NEWYORK_CLOSE", metaData.getObservationTime());
    }
  }

  @Override
  @Test
  public void testFRASecurity() {
  }

  @Override
  @Test
  public void testFXFutureSecurity() {
  }

  @Override
  @Test
  public void testFXOptionSecurity() {
  }

  @Override
  @Test
  public void testForwardSwapSecurity() {
  }

  @Override
  @Test
  public void testFutureOptionSecurity() {
  }

  @Override
  @Test
  public void testGovernmentBondSecurity() {
  }

  @Override
  @Test
  public void testIndexFutureSecurity() {
  }

  @Override
  @Test
  public void testInterestRateFutureSecurity() {
  }

  @Override
  @Test
  public void testMetalFutureSecurity() {
  }

  @Override
  @Test
  public void testMunicipalBondSecurity() {
  }

  @Override
  @Test
  public void testOptionOptionSecurity() {
  }

  @Override
  @Test
  public void testStockFutureSecurity() {
  }

  @Override
  @Test
  public void testSwapOptionSecurity() {
  }

  @Override
  @Test
  public void testSwapSecurity() {
  }
  
  private class TimeSeriesMetaDataTestCase extends SecurityTestCase {

    @Override
    protected <T extends DefaultSecurity> void testSecurity(Class<T> securityClass, T security) {
      s_logger.debug("Testing {} instance {}", securityClass, security.hashCode());
      _mockSecuritySource.addSecurity(security);
      if (security instanceof FinancialSecurity) {
        FinancialSecurity finSec = (FinancialSecurity) security;
        finSec.accept(new FinancialSecurityVisitorAdapter<Void>(new BondSecurityVisitor<Void>() {
          @Override
          public Void visitCorporateBondSecurity(CorporateBondSecurity security) {
            if (security.getIdentifiers().size() > 0) {
              IdentifierBundle identifierBundle = security.getIdentifiers();
              TimeSeriesMetaData metaData = _metaDataResolver.getDefaultMetaData(identifierBundle);
              assertEquals("BLOOMBERG", metaData.getDataSource());
              assertEquals("PX_LAST", metaData.getDataField());
              assertEquals("CMPL", metaData.getDataProvider());
              assertEquals("LONDON_CLOSE", metaData.getObservationTime());
              
              Set<String> expectedDataFields = Sets.newHashSet("PX_LAST", "VOLUME");
              Set<String> expectedDataSources = Sets.newHashSet("BLOOMBERG", "REUTERS");
              Set<String> expectedDataProviders = Sets.newHashSet("CMPL", "CMPN", "CMPT");
              Collection<TimeSeriesMetaData> availableMetaData = _metaDataResolver.getAvailableMetaData(identifierBundle);
              assertNotNull(availableMetaData);
              assertTrue(availableMetaData.size() == 12);
              for (TimeSeriesMetaData timeSeriesMetaData : availableMetaData) {
                assertTrue(expectedDataFields.contains(timeSeriesMetaData.getDataField()));
                assertTrue(expectedDataSources.contains(timeSeriesMetaData.getDataSource()));
                String dataProvider = timeSeriesMetaData.getDataProvider();
                assertTrue(expectedDataProviders.contains(dataProvider));
                String expectedObservationTime = DefaultTimeSeriesResolver.NON_EXCHANGE_DATA_MAP.get(dataProvider);
                assertEquals(expectedObservationTime, timeSeriesMetaData.getObservationTime());
              }
            }
            return null;
          }

          @Override
          public Void visitGovernmentBondSecurity(GovernmentBondSecurity security) {
            return null;
          }

          @Override
          public Void visitMunicipalBondSecurity(MunicipalBondSecurity security) {
            return null;
          }
        }, new CashSecurityVisitor<Void>() {

          @Override
          public Void visitCashSecurity(CashSecurity security) {
            return null;
          }

        }, new EquitySecurityVisitor<Void>() {

          @Override
          public Void visitEquitySecurity(EquitySecurity security) {
            return null;
          }
        }, new FRASecurityVisitor<Void>() {

          @Override
          public Void visitFRASecurity(FRASecurity security) {
            return null;
          }
        }, new FutureSecurityVisitor<Void>() {

          @Override
          public Void visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
            return null;
          }

          @Override
          public Void visitBondFutureSecurity(BondFutureSecurity security) {
            return null;
          }

          @Override
          public Void visitEnergyFutureSecurity(EnergyFutureSecurity security) {
            return null;
          }

          @Override
          public Void visitFXFutureSecurity(FXFutureSecurity security) {
            return null;
          }

          @Override
          public Void visitIndexFutureSecurity(IndexFutureSecurity security) {
            return null;
          }

          @Override
          public Void visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
            return null;
          }

          @Override
          public Void visitMetalFutureSecurity(MetalFutureSecurity security) {
            return null;
          }

          @Override
          public Void visitStockFutureSecurity(StockFutureSecurity security) {
            return null;
          }
        }, new OptionSecurityVisitor<Void>() {

          @Override
          public Void visitBondOptionSecurity(BondOptionSecurity security) {
            return null;
          }

          @Override
          public Void visitEquityOptionSecurity(EquityOptionSecurity security) {
            return null;
          }

          @Override
          public Void visitFutureOptionSecurity(FutureOptionSecurity security) {
            return null;
          }

          @Override
          public Void visitFXOptionSecurity(FXOptionSecurity security) {
            return null;
          }

          @Override
          public Void visitOptionOptionSecurity(OptionOptionSecurity security) {
            return null;
          }

          @Override
          public Void visitSwapOptionSecurity(SwapOptionSecurity security) {
            return null;
          }
        }, new SwapSecurityVisitor<Void>() {

          @Override
          public Void visitForwardSwapSecurity(ForwardSwapSecurity security) {
            return null;
          }

          @Override
          public Void visitSwapSecurity(SwapSecurity security) {
            return null;
          }
        }));
      }
      
    }
    
  }

}
