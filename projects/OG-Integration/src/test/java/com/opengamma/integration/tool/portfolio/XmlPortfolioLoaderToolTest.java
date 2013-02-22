package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.tool.portfolio.xml.XmlPortfolioReader;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.impl.InMemoryPortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.util.test.DbTest;

/**
 * Test the portfolio loader tool behaves as expected. Data should be read from a file and
 * inserted into the correct database masters.
 */
@Test
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
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testLoadingFileWithNoSchemaVersionFails() {
    String fileLocation = "src/test/resources/xml_portfolios/empty_portfolio_no_version";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();
  }

  @Test
  public void testEmptyPortfolio() {

    // We should get a default portfolio and position automatically generated for the trades
    String fileLocation = "src/test/resources/xml_portfolios/empty_portfolio.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 0);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 0);
  }

  @Test
  public void testMissingReference() {

    String fileLocation = "src/test/resources/xml_portfolios/missing_trade_reference.xml";
    File file = new File(fileLocation);
    try {
      new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();
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
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 2);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 2);
  }

  @Test
  public void testSinglePortfolioSinglePositionSingleIrs() {

    String fileLocation = "src/test/resources/xml_portfolios/single_irs.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  @Test
  public void testSinglePortfolioSinglePositionSingleFxOption() {

    String fileLocation = "src/test/resources/xml_portfolios/single_fx_option.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  @Test
  public void testSinglePortfolioNoPositionSingleFxOption() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/single_fx_option_no_position.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

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

  @Test
  public void testSinglePortfolioNoPositionSingleFxDigitalOption() {

    // We should get a position automatically generated for the trade
    String fileLocation = "src/test/resources/xml_portfolios/fx_digital_option_no_position.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 1);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 1);
  }

  @Test
  public void testSinglePortfolioNoPositionMultipleFxOption() {

    // We should get a position automatically generated for the trades
    String fileLocation = "src/test/resources/xml_portfolios/double_fx_option_no_position.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 2);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 2);
  }

  @Test
  public void testSinglePortfolioSinglePositionMultipleIrs() {

    String fileLocation = "src/test/resources/xml_portfolios/triple_irs.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), 1);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 3);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 3);
  }

  @Test
  public void testNestedPortfolios() {
    String fileLocation = "src/test/resources/xml_portfolios/nested_portfolios.xml";
    File file = new File(fileLocation);
    new PortfolioLoader(_toolContext, "guff", null, file.getAbsolutePath(), true, true, false, false, false, true).execute();

    List<ManageablePortfolio> portfolios = _portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios();
    assertEquals(portfolios.size(), 1);

    assertEquals(portfolios.get(0).getRootNode().getChildNodes().size(), 2);

    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), 4);
    assertEquals(_securityMaster.search(new SecuritySearchRequest()).getSecurities().size(), 4);
  }
}
