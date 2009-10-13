/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;

import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.analytics.AbstractAnalyticValue;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.DiscountCurveValueDefinition;
import com.opengamma.engine.analytics.GreeksAnalyticFunction;
import com.opengamma.engine.analytics.GreeksResultValueDefinition;
import com.opengamma.engine.analytics.HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction;
import com.opengamma.engine.analytics.InMemoryAnalyticFunctionRepository;
import com.opengamma.engine.analytics.MarketDataAnalyticValue;
import com.opengamma.engine.analytics.ResolveSecurityKeyToMarketDataHeaderDefinition;
import com.opengamma.engine.analytics.yc.DiscountCurveAnalyticFunction;
import com.opengamma.engine.analytics.yc.DiscountCurveDefinition;
import com.opengamma.engine.analytics.yc.FixedIncomeStrip;
import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.position.csv.CSVPositionMaster;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.EquitySecurity;
import com.opengamma.engine.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.engine.security.InMemorySecurityMaster;
import com.opengamma.engine.security.OptionType;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityKey;
import com.opengamma.engine.security.SecurityKeyImpl;
import com.opengamma.engine.view.MapViewComputationCacheSource;
import com.opengamma.engine.view.ViewComputationCacheSource;
import com.opengamma.engine.view.ViewDefinitionImpl;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.engine.view.ViewProcessingContext;
import com.opengamma.engine.view.calcnode.LinkedBlockingCompletionQueue;
import com.opengamma.engine.view.calcnode.LinkedBlockingJobQueue;
import com.opengamma.engine.view.calcnode.SingleThreadCalculationNode;
import com.opengamma.financial.securities.Currency;
import com.opengamma.fudge.FudgeMsg;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author jim
 */
public class ViewManager implements Lifecycle {
  private final Clock _clock = Clock.system(TimeZone.UTC);
  private static final double ONEYEAR = 365.25;
  private static final IdentificationDomain BLOOMBERG = new IdentificationDomain("BLOOMBERG");
  private final LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private final LiveDataSnapshotProvider _liveDataSnapshotProvider;
  private final ViewImpl _view;
  private List<SingleThreadCalculationNode> _calculationNodes;
  private SnapshotPopulatorJob _popJob;
  
  public ViewImpl getView() {
    return _view;
  }
  
  @Override
  public boolean isRunning() {
    return getView().isRunning();
  }
  
  @Override
  public void start() {
    for(SingleThreadCalculationNode calcNode: _calculationNodes) {
      calcNode.start();
    }
    getView().start();
  }
  
  @Override
  public void stop() {
    getView().stop();
    _popJob.terminate();
    for(SingleThreadCalculationNode calcNode : _calculationNodes) {
      calcNode.stop();
    }
    _calculationNodes = null;
  }
  
  public ViewManager(LiveDataAvailabilityProvider ldap, LiveDataSnapshotProvider ldsp) {
    assert ldap != null;
    assert ldsp != null;
    _liveDataAvailabilityProvider = ldap;
    _liveDataSnapshotProvider = ldsp;
    try {
      _view = constructTrivialExampleView(ldap, ldsp);
      _view.init();
      if(ldsp instanceof InMemoryLKVSnapshotProvider) {
        InMemoryLKVSnapshotProvider snapshotProvider = (InMemoryLKVSnapshotProvider) _view.getProcessingContext().getLiveDataSnapshotProvider();
        DiscountCurveDefinition curveDefinition = constructDiscountCurveDefinition("USD", "Stupidly Lame");
        _popJob = new SnapshotPopulatorJob(snapshotProvider, curveDefinition);
        Thread popThread = new Thread(_popJob);
        popThread.start();
      }
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Error constructing view", e);
    }
  }
  
  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @return the liveDataSnapshotProvider
   */
  public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return _liveDataSnapshotProvider;
  }

  private ViewImpl constructTrivialExampleView(LiveDataAvailabilityProvider ldap, LiveDataSnapshotProvider ldsp) throws Exception {
    ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("Kirk", "KirkPortfolio");
    //viewDefinition.addValueDefinition("EQUITY_OPTION", HardCodedUSDDiscountCurveAnalyticFunction.getDiscountCurveValueDefinition());
    viewDefinition.addValueDefinition("EQUITY_OPTION", new DiscountCurveValueDefinition());
    viewDefinition.addValueDefinition("EQUITY_OPTION", new GreeksResultValueDefinition());
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
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    List<Security> securities = new ArrayList<Security>();
    String[] tickers = new String[] {"APVJS.X", "APVJN.X", "AJLJV.X"};
    double[] strikes = new double[] {195.0, 170.0, 210.0 };
    Expiry expiry = new Expiry(_clock.zonedDateTime().withDate(2009, 10, 16).withTime(17, 00));
    Security aapl = new EquitySecurity("AAPL", "BLOOMBERG");
    //Security aapl = new EquitySecurity("AAPL US Equity", "BbgId");
    secMaster.add(aapl);
    
    for (int i=0; i<tickers.length; i++) {
      DefaultSecurity security = new EuropeanVanillaEquityOptionSecurity(OptionType.CALL, strikes[i], expiry, aapl.getIdentityKey(), Currency.getInstance("USD"));
      security.setIdentifiers(Collections.singleton(new DomainSpecificIdentifier(BLOOMBERG, tickers[i])));
      securities.add(security);
      secMaster.add(security);
    }
    
    DiscountCurveDefinition curveDefinition = constructDiscountCurveDefinition("USD", "Stupidly Lame");
    DiscountCurveAnalyticFunction discountCurveFunction = new DiscountCurveAnalyticFunction(curveDefinition);
    HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction volSurfaceFunction = new HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction();
    GreeksAnalyticFunction greeksFunction = new GreeksAnalyticFunction();
    InMemoryAnalyticFunctionRepository functionRepo = new InMemoryAnalyticFunctionRepository();
    functionRepo.addFunction(discountCurveFunction, discountCurveFunction);
    functionRepo.addFunction(volSurfaceFunction, volSurfaceFunction);
    functionRepo.addFunction(greeksFunction, greeksFunction);

    if(ldap instanceof FixedLiveDataAvailabilityProvider) {
      FixedLiveDataAvailabilityProvider fldap = (FixedLiveDataAvailabilityProvider) ldap;
      fldap.addDefinition(new ResolveSecurityKeyToMarketDataHeaderDefinition(aapl.getIdentityKey()));
      for(Security security : securities) {
        for(AnalyticValueDefinition<?> definition : discountCurveFunction.getInputs()) {
          fldap.addDefinition(definition);
        }
        for(AnalyticValueDefinition<?> definition : volSurfaceFunction.getInputs(security)) {
          if (!definition.getValue("TYPE").equals("DISCOUNT_CURVE")) { // skip derived data.
            fldap.addDefinition(definition);
          }
        }
      }
    }
    
    ViewComputationCacheSource cacheFactory = new MapViewComputationCacheSource();
    
    LinkedBlockingJobQueue jobQueue = new LinkedBlockingJobQueue();
    LinkedBlockingCompletionQueue completionQueue = new LinkedBlockingCompletionQueue();
    _calculationNodes = new ArrayList<SingleThreadCalculationNode>();
    int nNodes = 2;
    for(int i = 0; i < nNodes; i++) {
      SingleThreadCalculationNode calcNode = new SingleThreadCalculationNode(cacheFactory, functionRepo, secMaster, jobQueue, completionQueue);
      _calculationNodes.add(calcNode);
    }
    
    ViewProcessingContext processingContext = new ViewProcessingContext(
        ldap, ldsp, functionRepo, positionMaster, secMaster, cacheFactory,
        jobQueue, completionQueue
      );
    
    ViewImpl view = new ViewImpl(viewDefinition, processingContext);
    view.setComputationExecutorService(Executors.newSingleThreadExecutor());
    
    return view;
  }
  
  private static AnalyticValueDefinition<?> constructBloombergTickerDefinition(String bbTicker) {
    ResolveSecurityKeyToMarketDataHeaderDefinition definition =
      new ResolveSecurityKeyToMarketDataHeaderDefinition(
          new SecurityKeyImpl(new DomainSpecificIdentifier(BLOOMBERG, bbTicker)));
    return definition;
  }
  
  private static DiscountCurveDefinition constructDiscountCurveDefinition(String isoCode, String name) {
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
  @SuppressWarnings("unchecked")
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
      FudgeMsg dataFields = new FudgeMsg();
      dataFields.add(MarketDataAnalyticValue.INDICATIVE_VALUE_NAME, currValue);
      
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
    populateOptions(snapshotProvider, addRandom);
  }
  
  private SecurityKey makeSecurityKey(String ticker) {
    return new SecurityKeyImpl(new DomainSpecificIdentifier(new IdentificationDomain("BLOOMBERG"), ticker));
  }
  
  private AnalyticValue<Map<String, Double>> makeHeaderValue(AnalyticValueDefinition<Map<String, Double>> def, String field, Double value) {
    Map<String, Double> map = new HashMap<String, Double>();
    map.put(field, value);
    return new AbstractAnalyticValue<Map<String, Double>>(def, map) {
      @Override
      public AnalyticValue<Map<String, Double>> scaleForPosition(
          BigDecimal quantity) {
        return this;
      }
    };
  }
  
  private void populateOptions(InMemoryLKVSnapshotProvider snapshotProvider, boolean addRandom) {
    final double OPTION_SCALE_FACTOR = 5.0;
    final double UNDERLYING_SCALE_FACTOR = 5.0;
    AnalyticValueDefinition<Map<String, Double>> apvjs_x_def = new ResolveSecurityKeyToMarketDataHeaderDefinition(makeSecurityKey("APVJS.X"));
    AnalyticValueDefinition<Map<String, Double>> apvjn_x_def = new ResolveSecurityKeyToMarketDataHeaderDefinition(makeSecurityKey("APVJN.X"));
    AnalyticValueDefinition<Map<String, Double>> ajljv_x_def = new ResolveSecurityKeyToMarketDataHeaderDefinition(makeSecurityKey("AJLJV.X"));
    AnalyticValueDefinition<Map<String, Double>> aapl_def = new ResolveSecurityKeyToMarketDataHeaderDefinition(makeSecurityKey("AAPL"));
    
    AnalyticValue<Map<String, Double>> apvjs_x_val = makeHeaderValue(apvjs_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 2.69);
    AnalyticValue<Map<String, Double>> apvjn_x_val = makeHeaderValue(apvjn_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 16.75);
    AnalyticValue<Map<String, Double>> ajljv_x_val = makeHeaderValue(ajljv_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 0.66);
    AnalyticValue<Map<String, Double>> aapl_val = makeHeaderValue(aapl_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * UNDERLYING_SCALE_FACTOR * 0.01)) * 185.5);
    snapshotProvider.addValue(apvjs_x_val);
    snapshotProvider.addValue(apvjn_x_val);
    snapshotProvider.addValue(ajljv_x_val);
    snapshotProvider.addValue(aapl_val);
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

}
