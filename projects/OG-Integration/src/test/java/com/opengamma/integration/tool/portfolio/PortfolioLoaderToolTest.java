/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.masterdb.portfolio.DbPortfolioMaster;
import com.opengamma.masterdb.position.DbPositionMaster;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test the portfolio loader tool behaves as expected. Data should be read from a file and
 * inserted into the correct database masters.
 */
@Test(groups = TestGroup.INTEGRATION)
public class PortfolioLoaderToolTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioLoaderToolTest.class);

  private ToolContext _toolContext;
  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private SecurityMaster _securityMaster;

  private File _tempFile;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public PortfolioLoaderToolTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  public void doSetUp() throws Exception {
    _tempFile = File.createTempFile("portfolio-", ".csv");
    s_logger.info("Created temp file: " + _tempFile.getAbsolutePath());

    _portfolioMaster = new DbPortfolioMaster(getDbConnector());
    _positionMaster = new DbPositionMaster(getDbConnector());
    _securityMaster = new DbSecurityMaster(getDbConnector());

    _toolContext = new ToolContext();
    _toolContext.setPortfolioMaster(_portfolioMaster);
    _toolContext.setPositionMaster(_positionMaster);
    _toolContext.setSecurityMaster(_securityMaster);
  }

  @Override
  public void doTearDown() {
    _toolContext = null;
    _positionMaster = null;
    _portfolioMaster = null;

    // Clean up the file we were using
    if (_tempFile != null && _tempFile.exists()) {
      s_logger.info("Removing file: " + _tempFile.getAbsolutePath());
      _tempFile.delete();
    }
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToolContextMustBeProvided() {
    new PortfolioLoader(null, "My portfolio", "Equity", _tempFile.getAbsolutePath(), true, false, false, false, true,
        true, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPortfolioNameMustBeProvided() {
    new PortfolioLoader(_toolContext, null, "Equity", _tempFile.getAbsolutePath(), true, false, false, false, true,
        true, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFilenameMustBeProvided() {
    new PortfolioLoader(_toolContext, "My portfolio", "Equity", null, true, false, false, false, true, true, null);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testFileMustHaveRecognisedExtension() {
    new PortfolioLoader(_toolContext, "My portfolio", "Equity", "some_file.goobledygook", true,
        false, false, false, true,
        true, null).execute();
  }

  @Test
  public void testLoadEquityPortfolio() throws IOException {

    final String data = "\"companyName\",\"currency\",\"exchange\",\"exchangeCode\",\"externalIdBundle\",\"name\",\"position:quantity\",\"securityType\",\"shortName\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"EXXON MOBIL CORP\",\"USD\",\"NEW YORK STOCK EXCHANGE INC.\",\"XNYS\",\"BLOOMBERG_BUID~EQ0010054600001000, BLOOMBERG_TICKER~XOM US Equity, CUSIP~30231G102, ISIN~US30231G1022, SEDOL1~2326618\",\"EXXON MOBIL CORP\",\"1264\",\"EQUITY\",\"XOM\",\"CPID~123\",,,,,,,,\n" +
        "\"APPLE INC\",\"USD\",\"NASDAQ/NGS (GLOBAL SELECT MARKET)\",\"XNGS\",\"BLOOMBERG_BUID~EQ0010169500001000, BLOOMBERG_TICKER~AAPL US Equity, CUSIP~037833100, ISIN~US0378331005, SEDOL1~2046251\",\"APPLE INC\",\"257\",\"EQUITY\",\"AAPL\",\"CPID~234\",,,,,,,,\n" +
        "\"MICROSOFT CORP\",\"USD\",\"NASDAQ/NGS (GLOBAL SELECT MARKET)\",\"XNGS\",\"BLOOMBERG_BUID~EQ0010174300001000, BLOOMBERG_TICKER~MSFT US Equity, CUSIP~594918104, ISIN~US5949181045, SEDOL1~2588173\",\"MICROSOFT CORP\",\"3740\",\"EQUITY\",\"MSFT\",\"CPID~345\",,,,,,,,";

    doPortfolioLoadTest("Equity Portfolio", "Equity", data, 1, 3, 3);
  }


  @Test
  public void testLoadEquityPortfolioMultiplePositionsSameSecurity() throws IOException {

    final String data = "\"companyName\",\"currency\",\"exchange\",\"exchangeCode\",\"externalIdBundle\",\"name\",\"position:quantity\",\"securityType\",\"shortName\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"EXXON MOBIL CORP\",\"USD\",\"NEW YORK STOCK EXCHANGE INC.\",\"XNYS\",\"BLOOMBERG_BUID~EQ0010054600001000, BLOOMBERG_TICKER~XOM US Equity, CUSIP~30231G102, ISIN~US30231G1022, SEDOL1~2326618\",\"EXXON MOBIL CORP\",\"1264\",\"EQUITY\",\"XOM\",\"CPID~123\",,,,,,,,\n" +
        "\"APPLE INC\",\"USD\",\"NASDAQ/NGS (GLOBAL SELECT MARKET)\",\"XNGS\",\"BLOOMBERG_BUID~EQ0010169500001000, BLOOMBERG_TICKER~AAPL US Equity, CUSIP~037833100, ISIN~US0378331005, SEDOL1~2046251\",\"APPLE INC\",\"257\",\"EQUITY\",\"AAPL\",\"CPID~234\",,,,,,,,\n" +
        "\"MICROSOFT CORP\",\"USD\",\"NASDAQ/NGS (GLOBAL SELECT MARKET)\",\"XNGS\",\"BLOOMBERG_BUID~EQ0010174300001000, BLOOMBERG_TICKER~MSFT US Equity, CUSIP~594918104, ISIN~US5949181045, SEDOL1~2588173\",\"MICROSOFT CORP\",\"3740\",\"EQUITY\",\"MSFT\",\"CPID~345\",,,,,,,,\n" +
        "\"MICROSOFT CORP\",\"USD\",\"NASDAQ/NGS (GLOBAL SELECT MARKET)\",\"XNGS\",\"BLOOMBERG_BUID~EQ0010174300001000, BLOOMBERG_TICKER~MSFT US Equity, CUSIP~594918104, ISIN~US5949181045, SEDOL1~2588173\",\"MICROSOFT CORP\",\"3740\",\"EQUITY\",\"MSFT\",\"CPID~345\",,,,,,,,";

    doPortfolioLoadTest("Equity Portfolio", "Equity", data, 1, 4, 3);
  }

  @Test
  public void testLoadEquityIndexFutureOptionPortfolio() throws IOException {

    final String data = "\"currency\",\"exchange\",\"exerciseType\",\"expiry\",\"externalIdBundle\",\"underlyingId\",\"optionType\",\"position:quantity\",\"securityType\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"USD\",\"NEW YORK STOCK EXCHANGE INC.\",\"EX_TYPE\",\"2050-01-01T00:00:00+00:00[Europe/London]\",\"EIFO_ID~EIFO1234\",\"UNDERLYING_ID~ul9999\",\"PUT\",\"1264\",\"EQUITY_INDEX_FUTURE_OPTION\",\"CPID~123\",,,,,,,,\n";

    doPortfolioLoadTest("EquityIndexFutureOption Portfolio", "EquityIndexFutureOption", data, 1, 1, 1);
  }

  @Test
  public void testLoadEquityIndexDividendFutureOptionPortfolio() throws IOException {

    final String data = "\"currency\",\"exchange\",\"exerciseType\",\"expiry\",\"externalIdBundle\",\"underlyingId\",\"optionType\",\"position:quantity\",\"securityType\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"USD\",\"NEW YORK STOCK EXCHANGE INC.\",\"EX_TYPE\",\"2050-01-01T00:00:00+00:00[Europe/London]\",\"EIFO_ID~EIFO1234\",\"UNDERLYING_ID~ul9999\",\"PUT\",\"1264\",\"EQUITY_INDEX_DIVIDEND_FUTURE_OPTION\",\"CPID~123\",,,,,,,,\n";

    doPortfolioLoadTest("EquityIndexDividendFutureOption Portfolio", "EquityIndexDividendFutureOption", data, 1, 1, 1);
  }

  @Test
  public void testLoadCashFlowPortfolio() throws IOException {

    final String data = "\"amount\",\"currency\",\"settlement\",\"externalIdBundle\",\"position:quantity\",\"securityType\",\"shortName\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "150000,\"USD\",\"2014-01-01T00:00:00+00:00[Europe/London]\",\"SOME_ID~CF001\",\"4\",\"CASHFLOW\",,\"CPID~123\",,,,,,,,\n" +
        "60000,\"EUR\",\"2014-02-02T00:00:00+00:00[Europe/London]\",\"SOME_ID~CF002\",\"2\",\"CASHFLOW\",,\"CPID~234\",,,,,,,,\n";

    doPortfolioLoadTest("Cashflow Portfolio", "CashFlow", data, 1, 2, 2);
  }

  @Test
  public void testLoadCommodityFutureOptionPortfolio() throws IOException {

    final String data = "\"currency\",\"tradingExchange\",\"settlementExchange\",\"exerciseType\",\"expiry\",\"externalIdBundle\",\"underlyingId\",\"optionType\",\"position:quantity\",\"securityType\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"USD\",\"CME\",\"CME\",\"EX_TYPE\",\"2050-01-01T00:00:00+00:00[Europe/London]\",\"EIFO_ID~EIFO1234\",\"UNDERLYING_ID~ul9999\",\"PUT\",\"1264\",\"COMMODITY_FUTUREOPTION\",\"CPID~123\",,,,,,,,\n";

    doPortfolioLoadTest("CommodityFutureOption Portfolio", "CommodityFutureOption", data, 1, 1, 1);
  }

  @Test
  public void testStandardCdsPortfolio() throws IOException {

    final String data = "adjustCashSettlementDate,adjustEffectiveDate,adjustMaturityDate,businessDayConvention,buy,cashSettlementDate,coupon,couponFrequency,dayCount,debtSeniority,effectiveDate,externalIdBundle,immAdjustMaturityDate,includeAccruedPremium,maturityDate,name,notional,position:quantity,protectionBuyer,protectionSeller,protectionStart,quotedSpread,recoveryRate,referenceEntity,regionId,restructuringClause,startDate,stubType,trade:counterpartyExternalId,trade:premium,trade:premiumCurrency,trade:premiumDate,trade:premiumTime,trade:quantity,trade:tradeDate,trade:tradeTime,upfrontAmount\n" +
        "FALSE,FALSE,FALSE,Following,TRUE,2013-03-24T00:00:00.0Z,100,Semi-annual,ACT/360,SNRFOR,2013-03-21T00:00:00.0Z,CSV_LOADER~41355.6232870372,FALSE,TRUE,2020-03-20T00:00:00.0Z,STEM GBP 100 7Y,GBP 1000000,1,EXTERNAL_CODE~ProtBuyer_1,EXTERNAL_CODE~ProtSeller_1,TRUE,100,0.4,MARKIT_RED_CODE~5AB67W,FINANCIAL_REGION~CARIBBEAN,MR,2013-03-20T00:00:00.0Z,SHORT_START,EXTERNAL_CODE~ProtSeller_1,,,,,1,2013-03-22,,GBP 50000";

    doPortfolioLoadTest("Standard CDS Portfolio", "StandardVanillaCDS", data, 1, 1, 1);
  }

  @Test
  public void testCdsIndexPortfolio() throws IOException {

    final String data = "adjustSettlementDate,adjustEffectiveDate,adjustMaturityDate,businessDayConvention,buy,settlementDate,coupon,couponFrequency,dayCount,effectiveDate,externalIdBundle,immAdjustMaturityDate,includeAccruedPremium,indexCoupon,maturityDate,name,notional,position:quantity,protectionBuyer,protectionSeller,protectionStart,quotedSpread,recoveryRate,referenceEntity,startDate,stubType,trade:counterpartyExternalId,trade:premium,trade:premiumCurrency,trade:premiumDate,trade:premiumTime,trade:quantity,trade:tradeDate,trade:tradeTime,upfrontPayment\n" +
        "FALSE,FALSE,FALSE,Following,TRUE,2013-03-24T00:00:00.0Z,100,Semi-annual,ACT/360,2013-03-21T00:00:00.0Z,CSV_LOADER~41355.6232870372,FALSE,TRUE,0.01,2020-03-20T00:00:00.0Z,STEM GBP 100 7Y,GBP 1000000,1,EXTERNAL_CODE~ProtBuyer_1,EXTERNAL_CODE~ProtSeller_1,TRUE,100,0.4,MARKIT_RED_CODE~5AB67W,2013-03-20T00:00:00.0Z,SHORT_START,EXTERNAL_CODE~ProtSeller_1,,,,,1,2013-03-22,,GBP 50000";

    doPortfolioLoadTest("CDS Index Portfolio", "CreditDefaultSwapIndex", data, 1, 1, 1);
  }

  @Test
  public void testLoadFxFutureOptionPortfolio() throws IOException {

    final String data = "\"currency\",\"tradingExchange\",\"settlementExchange\",\"exerciseType\",\"expiry\",\"externalIdBundle\",\"underlyingId\",\"optionType\",\"position:quantity\",\"securityType\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"USD\",\"CME\",\"CME\",\"EX_TYPE\",\"2050-01-01T00:00:00+00:00[Europe/London]\",\"EIFO_ID~EIFO1234\",\"UNDERLYING_ID~ul9999\",\"PUT\",\"1264\",\"FX_FUTUREOPTION\",\"CPID~123\",,,,,,,,\n";

    doPortfolioLoadTest("FxFutureOption Portfolio", "FxFutureOption", data, 1, 1, 1);
  }

  private void doPortfolioLoadTest(final String portfolioName, final String securityType, final String data, final int expectedPortfolios, final int expectedPositions, final int expectedSecurities) {

    populateFileWithData(data);

    new PortfolioLoader(_toolContext, portfolioName, securityType, _tempFile.getAbsolutePath(), true,
        false, false, false, true,
        true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), expectedPortfolios);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), expectedPositions);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), expectedSecurities);
  }

  private void populateFileWithData(final String data) {

    try(BufferedWriter writer = new BufferedWriter(new FileWriter(_tempFile))) {
      writer.write(data);
      writer.flush();
    } catch (final IOException e) {
      fail("Unable to write data to file: " + _tempFile.getAbsolutePath());
    }
  }
}
