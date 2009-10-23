/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.position.FullyPopulatedPortfolio;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionBean;
import com.opengamma.engine.view.FullyPopulatedPortfolioNode;
import com.opengamma.engine.view.FullyPopulatedPosition;
import com.opengamma.financial.Currency;
import com.opengamma.financial.security.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.financial.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.OptionType;
import com.opengamma.util.time.Expiry;


/**
 * 
 *
 * @author jim
 */
public class AggregationTest {
  private static final Logger s_logger = LoggerFactory.getLogger(AggregationTest.class);
  private List<FullyPopulatedPosition> _equities;
  private List<FullyPopulatedPosition> _americanOptions;
  private List<FullyPopulatedPosition> _europeanOptions;
  private List<FullyPopulatedPosition> _allOptions;
  private ArrayList<FullyPopulatedPosition> _usd;
  private ArrayList<FullyPopulatedPosition> _gbp;
  private List<FullyPopulatedPosition> _gbpEquities;
  private ArrayList<FullyPopulatedPosition> _usdEquities;

  public FullyPopulatedPortfolio makeTestPortfolio() {
    Expiry expiry = new Expiry(ZonedDateTime.fromInstant(Clock.system(TimeZone.UTC).instant(), TimeZone.UTC));
    
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
    
    FullyPopulatedPosition aaplPos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(2000), aaplSec.getIdentityKey()), aaplSec);
    FullyPopulatedPosition aaplOption1Pos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(5000), aaplOption1.getIdentityKey()), aaplOption1);
    FullyPopulatedPosition aaplOption2Pos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(5000), aaplOption2.getIdentityKey()), aaplOption2);
    FullyPopulatedPosition aaplOption3Pos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(5000), aaplOption3.getIdentityKey()), aaplOption3);
    FullyPopulatedPosition aaplOption4Pos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(5000), aaplOption4.getIdentityKey()), aaplOption4);
    FullyPopulatedPosition aaplOption5Pos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(5000), aaplOption5.getIdentityKey()), aaplOption5);
    
    FullyPopulatedPosition vodafPos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(-1000), vodafSec.getIdentityKey()), vodafSec);
    FullyPopulatedPosition vodafOption1Pos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(5000), vodafOption1.getIdentityKey()), vodafOption1);
    FullyPopulatedPosition vodafOption2Pos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(5000), vodafOption2.getIdentityKey()), vodafOption2);
    FullyPopulatedPosition vodafOption3Pos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(5000), vodafOption3.getIdentityKey()), vodafOption3);
    FullyPopulatedPosition vodafOption4Pos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(5000), vodafOption4.getIdentityKey()), vodafOption4);
    
    FullyPopulatedPosition ibmPos = new FullyPopulatedPosition(new PositionBean(new BigDecimal(4000), ibmSec.getIdentityKey()), ibmSec);
    
    FullyPopulatedPortfolio fullyPopulatedPortfolio = new FullyPopulatedPortfolio("Test Portfolio");
    
    FullyPopulatedPortfolioNode callNode = new FullyPopulatedPortfolioNode("calls");
    FullyPopulatedPortfolioNode putNode = new FullyPopulatedPortfolioNode("puts");
    FullyPopulatedPortfolioNode ukCallNode = new FullyPopulatedPortfolioNode("uk calls");
    FullyPopulatedPortfolioNode usCallNode = new FullyPopulatedPortfolioNode("us calls");
    
    fullyPopulatedPortfolio.addPosition(ibmPos.getPosition(), ibmPos.getSecurity());
    fullyPopulatedPortfolio.addSubNode(putNode);
    fullyPopulatedPortfolio.addSubNode(callNode);
    callNode.addSubNode(ukCallNode);
    callNode.addSubNode(usCallNode);
    
    FullyPopulatedPosition[] putList = new FullyPopulatedPosition[] { aaplOption2Pos, aaplOption4Pos, vodafOption1Pos, vodafOption3Pos };
    FullyPopulatedPosition[] ukCallList = new FullyPopulatedPosition[] { vodafOption2Pos, vodafOption4Pos };
    FullyPopulatedPosition[] usCallList = new FullyPopulatedPosition[] { aaplOption1Pos, aaplOption3Pos, aaplOption5Pos };
    
    _equities = Arrays.asList(new FullyPopulatedPosition[] { aaplPos, vodafPos, ibmPos });
    _americanOptions = Arrays.asList(new FullyPopulatedPosition[] { aaplOption1Pos, aaplOption2Pos, aaplOption3Pos, aaplOption4Pos, aaplOption5Pos });
    _europeanOptions = Arrays.asList(new FullyPopulatedPosition[] { vodafOption1Pos, vodafOption2Pos, vodafOption3Pos, vodafOption4Pos });
    _allOptions = new ArrayList<FullyPopulatedPosition>(_americanOptions);
    _allOptions.addAll(_europeanOptions);
    _usd = new ArrayList<FullyPopulatedPosition>(_americanOptions);
    _usd.add(ibmPos);
    _usd.add(aaplPos);
    _gbp = new ArrayList<FullyPopulatedPosition>(_europeanOptions);
    _gbp.add(vodafPos);
    _gbpEquities = Collections.singletonList(vodafPos);
    _usdEquities = new ArrayList<FullyPopulatedPosition>();
    _usdEquities.add(aaplPos);
    _usdEquities.add(ibmPos);
    
    for (FullyPopulatedPosition position : putList) {
      putNode.addPosition(position.getPosition(), position.getSecurity());
    }
    putNode.addPosition(aaplPos.getPosition(), aaplPos.getSecurity());
    
    for (FullyPopulatedPosition position : ukCallList) {
      ukCallNode.addPosition(position.getPosition(), position.getSecurity());
    }
    ukCallNode.addPosition(vodafPos.getPosition(), vodafPos.getSecurity());
    
    for (FullyPopulatedPosition position : usCallList) {
      usCallNode.addPosition(position.getPosition(), position.getSecurity());
    }
    return fullyPopulatedPortfolio;
  }
  
  @Test
  public void testDetailedAssetClassAggregation() {
    FullyPopulatedPortfolio testPortfolio = makeTestPortfolio();
    PortfolioAggregator aggregator = new PortfolioAggregator(new DetailedAssetClassAggregationFunction());
    FullyPopulatedPortfolioNode aggregatedPortfolio = aggregator.aggregate(testPortfolio);
    Assert.assertEquals(0, aggregatedPortfolio.getPositions().size());
    Assert.assertEquals(0, aggregatedPortfolio.getPopulatedPositions().size());
    Assert.assertEquals(3, aggregatedPortfolio.getPopulatedSubNodes().size());
    for (FullyPopulatedPortfolioNode node : aggregatedPortfolio.getPopulatedSubNodes()) {
      Assert.assertEquals(0, node.getPopulatedSubNodes().size());
      if (node.getName().endsWith(DetailedAssetClassAggregationFunction.EQUITIES)) {
        Assert.assertTrue(_equities.containsAll(node.getPopulatedPositions()));
        Assert.assertTrue(node.getPopulatedPositions().containsAll(_equities));
      } else if (node.getName().endsWith(DetailedAssetClassAggregationFunction.AMERICAN_VANILLA_EQUITY_OPTIONS)) {
        Assert.assertTrue(_americanOptions.containsAll(node.getPopulatedPositions()));
        Assert.assertTrue(node.getPopulatedPositions().containsAll(_americanOptions));
      } else if (node.getName().endsWith(DetailedAssetClassAggregationFunction.EUROPEAN_VANILLA_EQUITY_OPTIONS)) {
        Assert.assertTrue(_europeanOptions.containsAll(node.getPopulatedPositions()));
        Assert.assertTrue(node.getPopulatedPositions().containsAll(_europeanOptions));
      } else {
        Assert.fail();
      }
    }
  }
  
  @Test
  public void testAssetClassAggregation() {
    FullyPopulatedPortfolio testPortfolio = makeTestPortfolio();
    PortfolioAggregator aggregator = new PortfolioAggregator(new AssetClassAggregationFunction());
    FullyPopulatedPortfolioNode aggregatedPortfolio = aggregator.aggregate(testPortfolio);
    Assert.assertEquals(0, aggregatedPortfolio.getPositions().size());
    Assert.assertEquals(0, aggregatedPortfolio.getPopulatedPositions().size());
    Assert.assertEquals(2, aggregatedPortfolio.getPopulatedSubNodes().size());
    int total = 0; // this makes sure both branches are visited only once.
    for (FullyPopulatedPortfolioNode node : aggregatedPortfolio.getPopulatedSubNodes()) {
      Assert.assertEquals(0, node.getPopulatedSubNodes().size());
      if (node.getName().endsWith(AssetClassAggregationFunction.EQUITIES)) {
        Assert.assertTrue(_equities.containsAll(node.getPopulatedPositions()));
        Assert.assertTrue(node.getPopulatedPositions().containsAll(_equities));
        total += 10;
      } else if (node.getName().endsWith(AssetClassAggregationFunction.EQUITY_OPTIONS)) {
        Assert.assertTrue(_allOptions.containsAll(node.getPopulatedPositions()));
        Assert.assertTrue(node.getPopulatedPositions().containsAll(_allOptions));
        total += 1;
      } else {
        Assert.fail();
      }
    }
    Assert.assertEquals(11, total);
  }
  
  @Test
  public void testCurrencyAggregation() {
    FullyPopulatedPortfolio testPortfolio = makeTestPortfolio();
    PortfolioAggregator aggregator = new PortfolioAggregator(new CurrencyAggregationFunction());
    FullyPopulatedPortfolioNode aggregatedPortfolio = aggregator.aggregate(testPortfolio);
    Assert.assertEquals(0, aggregatedPortfolio.getPositions().size());
    Assert.assertEquals(0, aggregatedPortfolio.getPopulatedPositions().size());
    Assert.assertEquals(2, aggregatedPortfolio.getPopulatedSubNodes().size());
    int total = 0; // this makes sure both branches are visited only once.
    for (FullyPopulatedPortfolioNode node : aggregatedPortfolio.getPopulatedSubNodes()) {
      Assert.assertEquals(0, node.getPopulatedSubNodes().size());
      if (node.getName().contains("GBP")) {
        Assert.assertTrue(_gbp.containsAll(node.getPopulatedPositions()));
        Assert.assertTrue(node.getPopulatedPositions().containsAll(_gbp));
        total += 10;
      } else if (node.getName().contains("USD")) {
        Assert.assertTrue(_usd.containsAll(node.getPopulatedPositions()));
        Assert.assertTrue(node.getPopulatedPositions().containsAll(_usd));
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
    FullyPopulatedPortfolio testPortfolio = makeTestPortfolio();
    PortfolioAggregator aggregator = new PortfolioAggregator(new AssetClassAggregationFunction(), new CurrencyAggregationFunction());
    FullyPopulatedPortfolioNode aggregatedPortfolio = aggregator.aggregate(testPortfolio);
    Assert.assertEquals(0, aggregatedPortfolio.getPositions().size());
    Assert.assertEquals(0, aggregatedPortfolio.getPopulatedPositions().size());
    Assert.assertEquals(2, aggregatedPortfolio.getPopulatedSubNodes().size());
    int total = 0; // make sure both branches are visited only once.
    for (FullyPopulatedPortfolioNode node : aggregatedPortfolio.getPopulatedSubNodes()) {
      Assert.assertEquals(0, node.getPopulatedPositions().size());
      if (node.getName().endsWith(AssetClassAggregationFunction.EQUITIES)) {
        int subTotal = 0; // this makes sure both branches are visited only once.
        for (FullyPopulatedPortfolioNode subNode : node.getPopulatedSubNodes()) {
          Assert.assertEquals(0, subNode.getPopulatedSubNodes().size());
          if (subNode.getName().contains("GBP")) {
            System.err.println(node.getPopulatedPositions());
            Assert.assertTrue(_gbpEquities.containsAll(subNode.getPopulatedPositions()));
            Assert.assertTrue(subNode.getPopulatedPositions().containsAll(_gbpEquities));
            subTotal += 10;
          } else if (subNode.getName().contains("USD")) {
            Assert.assertTrue(_usdEquities.containsAll(subNode.getPopulatedPositions()));
            Assert.assertTrue(subNode.getPopulatedPositions().containsAll(_usdEquities));
            subTotal += 1;
          } else {
            Assert.fail();
          }
        }
        Assert.assertEquals(11, subTotal);
        total += 10;
      } else if (node.getName().endsWith(AssetClassAggregationFunction.EQUITY_OPTIONS)) {
        int subTotal = 0; // this makes sure both branches are visited only once.
        for (FullyPopulatedPortfolioNode subNode : node.getPopulatedSubNodes()) {
          Assert.assertEquals(0, subNode.getPopulatedSubNodes().size());
          if (subNode.getName().contains("GBP")) {
            Assert.assertTrue(_europeanOptions.containsAll(subNode.getPopulatedPositions()));
            Assert.assertTrue(subNode.getPopulatedPositions().containsAll(_europeanOptions));
            subTotal += 10;
          } else if (subNode.getName().contains("USD")) {
            Assert.assertTrue(_americanOptions.containsAll(subNode.getPopulatedPositions()));
            Assert.assertTrue(subNode.getPopulatedPositions().containsAll(_americanOptions));
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
}
