/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.impl.InMemoryPortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.util.test.TestGroup;

/**
 * Test the portfolio loader tool behaves as expected. Data should be read from a file and
 * inserted into the correct database masters.
 */
@Test(groups = TestGroup.UNIT)
public class XmlPortfolioLoaderToolTest {

  private ToolContext _toolContext;
  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private SecurityMaster _securityMaster;

  @BeforeMethod
  public void setUp() throws Exception {

    _toolContext = new ToolContext();
    _portfolioMaster = new InMemoryPortfolioMaster();
    _toolContext.setPortfolioMaster(_portfolioMaster);
    _positionMaster = new InMemoryPositionMaster();
    _toolContext.setPositionMaster(_positionMaster);
    _securityMaster = new InMemorySecurityMaster();
    _toolContext.setSecurityMaster(_securityMaster);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _toolContext = null;
    _positionMaster = null;
    _portfolioMaster = null;
    _securityMaster = null;
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testLoadingFileWithWrongRootElementFails() {
    String fileLocation = "src/test/resources/xml_portfolios/wrong_root_element.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testLoadingFileWithNoSchemaVersionFails() {
    String fileLocation = "src/test/resources/xml_portfolios/empty_portfolio_no_version";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testDuplicateExternalIds() {
    String fileLocation = "src/test/resources/xml_portfolios/duplicate_external_ids.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();
  }

  @Test
  public void testEmptyPortfolio() {

    // We should get a default portfolio and position automatically generated for the trades
    String fileLocation = "src/test/resources/xml_portfolios/empty_portfolio.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 0);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 0);
  }

  @Test
  public void testMissingReference() {

    String fileLocation = "src/test/resources/xml_portfolios/missing_trade_reference.xml";
    File file = new File(fileLocation);
    try {
      new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true,
                          true, null).execute();
    }
    catch (OpenGammaRuntimeException e) {

      // Parse failed so no data should have been added to masters
      assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 0);
      assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 0);
      assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 0);
    }
  }

  @Test
  public void testSinglePortfolioSinglePositionNoTrades() {
    // to be added
  }

  @Test
  public void testSinglePortfolioMultiplePositionNoTrades() {
    // to be added
  }

  @Test
  public void testNoPortfolioMultiplePositionNoTradesCreatesDefaultPortfolio() {
     // to be added
  }

  @Test
  public void testNoPortfolioNoPositionMultipleTradesCreatesDefaultPortfolioAndPositions() {

    // We should get a default portfolio and position automatically generated for the trades
    String fileLocation = "src/test/resources/xml_portfolios/double_fx_option_no_position_no_portfolio.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 2);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 2);
  }

  @Test
  public void testSinglePortfolioSinglePositionSingleIrs() {

    String fileLocation = "src/test/resources/xml_portfolios/single_irs.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  @Test
  public void testSinglePortfolioSinglePositionFra() {

    String fileLocation = "src/test/resources/xml_portfolios/single_fra.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  
  @Test
  public void testSinglePortfolioSinglePositionSingleFxOption() {

    String fileLocation = "src/test/resources/xml_portfolios/single_fx_option.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  @Test
  public void testSinglePortfolioNoPositionSingleFxOption() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/single_fx_option_no_position.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }


  /*
  Removed until ready to fully implement
  @Test
  public void testSinglePortfolioNoPositionSingleEquityVarianceSwap() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/single_equity_variance_swap_no_position.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }
  */

  @Test
  public void testSinglePortfolioNoPositionSingleFxDigitalOption() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/fx_digital_option_no_position.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  @Test
  public void testSinglePortfolioNoPositionSingleFxForward() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/fx_forward_no_position.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  @Test
  public void testSinglePortfolioNoPositionSingleNdfFxForward() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/ndf_fx_forward_no_position.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  @Test
  public void testSinglePortfolioNoPositionSingleOtcEquityIndexOption() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/otc_equity_index_option.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    List<ManageablePosition> positions = _positionMaster.search(new PositionSearchRequest()).getPositions();
    assertEquals(positions.size(), 1);

    assertEquals(Iterables.getOnlyElement(positions.get(0).getTrades()).getQuantity(), new BigDecimal(250000));
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  @Test
  public void testSinglePortfolioNoPositionListedEquityIndexOption() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/listed_index_option.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    List<ManageablePosition> positions = _positionMaster.search(new PositionSearchRequest()).getPositions();
    assertEquals(positions.size(), 1);

    ManageablePosition position = positions.get(0);
    List<ManageableTrade> trades = position.getTrades();
    assertEquals(trades.size(), 2);
    assertEquals(trades.get(0).getQuantity(), BigDecimal.valueOf(25000));
    assertEquals(trades.get(1).getQuantity(), BigDecimal.valueOf(-10000));

    assertEquals(position.getQuantity(), BigDecimal.valueOf(15000));
    assertEquals(position.getAttributes().get("pos-attr1"), "pos-attr1-value");
    assertEquals(position.getAttributes().get("pos-attr2"), "pos-attr2-value");

    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  @Test
  public void testSinglePortfolioNoPositionListedEquityIndexFuture() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/listed_index_future.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    List<ManageablePosition> positions = _positionMaster.search(new PositionSearchRequest()).getPositions();
    assertEquals(positions.size(), 1);

    ManageablePosition position = positions.get(0);
    List<ManageableTrade> trades = position.getTrades();
    assertEquals(trades.size(), 1);
    assertEquals(trades.get(0).getQuantity(), BigDecimal.valueOf(1000));
    assertEquals(position.getQuantity(), BigDecimal.valueOf(1000));

    List<ManageableSecurity> securities = _securityMaster.search(new SecuritySearchRequest()).getSecurities();
    assertEquals(securities.size(), 1);
    assertEquals(securities.get(0).getAttributes().get("sec-attr1"), "sec-attr1-value");
    assertEquals(securities.get(0).getAttributes().get("sec-attr2"), "sec-attr2-value"); }

  @Test
  public void testSinglePortfolioNoPositionListedEquityIndexFutureOption() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/listed_index_future_option.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    List<ManageablePosition> positions = _positionMaster.search(new PositionSearchRequest()).getPositions();
    assertEquals(positions.size(), 1);

    ManageablePosition position = positions.get(0);
    List<ManageableTrade> trades = position.getTrades();
    assertEquals(trades.size(), 1);
    ManageableTrade trade = trades.get(0);
    assertEquals(trade.getQuantity(), BigDecimal.valueOf(1000));
    assertEquals(trade.getAttributes().get("attr1"), "attr1-value");
    assertEquals(trade.getAttributes().get("attr2"), "attr2-value");

    assertEquals(position.getQuantity(), BigDecimal.valueOf(1000));

    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  @Test
  public void testSinglePortfolioNoPositionSingleSwaption() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/swaption_no_position.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);

    // We end up with 2 securities: the swaption itself, and its underlying swap
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 2);
  }

  @Test
  public void testSinglePortfolioNoPositionMultipleFxOption() {

    // We should get a position automatically generated for the trades
    String fileLocation = "src/test/resources/xml_portfolios/double_fx_option_no_position.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 2);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 2);
  }

  @Test
  public void testSinglePortfolioSinglePositionMultipleIrs() {

    String fileLocation = "src/test/resources/xml_portfolios/triple_irs.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 3);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 3);
  }

  @Test
  public void testNestedPortfolios() {
    String fileLocation = "src/test/resources/xml_portfolios/nested_portfolios.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    List<ManageablePortfolio> portfolios = _portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios();
    assertEquals(portfolios.size(), 1);

    assertEquals(portfolios.get(0).getRootNode().getChildNodes().size(), 2);

    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 4);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 4);
  }

  @Test
  public void testDefineSecuritiesAgainstPositions() {

    String fileLocation = "src/test/resources/xml_portfolios/position_defined_securities_portfolio.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 2);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 2);
  }

  @Test
  public void testMultiPortfolioLoad() {

    String fileLocation = "src/test/resources/xml_portfolios/multi_portfolio.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 2);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 2);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 2);
  }

  @Test
  public void testMultitypePortfolioLoad() {

    String fileLocation = "src/test/resources/xml_portfolios/multitype_portfolio.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 2);
    Set<ManageableTrade> tradeSet = extractTrades(_positionMaster);
    assertEquals(tradeSet.size(), 2);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 2);
  }
  
  private Set<ManageableTrade> extractTrades(PositionMaster positionMaster) {
    Set<ManageableTrade> tradeSet = Sets.newHashSet();
    for (ManageablePosition p : positionMaster.search(new PositionSearchRequest()).getPositions()) {
      tradeSet.addAll(p.getTrades());
    }
    return tradeSet;
  }

  @Test
  public void testMultiPortfolioLoadWithBadPortfolio() {

    String fileLocation = "src/test/resources/xml_portfolios/double_portfolio_one_bad.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, false, false, false, true, true, null).execute();

    // Only one of the portfolios should ,make it in
    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }
}
