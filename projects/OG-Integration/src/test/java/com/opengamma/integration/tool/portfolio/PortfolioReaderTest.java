/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.integration.copier.portfolio.reader.SingleSheetSimplePositionReader;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

@Test(groups = TestGroup.UNIT)
public class PortfolioReaderTest {

  private File _tempFile;

  @Test
  public void testListedSecurityLoad() {

    // An EquityIndexFutureOption
    String data = "\"currency\",\"exchange\",\"exerciseType\",\"expiry\",\"externalIdBundle\",\"underlyingId\",\"optionType\",\"position:quantity\",\"securityType\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"USD\",\"NEW YORK STOCK EXCHANGE INC.\",\"European\",\"2050-01-01T00:00:00+00:00[Europe/London]\",\"EIFO_ID~EIFO1234\",\"UNDERLYING_ID~ul9999\",\"PUT\",\"1264\",\"EQUITY_INDEX_FUTURE_OPTION\",\"CPID~123\",,,,,,1000,\"2014-01-01\",\n";
    populateFileWithData(data);
    PortfolioReader portfolioReader = new PortfolioReader(new SingleSheetSimplePositionReader(_tempFile.getAbsolutePath(), "EquityIndexFutureOption"),
                                                             "EquityIndexFutureOption Portfolio"
    );

    Pair<Portfolio, Set<ManageableSecurity>> pair = portfolioReader.createPortfolio();
    Portfolio portfolio = pair.getFirst();
    assertThat(portfolio.getName(), is("EquityIndexFutureOption Portfolio"));

    Set<ManageableSecurity> securities = pair.getSecond();
    assertThat(securities.size(), is(1));
    assertThat(securities.iterator().next().getExternalIdBundle(), is(ExternalIdBundle.of("EIFO_ID", "EIFO1234")));
  }

  @Test
  public void testOtcSecurityLoad() {
    String data = "adjustCashSettlementDate,adjustEffectiveDate,adjustMaturityDate,businessDayConvention,buy,cashSettlementDate,coupon,couponFrequency,dayCount,debtSeniority,effectiveDate,immAdjustMaturityDate,includeAccruedPremium,maturityDate,name,notional,position:quantity,protectionBuyer,protectionSeller,protectionStart,quotedSpread,recoveryRate,referenceEntity,regionId,restructuringClause,startDate,stubType,trade:counterpartyExternalId,trade:premium,trade:premiumCurrency,trade:premiumDate,trade:premiumTime,trade:quantity,trade:tradeDate,trade:tradeTime,upfrontAmount\n" +
        "FALSE,FALSE,FALSE,Following,TRUE,2013-03-24T00:00:00.0Z,100,Semi-annual,ACT/360,SNRFOR,2013-03-21T00:00:00.0Z,FALSE,TRUE,2020-03-20T00:00:00.0Z,STEM GBP 100 7Y,GBP 1000000,1,EXTERNAL_CODE~ProtBuyer_1,EXTERNAL_CODE~ProtSeller_1,TRUE,100,0.4,MARKIT_RED_CODE~5AB67W,FINANCIAL_REGION~CARIBBEAN,MR,2013-03-20T00:00:00.0Z,SHORT_START,EXTERNAL_CODE~ProtSeller_1,,,,,1,2013-03-22,,GBP 50000";
    populateFileWithData(data);

    PortfolioReader portfolioReader = new PortfolioReader(new SingleSheetSimplePositionReader(_tempFile.getAbsolutePath(), "StandardVanillaCDS"),
                                                             "CDS Portfolio"
    );

    Pair<Portfolio, Set<ManageableSecurity>> pair = portfolioReader.createPortfolio();
    Portfolio portfolio = pair.getFirst();
    assertThat(portfolio.getName(), is("CDS Portfolio"));

    Set<ManageableSecurity> securities = pair.getSecond();
    assertThat(securities.size(), is(1));
    // We expect an external id to be generated for us
    assertThat(securities.iterator().next().getExternalIdBundle().isEmpty(), is(false));
  }


  @BeforeMethod
  public void doSetUp() throws Exception {
    _tempFile = File.createTempFile("portfolio-", ".csv");
  }

  @AfterMethod
  public void doTearDown() {
    // Clean up the file we were using - not strictly necessary as it's a temp one anyway
    if (_tempFile != null && _tempFile.exists()) {
      _tempFile.delete();
    }
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
