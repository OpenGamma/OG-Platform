/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test aggregation.
 */
@Test(groups = TestGroup.UNIT)
public class AggregationTest {
//  private static final Logger s_logger = LoggerFactory.getLogger(AggregationTest.class);
//  private List<Position> _equities;
//  private List<Position> _americanOptions;
//  private List<Position> _europeanOptions;
//  private List<Position> _allOptions;
//  private ArrayList<Position> _usd;
//  private ArrayList<Position> _gbp;
//  private List<Position> _gbpEquities;
//  private ArrayList<Position> _usdEquities;
  
  // TODO kirk 2009-11-03 -- Bring this up to date with the new security identity keys.

  /*
  public Portfolio makeTestPortfolio() {
    Expiry expiry = new Expiry(ZonedDateTime.fromInstant(Clock.systemUTC().instant(), ZoneOffset.UTC));
    
    EquitySecurity aaplSec = new EquitySecurity("AAPL US", "BLOOMBERG");
    aaplSec.setCurrency(Currency.getInstance("USD"));
    
    AmericanVanillaEquityOptionSecurity aaplOption1 = new AmericanVanillaEquityOptionSecurity(OptionType.CALL, 190, expiry, aaplSec.getIdentityKey(), Currency.getInstance("USD"));
    AmericanVanillaEquityOptionSecurity aaplOption2 = new AmericanVanillaEquityOptionSecurity(OptionType.PUT, 195, expiry, aaplSec.getIdentityKey(), Currency.getInstance("USD"));
    AmericanVanillaEquityOptionSecurity aaplOption3 = new AmericanVanillaEquityOptionSecurity(OptionType.CALL, 200, expiry, aaplSec.getIdentityKey(), Currency.getInstance("USD"));
    AmericanVanillaEquityOptionSecurity aaplOption4 = new AmericanVanillaEquityOptionSecurity(OptionType.PUT, 205, expiry, aaplSec.getIdentityKey(), Currency.getInstance("USD"));
    AmericanVanillaEquityOptionSecurity aaplOption5 = new AmericanVanillaEquityOptionSecurity(OptionType.CALL, 210, expiry, aaplSec.getIdentityKey(), Currency.getInstance("USD"));
    
    EquitySecurity vodafSec = new EquitySecurity("VODAF", "BLOOMBERG");
    vodafSec.setCurrency(Currency.getInstance("GBP"));
    
    EuropeanVanillaEquityOptionSecurity vodafOption1 = new EuropeanVanillaEquityOptionSecurity(OptionType.PUT, 105, expiry, vodafSec.getIdentityKey(), Currency.getInstance("GBP"));
    EuropeanVanillaEquityOptionSecurity vodafOption2 = new EuropeanVanillaEquityOptionSecurity(OptionType.CALL, 110, expiry, vodafSec.getIdentityKey(), Currency.getInstance("GBP"));
    EuropeanVanillaEquityOptionSecurity vodafOption3 = new EuropeanVanillaEquityOptionSecurity(OptionType.PUT, 115, expiry, vodafSec.getIdentityKey(), Currency.getInstance("GBP"));
    EuropeanVanillaEquityOptionSecurity vodafOption4 = new EuropeanVanillaEquityOptionSecurity(OptionType.CALL, 120, expiry, vodafSec.getIdentityKey(), Currency.getInstance("GBP"));
    
    EquitySecurity ibmSec = new EquitySecurity("IBM US", "BLOOMBERG");
    ibmSec.setCurrency(Currency.getInstance("USD"));
    
    Position aaplPos = new PositionBean(new BigDecimal(2000), aaplSec.getIdentityKey(), aaplSec);
    Position aaplOption1Pos = new PositionBean(new BigDecimal(5000), aaplOption1.getIdentityKey(), aaplOption1);
    Position aaplOption2Pos = new PositionBean(new BigDecimal(5000), aaplOption2.getIdentityKey(), aaplOption2);
    Position aaplOption3Pos = new PositionBean(new BigDecimal(5000), aaplOption3.getIdentityKey(), aaplOption3);
    Position aaplOption4Pos = new PositionBean(new BigDecimal(5000), aaplOption4.getIdentityKey(), aaplOption4);
    Position aaplOption5Pos = new PositionBean(new BigDecimal(5000), aaplOption5.getIdentityKey(), aaplOption5);
    
    Position vodafPos = new PositionBean(new BigDecimal(-1000), vodafSec.getIdentityKey(), vodafSec);
    Position vodafOption1Pos = new PositionBean(new BigDecimal(5000), vodafOption1.getIdentityKey(), vodafOption1);
    Position vodafOption2Pos = new PositionBean(new BigDecimal(5000), vodafOption2.getIdentityKey(), vodafOption2);
    Position vodafOption3Pos = new PositionBean(new BigDecimal(5000), vodafOption3.getIdentityKey(), vodafOption3);
    Position vodafOption4Pos = new PositionBean(new BigDecimal(5000), vodafOption4.getIdentityKey(), vodafOption4);
    
    Position ibmPos = new PositionBean(new BigDecimal(4000), ibmSec.getIdentityKey(), ibmSec);
    
    PortfolioImpl fullyPopulatedPortfolio = new PortfolioImpl("Test Portfolio");
    
    PortfolioNodeImpl callNode = new PortfolioNodeImpl("calls");
    PortfolioNodeImpl putNode = new PortfolioNodeImpl("puts");
    PortfolioNodeImpl ukCallNode = new PortfolioNodeImpl("uk calls");
    PortfolioNodeImpl usCallNode = new PortfolioNodeImpl("us calls");
    
    fullyPopulatedPortfolio.addPosition(ibmPos);
    fullyPopulatedPortfolio.addSubNode(putNode);
    fullyPopulatedPortfolio.addSubNode(callNode);
    callNode.addSubNode(ukCallNode);
    callNode.addSubNode(usCallNode);
    
    Position[] putList = new Position[] { aaplOption2Pos, aaplOption4Pos, vodafOption1Pos, vodafOption3Pos };
    Position[] ukCallList = new Position[] { vodafOption2Pos, vodafOption4Pos };
    Position[] usCallList = new Position[] { aaplOption1Pos, aaplOption3Pos, aaplOption5Pos };
    
    _equities = Arrays.asList(new Position[] { aaplPos, vodafPos, ibmPos });
    _americanOptions = Arrays.asList(new Position[] { aaplOption1Pos, aaplOption2Pos, aaplOption3Pos, aaplOption4Pos, aaplOption5Pos });
    _europeanOptions = Arrays.asList(new Position[] { vodafOption1Pos, vodafOption2Pos, vodafOption3Pos, vodafOption4Pos });
    _allOptions = new ArrayList<Position>(_americanOptions);
    _allOptions.addAll(_europeanOptions);
    _usd = new ArrayList<Position>(_americanOptions);
    _usd.add(ibmPos);
    _usd.add(aaplPos);
    _gbp = new ArrayList<Position>(_europeanOptions);
    _gbp.add(vodafPos);
    _gbpEquities = Collections.singletonList(vodafPos);
    _usdEquities = new ArrayList<Position>();
    _usdEquities.add(aaplPos);
    _usdEquities.add(ibmPos);
    
    for (Position position : putList) {
      putNode.addPosition(position);
    }
    putNode.addPosition(aaplPos);
    
    for (Position position : ukCallList) {
      ukCallNode.addPosition(position);
    }
    ukCallNode.addPosition(vodafPos);
    
    for (Position position : usCallList) {
      usCallNode.addPosition(position);
    }
    return fullyPopulatedPortfolio;
  }
  
  @Test
  public void testDetailedAssetClassAggregation() {
    Portfolio testPortfolio = makeTestPortfolio();
    PortfolioAggregator aggregator = new PortfolioAggregator(new DetailedAssetClassAggregationFunction());
    PortfolioNode aggregatedPortfolio = aggregator.aggregate(testPortfolio);
    Assert.assertEquals(0, aggregatedPortfolio.getPositions().size());
    Assert.assertEquals(3, aggregatedPortfolio.getSubNodes().size());
    for (PortfolioNode node : aggregatedPortfolio.getSubNodes()) {
      Assert.assertEquals(0, node.getSubNodes().size());
      if (node.getName().endsWith(DetailedAssetClassAggregationFunction.EQUITIES)) {
        Assert.assertTrue(_equities.containsAll(node.getPositions()));
        Assert.assertTrue(node.getPositions().containsAll(_equities));
      } else if (node.getName().endsWith(DetailedAssetClassAggregationFunction.AMERICAN_VANILLA_EQUITY_OPTIONS)) {
        Assert.assertTrue(_americanOptions.containsAll(node.getPositions()));
        Assert.assertTrue(node.getPositions().containsAll(_americanOptions));
      } else if (node.getName().endsWith(DetailedAssetClassAggregationFunction.EUROPEAN_VANILLA_EQUITY_OPTIONS)) {
        Assert.assertTrue(_europeanOptions.containsAll(node.getPositions()));
        Assert.assertTrue(node.getPositions().containsAll(_europeanOptions));
      } else {
        Assert.fail();
      }
    }
  }
  
  @Test
  public void testAssetClassAggregation() {
    Portfolio testPortfolio = makeTestPortfolio();
    PortfolioAggregator aggregator = new PortfolioAggregator(new AssetClassAggregationFunction());
    PortfolioNode aggregatedPortfolio = aggregator.aggregate(testPortfolio);
    Assert.assertEquals(0, aggregatedPortfolio.getPositions().size());
    Assert.assertEquals(2, aggregatedPortfolio.getSubNodes().size());
    int total = 0; // this makes sure both branches are visited only once.
    for (PortfolioNode node : aggregatedPortfolio.getSubNodes()) {
      Assert.assertEquals(0, node.getSubNodes().size());
      if (node.getName().endsWith(AssetClassAggregationFunction.EQUITIES)) {
        Assert.assertTrue(_equities.containsAll(node.getPositions()));
        Assert.assertTrue(node.getPositions().containsAll(_equities));
        total += 10;
      } else if (node.getName().endsWith(AssetClassAggregationFunction.EQUITY_OPTIONS)) {
        Assert.assertTrue(_allOptions.containsAll(node.getPositions()));
        Assert.assertTrue(node.getPositions().containsAll(_allOptions));
        total += 1;
      } else {
        Assert.fail();
      }
    }
    Assert.assertEquals(11, total);
  }
  
  @Test
  public void testCurrencyAggregation() {
    Portfolio testPortfolio = makeTestPortfolio();
    PortfolioAggregator aggregator = new PortfolioAggregator(new CurrencyAggregationFunction());
    PortfolioNode aggregatedPortfolio = aggregator.aggregate(testPortfolio);
    Assert.assertEquals(0, aggregatedPortfolio.getPositions().size());
    Assert.assertEquals(2, aggregatedPortfolio.getSubNodes().size());
    int total = 0; // this makes sure both branches are visited only once.
    for (PortfolioNode node : aggregatedPortfolio.getSubNodes()) {
      Assert.assertEquals(0, node.getSubNodes().size());
      if (node.getName().contains("GBP")) {
        Assert.assertTrue(_gbp.containsAll(node.getPositions()));
        Assert.assertTrue(node.getPositions().containsAll(_gbp));
        total += 10;
      } else if (node.getName().contains("USD")) {
        Assert.assertTrue(_usd.containsAll(node.getPositions()));
        Assert.assertTrue(node.getPositions().containsAll(_usd));
        total += 1;
      } else {
        Assert.fail();
      }
    }  
    Assert.assertEquals(11, total);
  }
  
  @Test
  public void testMultiLevelAggregation() {
    s_logger.info("Starting testMultiLevelAggregation()");
    Portfolio testPortfolio = makeTestPortfolio();
    PortfolioAggregator aggregator = new PortfolioAggregator(new AssetClassAggregationFunction(), new CurrencyAggregationFunction());
    PortfolioNode aggregatedPortfolio = aggregator.aggregate(testPortfolio);
    Assert.assertEquals(0, aggregatedPortfolio.getPositions().size());
    Assert.assertEquals(2, aggregatedPortfolio.getSubNodes().size());
    int total = 0; // make sure both branches are visited only once.
    for (PortfolioNode node : aggregatedPortfolio.getSubNodes()) {
      Assert.assertEquals(0, node.getPositions().size());
      if (node.getName().endsWith(AssetClassAggregationFunction.EQUITIES)) {
        int subTotal = 0; // this makes sure both branches are visited only once.
        for (PortfolioNode subNode : node.getSubNodes()) {
          Assert.assertEquals(0, subNode.getSubNodes().size());
          if (subNode.getName().contains("GBP")) {
            System.err.println(node.getPositions());
            Assert.assertTrue(_gbpEquities.containsAll(subNode.getPositions()));
            Assert.assertTrue(subNode.getPositions().containsAll(_gbpEquities));
            subTotal += 10;
          } else if (subNode.getName().contains("USD")) {
            Assert.assertTrue(_usdEquities.containsAll(subNode.getPositions()));
            Assert.assertTrue(subNode.getPositions().containsAll(_usdEquities));
            subTotal += 1;
          } else {
            Assert.fail();
          }
        }
        Assert.assertEquals(11, subTotal);
        total += 10;
      } else if (node.getName().endsWith(AssetClassAggregationFunction.EQUITY_OPTIONS)) {
        int subTotal = 0; // this makes sure both branches are visited only once.
        for (PortfolioNode subNode : node.getSubNodes()) {
          Assert.assertEquals(0, subNode.getSubNodes().size());
          if (subNode.getName().contains("GBP")) {
            Assert.assertTrue(_europeanOptions.containsAll(subNode.getPositions()));
            Assert.assertTrue(subNode.getPositions().containsAll(_europeanOptions));
            subTotal += 10;
          } else if (subNode.getName().contains("USD")) {
            Assert.assertTrue(_americanOptions.containsAll(subNode.getPositions()));
            Assert.assertTrue(subNode.getPositions().containsAll(_americanOptions));
            subTotal += 1;
          } else {
            Assert.fail();
          }
        }
        Assert.assertEquals(11, subTotal);
        total += 1;
      } else {
        Assert.fail();
      }
    }    
    Assert.assertEquals(11, total);
  }
  */
  
  @Test
  public void testNothing() {
    // Just here to stop the failure temporarily.
  }
}
