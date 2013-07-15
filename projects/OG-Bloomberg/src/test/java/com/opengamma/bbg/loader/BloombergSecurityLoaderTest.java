/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.util.BloombergSecurityUtils.makeAPVLEquityOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeCommodityFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEURIBORFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEURODOLLARFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEquityIndexDividendFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEquityIndexFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeExpectedAAPLEquitySecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeFxFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeInterestRateFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeLIBORFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeSPXIndexOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeUSBondFuture;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.security.BloombergSecurityProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.DefaultSecurityLoader;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDetailProvider;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterFiles;
import com.opengamma.util.db.DbConnectorFactoryBean;
import com.opengamma.util.db.HibernateMappingFiles;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION, singleThreaded = true)
public class BloombergSecurityLoaderTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(BloombergSecurityLoaderTest.class);

  private BloombergReferenceDataProvider _bbgProvider;
  private DbSecurityMaster _securityMaster;
  private DefaultSecurityLoader _securityLoader;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public BloombergSecurityLoaderTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected Class<?> dbConnectorScope() {
    return BloombergSecurityLoaderTest.class;
  }

  @Override
  protected void initDbConnectorFactory(DbConnectorFactoryBean factory) {
    factory.setHibernateMappingFiles(new HibernateMappingFiles[] {new HibernateSecurityMasterFiles() });
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUpClass() {
    _bbgProvider = BloombergTestUtils.getBloombergReferenceDataProvider();
    _bbgProvider.start();
    ReferenceDataProvider cachingProvider = BloombergTestUtils.getMongoCachingReferenceDataProvider(_bbgProvider);
    ExchangeDataProvider exchangeProvider = DefaultExchangeDataProvider.getInstance();
    BloombergSecurityProvider secProvider = new BloombergSecurityProvider(cachingProvider, exchangeProvider );
    _securityMaster = new DbSecurityMaster(getDbConnector());
    _securityMaster.setDetailProvider(new HibernateSecurityMasterDetailProvider());
    _securityLoader = new DefaultSecurityLoader(_securityMaster, secProvider);
  }

  @Override
  protected void doTearDownClass() {
    if (_bbgProvider != null) {
      _bbgProvider.stop();
      _bbgProvider = null;
    }
    _securityMaster = null;
  }

  //-------------------------------------------------------------------------
  private void assertLoadAndSaveSecurity(FinancialSecurity expected) {
    //test we can load security from bloomberg
    ExternalIdBundle identifierBundle = expected.getExternalIdBundle();

    Map<ExternalIdBundle, UniqueId> loadedSecurities = _securityLoader.loadSecurities(Collections.singleton(identifierBundle));
    assertNotNull(loadedSecurities);
    assertEquals(1, loadedSecurities.size());
    UniqueId uid = loadedSecurities.get(identifierBundle);
    assertNotNull(uid);

    //test we can add and read from secmaster
    SecurityDocument securityDocument = _securityMaster.get(uid);
    assertNotNull(securityDocument);

    final Security fromSecMaster = securityDocument.getSecurity();
    assertNotNull(fromSecMaster);

    expected.accept(new FinancialSecurityVisitorAdapter<Void>() {

      private void assertSecurity() {
        fail();
      }

      @Override
      public Void visitCorporateBondSecurity(CorporateBondSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitGovernmentBondSecurity(GovernmentBondSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitMunicipalBondSecurity(MunicipalBondSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitCashSecurity(CashSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitCashFlowSecurity(CashFlowSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitEquitySecurity(EquitySecurity security) {
        assertTrue(fromSecMaster instanceof EquitySecurity);
        EquitySecurity actual = (EquitySecurity) fromSecMaster;

        assertEquals(security.getCompanyName(), actual.getCompanyName());
        assertEquals(security.getCurrency(), actual.getCurrency());
        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExchangeCode(), actual.getExchangeCode());
        assertEquals(security.getGicsCode(), actual.getGicsCode());
        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertEquals(security.getShortName(), actual.getShortName());
        assertNotNull(actual.getUniqueId());
        return null;
      }

      @Override
      public Void visitFRASecurity(FRASecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitBondFutureSecurity(BondFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitEnergyFutureSecurity(EnergyFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitEquityFutureSecurity(EquityFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitFXFutureSecurity(FXFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitIndexFutureSecurity(IndexFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitMetalFutureSecurity(MetalFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitStockFutureSecurity(StockFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      private Void visitFutureSecurity(FutureSecurity security) {
        security.accept(new FinancialSecurityVisitorAdapter<Void>() {

          @Override
          public Void visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitBondFutureSecurity(BondFutureSecurity security) {
            assertTrue("Security is instance of: " + fromSecMaster.getClass().getName(), fromSecMaster instanceof BondFutureSecurity);
            BondFutureSecurity actual = (BondFutureSecurity) fromSecMaster;

            assertEquals(new HashSet<>(security.getBasket()), new HashSet<>(actual.getBasket()));

            assertEquals(security.getContractCategory(), actual.getContractCategory());
            assertEquals(security.getCurrency(), actual.getCurrency());
            assertEquals(security.getExpiry(), actual.getExpiry());
            assertEquals(security.getFirstDeliveryDate(), actual.getFirstDeliveryDate());
            assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
            assertEquals(security.getLastDeliveryDate(), actual.getLastDeliveryDate());
            assertEquals(security.getName(), actual.getName());
            assertEquals(security.getSecurityType(), actual.getSecurityType());
            assertEquals(security.getSettlementExchange(), actual.getSettlementExchange());
            assertEquals(security.getTradingExchange(), actual.getTradingExchange());
            assertEquals(security.getUnitAmount(), actual.getUnitAmount());
            assertNotNull(actual.getUniqueId());

            //test underlying is loaded as well
            for (BondFutureDeliverable deliverable : security.getBasket()) {
              ExternalIdBundle identifiers = deliverable.getIdentifiers();
              assertUnderlyingIsLoaded(identifiers);
            }
            return null;
          }

          @Override
          public Void visitEnergyFutureSecurity(EnergyFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitEquityFutureSecurity(EquityFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitFXFutureSecurity(FXFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitIndexFutureSecurity(IndexFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
            assertTrue(fromSecMaster instanceof InterestRateFutureSecurity);
            InterestRateFutureSecurity actual = (InterestRateFutureSecurity) fromSecMaster;
            assertEquals(security.getCurrency(), actual.getCurrency());
            assertEquals(security.getExpiry(), actual.getExpiry());
            assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
            assertEquals(security.getName(), actual.getName());
            assertEquals(security.getSecurityType(), actual.getSecurityType());
            assertEquals(security.getSettlementExchange(), actual.getSettlementExchange());
            assertEquals(security.getTradingExchange(), actual.getTradingExchange());
            assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());
            assertEquals(security.getUnitAmount(), actual.getUnitAmount());
            assertNotNull(actual.getUniqueId());
            return null;
          }

          @Override
          public Void visitMetalFutureSecurity(MetalFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitStockFutureSecurity(StockFutureSecurity security) {
            assertSecurity();
            return null;
          }
        });
        return null;
      }

      @Override
      public Void visitSwapSecurity(SwapSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
        assertTrue(fromSecMaster instanceof EquityIndexOptionSecurity);
        EquityIndexOptionSecurity actual = (EquityIndexOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());
        assertNotNull(actual.getUniqueId());
        return null;
      }

      @Override
      public Void visitEquityOptionSecurity(EquityOptionSecurity security) {
        assertTrue(fromSecMaster instanceof EquityOptionSecurity);
        EquityOptionSecurity actual = (EquityOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());
        assertNotNull(actual.getUniqueId());

        //test underlying is loaded as well
        ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;
      }

      @Override
      public Void visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
        assertSecurity();
        return null;
      }
      
      @Override
      public Void visitFXOptionSecurity(FXOptionSecurity security) {
        assertSecurity();
        return null;
      }
      
      @Override
      public Void visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitSwaptionSecurity(SwaptionSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
        assertTrue(fromSecMaster instanceof IRFutureOptionSecurity);
        IRFutureOptionSecurity actual = (IRFutureOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.isMargined(), actual.isMargined());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());
        
        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertNotNull(actual.getUniqueId());

        //test underlying is loaded as well
        ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;           
      }

      @Override
      public Void visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security) {
        assertTrue(fromSecMaster instanceof CommodityFutureOptionSecurity);
        CommodityFutureOptionSecurity actual = (CommodityFutureOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getTradingExchange(), actual.getTradingExchange());
        assertEquals(security.getSettlementExchange(), actual.getSettlementExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());
        
        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertNotNull(actual.getUniqueId());

        //test underlying is loaded as well
        ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;
      }

      @Override
      public Void visitFxFutureOptionSecurity(FxFutureOptionSecurity security) {
        assertTrue(fromSecMaster instanceof FxFutureOptionSecurity);
        FxFutureOptionSecurity actual = (FxFutureOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getTradingExchange(), actual.getTradingExchange());
        assertEquals(security.getSettlementExchange(), actual.getSettlementExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());

        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertNotNull(actual.getUniqueId());

        //test underlying is loaded as well
        ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;
      }

      @Override
      public Void visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity security) {

        assertTrue(fromSecMaster instanceof EquityIndexDividendFutureOptionSecurity);
        EquityIndexDividendFutureOptionSecurity actual = (EquityIndexDividendFutureOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.isMargined(), actual.isMargined());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());

        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertNotNull(actual.getUniqueId());

        //test underlying is loaded as well
        ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;
      }

      @Override
      public Void visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {

        assertTrue(fromSecMaster instanceof EquityIndexFutureOptionSecurity);
        EquityIndexFutureOptionSecurity actual = (EquityIndexFutureOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.isMargined(), actual.isMargined());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());

        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertNotNull(actual.getUniqueId());

        //test underlying is loaded as well
        ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;
      }

      @Override
      public Void visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
        assertSecurity();
        return null;
      }

       @Override
      public Void visitFXForwardSecurity(FXForwardSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
        assertSecurity();      
        return null;
      }
      

      @Override
      public Void visitCapFloorSecurity(CapFloorSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitFXDigitalOptionSecurity(FXDigitalOptionSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity security) {
        assertSecurity();
        return null;
      }

    	@Override
    	public Void visitSimpleZeroDepositSecurity(SimpleZeroDepositSecurity security) {
    		assertSecurity();
    		return null;
    	}

    	@Override
    	public Void visitPeriodicZeroDepositSecurity(PeriodicZeroDepositSecurity security) {
    		assertSecurity();
    		return null;
    	}

    	@Override
    	public Void visitContinuousZeroDepositSecurity(ContinuousZeroDepositSecurity security) {
    		assertSecurity();
    		return null;
    	}
    });
  }

  public void testEquityOptionSecurity() {
    assertLoadAndSaveSecurity(makeAPVLEquityOptionSecurity());
  }

  public void testEquityIndexOptionSecurity() {
    assertLoadAndSaveSecurity(makeSPXIndexOptionSecurity());
  }

  public void testEquityIndexFutureOptionSecurity() {
    assertLoadAndSaveSecurity(makeEquityIndexFutureOptionSecurity());
  }

  public void testEquityIndexDividendFutureOptionSecurity() {
    assertLoadAndSaveSecurity(makeEquityIndexDividendFutureOptionSecurity());
  }

  public void testEquitySecurity() {
    assertLoadAndSaveSecurity(makeExpectedAAPLEquitySecurity());
  }

  public void testInterestRateFutureSecurity() {
    assertLoadAndSaveSecurity(makeInterestRateFuture());
  }

  public void testBondFutureSecurity() {
    assertLoadAndSaveSecurity(makeUSBondFuture());
  }

  public void testIRFutureOptionSecurity() {
    assertLoadAndSaveSecurity(makeEURODOLLARFutureOptionSecurity());
    assertLoadAndSaveSecurity(makeLIBORFutureOptionSecurity());
    assertLoadAndSaveSecurity(makeEURIBORFutureOptionSecurity());
  }

  public void testCommodityFutureOptionSecurity() {
    assertLoadAndSaveSecurity(makeCommodityFutureOptionSecurity());
  }

  public void testFxFutureOptionSecurity() {
    assertLoadAndSaveSecurity(makeFxFutureOptionSecurity());
  }

  private void assertUnderlyingIsLoaded(final ExternalId underlyingIdentifier) {
    assertUnderlyingIsLoaded(ExternalIdBundle.of(underlyingIdentifier));
  }

  private void assertUnderlyingIsLoaded(ExternalIdBundle identifiers) {
    SecuritySearchResult result = _securityMaster.search(new SecuritySearchRequest(identifiers));
    assertNotNull(result);
    assertFalse(result.getDocuments().isEmpty());
    assertNotNull(result.getFirstDocument().getSecurity());
  }

}
