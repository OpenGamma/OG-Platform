/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.engine.analytics.AbstractAnalyticValue;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.AnalyticValueDefinitionImpl;
import com.opengamma.engine.analytics.InMemoryAnalyticFunctionRepository;
import com.opengamma.engine.analytics.yc.DiscountCurveAnalyticFunction;
import com.opengamma.engine.analytics.yc.DiscountCurveDefinition;
import com.opengamma.engine.analytics.yc.FixedIncomeStrip;
import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.position.csv.CSVPositionMaster;
import com.opengamma.engine.security.InMemorySecurityMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityIdentificationDomain;
import com.opengamma.engine.security.SecurityIdentifier;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.securities.Currency;
import com.opengamma.util.Pair;
import com.opengamma.util.TerminatableJob;

/**
 * 
 *
 * @author kirk
 */
public class ViewImplTest {
  private static final double ONEYEAR = 365.25;
  
  protected ViewImpl constructTrivialExampleView() throws Exception {
    ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("Kirk", "KirkPortfolio");
    viewDefinition.addValueDefinition("KIRK", DiscountCurveAnalyticFunction.constructDiscountCurveValueDefinition(Currency.getInstance("USD")));
    final Portfolio portfolio = CSVPositionMaster.loadPortfolio("KirkPortfolio", getClass().getResourceAsStream("KirkPortfolio.txt"));
    PositionMaster positionMaster = new PositionMaster() {
      @Override
      public Portfolio getRootPortfolio(String portfolioName) {
        return portfolio;
      }

      @Override
      public Collection<String> getRootPortfolioNames() {
        return Collections.singleton(portfolio.getName());
      }
    };
    Security security = new Security() {
      @Override
      public Collection<SecurityIdentifier> getIdentifiers() {
        return Collections.singleton(new SecurityIdentifier(new SecurityIdentificationDomain("KIRK"), "ID1"));
      }
      @Override
      public String getSecurityType() {
        return "KIRK";
      }
    };
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    secMaster.add(security);

    DiscountCurveDefinition curveDefinition = constructDiscountCurveDefinition("USD", "Stupidly Lame");
    DiscountCurveAnalyticFunction function = new DiscountCurveAnalyticFunction(curveDefinition);
    InMemoryAnalyticFunctionRepository functionRepo = new InMemoryAnalyticFunctionRepository(Collections.singleton(function));
    
    FixedLiveDataAvailabilityProvider ldap = new FixedLiveDataAvailabilityProvider();
    for(AnalyticValueDefinition<?> definition : function.getInputs(security)) {
      ldap.addDefinition(definition);
    }
    
    ViewComputationCacheFactory cacheFactory = new ViewComputationCacheFactory() {
      @Override
      public ViewComputationCache generateCache() {
        return new MapViewComputationCache();
      }
    };
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    populateSnapshot(snapshotProvider, curveDefinition, false);
    
    ViewImpl view = new ViewImpl(viewDefinition);
    view.setPositionMaster(positionMaster);
    view.setAnalyticFunctionRepository(functionRepo);
    view.setLiveDataAvailabilityProvider(ldap);
    view.setSecurityMaster(secMaster);
    view.setComputationCacheFactory(cacheFactory);
    view.setLiveDataSnapshotProvider(snapshotProvider);
    
    return view;
  }
  
  @Test
  public void trivialExampleSingleCycle() throws Exception {
    ViewImpl view = constructTrivialExampleView();
    view.init();
    assertEquals(ViewCalculationState.NOT_STARTED, view.getCalculationState());
    view.runOneCycle();
    ViewComputationResultModel result = view.getMostRecentResult();
    assertNotNull(result);
    Collection<Position> positions = result.getPositions();
    assertNotNull(positions);
    assertEquals(1, positions.size());
    Position resultPosition = positions.iterator().next();
    assertNotNull(resultPosition);
    assertEquals(new BigDecimal(9873), resultPosition.getQuantity());
    
    Map<AnalyticValueDefinition<?>, AnalyticValue<?>> resultValues = result.getValues(resultPosition);
    assertNotNull(resultValues);
    assertEquals(1, resultValues.size());
    AnalyticValueDefinition<?> discountCurveValueDefinition = DiscountCurveAnalyticFunction.constructDiscountCurveValueDefinition(Currency.getInstance("USD"));
    AnalyticValue<?> discountCurveValue = resultValues.get(discountCurveValueDefinition);
    assertNotNull(discountCurveValue);
    assertEquals(discountCurveValueDefinition, discountCurveValue.getDefinition());
    assertNotNull(discountCurveValue.getValue());
    assertTrue(discountCurveValue.getValue() instanceof DiscountCurve);
    DiscountCurve theCurveItself = (DiscountCurve) discountCurveValue.getValue();
    System.out.println("Discount Curve is " + theCurveItself.getData());
  }

  @Test
  public void trivialExamplePerturbingCycles() throws Exception {
    ViewImpl view = constructTrivialExampleView();
    DiscountCurveDefinition curveDefinition = constructDiscountCurveDefinition("USD", "Stupidly Lame");
    view.init();
    
    view.addResultListener(new ComputationResultListener() {
      @Override
      public void computationResultAvailable(
          ViewComputationResultModel resultModel) {
        Collection<Position> positions = resultModel.getPositions();
        Position resultPosition = positions.iterator().next();
        Map<AnalyticValueDefinition<?>, AnalyticValue<?>> resultValues = resultModel.getValues(resultPosition);
        AnalyticValue<?> discountCurveValue = resultValues.get(DiscountCurveAnalyticFunction.constructDiscountCurveValueDefinition(Currency.getInstance("USD")));
        DiscountCurve theCurveItself = (DiscountCurve) discountCurveValue.getValue();
        System.out.println("Discount Curve is " + theCurveItself.getData());
      }
    });
    
    view.start();
    InMemoryLKVSnapshotProvider snapshotProvider = (InMemoryLKVSnapshotProvider) view.getLiveDataSnapshotProvider();
    SnapshotPopulatorJob popJob = new SnapshotPopulatorJob(snapshotProvider, curveDefinition);
    Thread popThread = new Thread(popJob);
    popThread.start();
    
    Thread.sleep(10000l);
    view.stop();
    popJob.terminate();
    popThread.join();
  }
  
  private class SnapshotPopulatorJob extends TerminatableJob {
    private final InMemoryLKVSnapshotProvider _snapshotProvider;
    private final DiscountCurveDefinition _curveDefinition;
    
    public SnapshotPopulatorJob(InMemoryLKVSnapshotProvider snapshotProvider,
        DiscountCurveDefinition curveDefinition) {
      _snapshotProvider = snapshotProvider;
      _curveDefinition = curveDefinition;
    }
    
    @Override
    protected void runOneCycle() {
      populateSnapshot(_snapshotProvider, _curveDefinition, true);
      try {
        Thread.sleep(10l);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    
  }
  
  public static AnalyticValueDefinition<?> constructBloombergTickerDefinition(String bbTicker) {
    @SuppressWarnings("unchecked")
    AnalyticValueDefinitionImpl<Map<String, Double>> definition = new AnalyticValueDefinitionImpl<Map<String, Double>>(
        new Pair<String, Object>("DATA_SOURCE", "BLOOMBERG"),
        new Pair<String, Object>("TYPE", "MARKET_DATA_HEADER"),
        new Pair<String, Object>("BB_TICKER", bbTicker)
        );
    return definition;
  }
  
  public static DiscountCurveDefinition constructDiscountCurveDefinition(String isoCode, String name) {
    DiscountCurveDefinition defn = new DiscountCurveDefinition(Currency.getInstance(isoCode), name);
    defn.addStrip(new FixedIncomeStrip(1/ONEYEAR, constructBloombergTickerDefinition("US1D")));
    defn.addStrip(new FixedIncomeStrip(2/ONEYEAR, constructBloombergTickerDefinition("US2D")));
    defn.addStrip(new FixedIncomeStrip(7/ONEYEAR, constructBloombergTickerDefinition("US7D")));
    defn.addStrip(new FixedIncomeStrip(1/12.0, constructBloombergTickerDefinition("US1M")));
    defn.addStrip(new FixedIncomeStrip(0.25, constructBloombergTickerDefinition("US3M")));
    defn.addStrip(new FixedIncomeStrip(0.5, constructBloombergTickerDefinition("US6M")));

    defn.addStrip(new FixedIncomeStrip(1.0, constructBloombergTickerDefinition("USSW1")));
    defn.addStrip(new FixedIncomeStrip(2.0, constructBloombergTickerDefinition("USSW2")));
    defn.addStrip(new FixedIncomeStrip(3.0, constructBloombergTickerDefinition("USSW3")));
    defn.addStrip(new FixedIncomeStrip(4.0, constructBloombergTickerDefinition("USSW4")));
    defn.addStrip(new FixedIncomeStrip(5.0, constructBloombergTickerDefinition("USSW5")));
    defn.addStrip(new FixedIncomeStrip(6.0, constructBloombergTickerDefinition("USSW6")));
    defn.addStrip(new FixedIncomeStrip(7.0, constructBloombergTickerDefinition("USSW7")));
    defn.addStrip(new FixedIncomeStrip(8.0, constructBloombergTickerDefinition("USSW8")));
    defn.addStrip(new FixedIncomeStrip(9.0, constructBloombergTickerDefinition("USSW9")));
    defn.addStrip(new FixedIncomeStrip(10.0, constructBloombergTickerDefinition("USSW10")));
    return defn;
  }

  /**
   * @param function 
   * @param snapshotProvider 
   * 
   */
  private void populateSnapshot(
      InMemoryLKVSnapshotProvider snapshotProvider,
      DiscountCurveDefinition curveDefinition,
      boolean addRandom) {
    // Inflection point is 10.
    double currValue = 0.005;
    for(FixedIncomeStrip strip : curveDefinition.getStrips()) {
      if(addRandom) {
        currValue += (Math.random() * 0.010);
      }
      final Map<String, Double> dataFields = new HashMap<String, Double>();
      dataFields.put(DiscountCurveAnalyticFunction.PRICE_FIELD_NAME, currValue);
      @SuppressWarnings("unchecked")
      AnalyticValue value = new AbstractAnalyticValue(strip.getStripValueDefinition(), dataFields) {
        @Override
        public AnalyticValue<Map<String, Double>> scaleForPosition(BigDecimal quantity) {
          return this;
        }
      };
      snapshotProvider.addValue(value);
      if(strip.getNumYears() <= 5.0) {
        currValue += 0.005;
      } else {
        currValue -= 0.001;
      }
    }
  }
}
