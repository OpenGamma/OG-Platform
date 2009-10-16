/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.AnalyticValueImpl;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.AnalyticValueDefinitionImpl;
import com.opengamma.engine.analytics.InMemoryAnalyticFunctionRepository;
import com.opengamma.engine.analytics.MarketDataAnalyticValue;
import com.opengamma.engine.analytics.yc.DiscountCurveAnalyticFunction;
import com.opengamma.engine.analytics.yc.DiscountCurveDefinition;
import com.opengamma.engine.analytics.yc.FixedIncomeStrip;
import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.position.csv.CSVPositionMaster;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.InMemorySecurityMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.view.calcnode.LinkedBlockingCompletionQueue;
import com.opengamma.engine.view.calcnode.LinkedBlockingJobQueue;
import com.opengamma.engine.view.calcnode.SingleThreadCalculationNode;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.securities.Currency;
import com.opengamma.fudge.FudgeMsg;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.util.TerminatableJob;

/**
 * 
 *
 * @author kirk
 */
public class ViewImplTest {
  private static final double ONEYEAR = 365.25;
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(ViewImplTest.class);
  
  private SingleThreadCalculationNode _calcNode;
  
  @After
  public void shutDownCalcNode() {
    if(_calcNode != null) {
      _calcNode.stop();
      _calcNode = null;
    }
  }
  
  protected ViewImpl constructTrivialExampleView() throws Exception {
    ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("Kirk", "KirkPortfolio");
    //viewDefinition.addValueDefinition("KIRK", DiscountCurveAnalyticFunction.constructDiscountCurveValueDefinition(Currency.getInstance("USD")));
    viewDefinition.addValueDefinition("KIRK", DiscountCurveAnalyticFunction.constructDiscountCurveValueDefinition(null));
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
    Security security = new DefaultSecurity("KIRK", Collections.singleton(new DomainSpecificIdentifier(new IdentificationDomain("KIRK"), "ID1")));
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    secMaster.add(security);

    DiscountCurveDefinition curveDefinition = constructDiscountCurveDefinition("USD", "Stupidly Lame");
    DiscountCurveAnalyticFunction function = new DiscountCurveAnalyticFunction(curveDefinition);
    InMemoryAnalyticFunctionRepository functionRepo = new InMemoryAnalyticFunctionRepository();
    functionRepo.addFunction(function, function);
    
    FixedLiveDataAvailabilityProvider ldap = new FixedLiveDataAvailabilityProvider();
    for(AnalyticValueDefinition<?> definition : function.getInputs()) {
      ldap.addDefinition(definition);
    }
    
    ViewComputationCacheSource cacheFactory = new MapViewComputationCacheSource();
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    populateSnapshot(snapshotProvider, curveDefinition, false);
    
    LinkedBlockingJobQueue jobQueue = new LinkedBlockingJobQueue();
    LinkedBlockingCompletionQueue completionQueue = new LinkedBlockingCompletionQueue();
    _calcNode = new SingleThreadCalculationNode(cacheFactory, functionRepo, secMaster, jobQueue, completionQueue);
    _calcNode.start();
    
    ViewProcessingContext processingContext = new ViewProcessingContext(
        ldap, snapshotProvider, functionRepo, positionMaster, secMaster, cacheFactory,
        jobQueue, completionQueue
      );
    
    ViewImpl view = new ViewImpl(viewDefinition, processingContext);
    view.setComputationExecutorService(Executors.newSingleThreadExecutor());
    
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
    
    assertNotNull(result.getValue(resultPosition, DiscountCurveAnalyticFunction.constructDiscountCurveValueDefinition(Currency.getInstance("USD"))));
    assertNull(result.getValue(resultPosition, DiscountCurveAnalyticFunction.constructDiscountCurveValueDefinition(Currency.getInstance("GBP"))));
    assertNotNull(result.getValue(resultPosition, DiscountCurveAnalyticFunction.constructDiscountCurveValueDefinition(null)));
    
    Map<AnalyticValueDefinition<?>, AnalyticValue<?>> resultValues = null;
    resultValues = result.getValues(resultPosition);
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
    InMemoryLKVSnapshotProvider snapshotProvider = (InMemoryLKVSnapshotProvider) view.getProcessingContext().getLiveDataSnapshotProvider();
    SnapshotPopulatorJob popJob = new SnapshotPopulatorJob(snapshotProvider, curveDefinition);
    Thread popThread = new Thread(popJob);
    popThread.start();
    
    Thread.sleep(10000l);
    view.stop();
    popJob.terminate();
    popThread.join();
  }
  
  @Test
  public void trivialExampleNoPerturbingDeltasEmpty() throws Exception {
    ViewImpl view = constructTrivialExampleView();
    view.init();

    final AtomicBoolean failed = new AtomicBoolean(false);
    view.addDeltaResultListener(new DeltaComputationResultListener() {
      @Override
      public void deltaResultAvailable(ViewDeltaResultModel deltaModel) {
        try {
          assertTrue(deltaModel.getNewPositions().isEmpty());
          assertTrue(deltaModel.getRemovedPositions().isEmpty());
          assertFalse(deltaModel.getAllPositions().isEmpty());
          assertTrue(deltaModel.getPositionsWithDeltas().isEmpty());
          Position position = deltaModel.getAllPositions().iterator().next();
          assertTrue(deltaModel.getDeltaValues(position).isEmpty());
        } catch (RuntimeException e) {
          e.printStackTrace();
          failed.set(true);
        }
      }
    });

    view.start();
    Thread.sleep(5000l);
    view.stop();
    assertFalse("Failed somewhere in listener. Check logs.", failed.get());
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
  
  @SuppressWarnings("unchecked")
  public AnalyticValueDefinition<?> constructBloombergTickerDefinition(String bbTicker) {
    return new AnalyticValueDefinitionImpl("BbgId", bbTicker);
  }
  
  public DiscountCurveDefinition constructDiscountCurveDefinition(String isoCode, String name) {
    DiscountCurveDefinition defn = new DiscountCurveDefinition(Currency.getInstance(isoCode), name);
    defn.addStrip(new FixedIncomeStrip(1/ONEYEAR, constructBloombergTickerDefinition("US00O/N Index")));
    defn.addStrip(new FixedIncomeStrip(7/ONEYEAR, constructBloombergTickerDefinition("US0001W Index")));
    defn.addStrip(new FixedIncomeStrip(14/ONEYEAR, constructBloombergTickerDefinition("US0002W Index")));
    defn.addStrip(new FixedIncomeStrip(1/12.0, constructBloombergTickerDefinition("US0001M Index")));
    defn.addStrip(new FixedIncomeStrip(0.25, constructBloombergTickerDefinition("US0003M Index")));
    defn.addStrip(new FixedIncomeStrip(0.5, constructBloombergTickerDefinition("US0006M Index")));

    defn.addStrip(new FixedIncomeStrip(1.0, constructBloombergTickerDefinition("USSW1 Curncy")));
    defn.addStrip(new FixedIncomeStrip(2.0, constructBloombergTickerDefinition("USSW2 Curncy")));
    defn.addStrip(new FixedIncomeStrip(3.0, constructBloombergTickerDefinition("USSW3 Curncy")));
    defn.addStrip(new FixedIncomeStrip(4.0, constructBloombergTickerDefinition("USSW4 Curncy")));
    defn.addStrip(new FixedIncomeStrip(5.0, constructBloombergTickerDefinition("USSW5 Curncy")));
    defn.addStrip(new FixedIncomeStrip(6.0, constructBloombergTickerDefinition("USSW6 Curncy")));
    defn.addStrip(new FixedIncomeStrip(7.0, constructBloombergTickerDefinition("USSW7 Curncy")));
    defn.addStrip(new FixedIncomeStrip(8.0, constructBloombergTickerDefinition("USSW8 Curncy")));
    defn.addStrip(new FixedIncomeStrip(9.0, constructBloombergTickerDefinition("USSW9 Curncy")));
    defn.addStrip(new FixedIncomeStrip(10.0, constructBloombergTickerDefinition("USSW10 Curncy")));
    return defn;
  }

  /**
   * @param function 
   * @param snapshotProvider 
   * 
   */
  protected void populateSnapshot(
      InMemoryLKVSnapshotProvider snapshotProvider,
      DiscountCurveDefinition curveDefinition,
      boolean addRandom) {
    // Inflection point is 10.
    double currValue = 0.005;
    for(FixedIncomeStrip strip : curveDefinition.getStrips()) {
      if(addRandom) {
        currValue += (Math.random() * 0.010);
      }
      final FudgeMsg dataFields = new FudgeMsg();
      dataFields.add(MarketDataAnalyticValue.INDICATIVE_VALUE_NAME, currValue);
      @SuppressWarnings("unchecked")
      AnalyticValue value = new AnalyticValueImpl(strip.getStripValueDefinition(), dataFields);
      snapshotProvider.addValue(value);
      if(strip.getNumYears() <= 5.0) {
        currValue += 0.005;
      } else {
        currValue -= 0.001;
      }
    }
  }
}
