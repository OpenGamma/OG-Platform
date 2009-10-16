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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.AnalyticValueImpl;
import com.opengamma.engine.analytics.DiscountCurveValueDefinition;
import com.opengamma.engine.analytics.GreeksAnalyticFunction;
import com.opengamma.engine.analytics.GreeksResultValueDefinition;
import com.opengamma.engine.analytics.HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction;
import com.opengamma.engine.analytics.InMemoryAnalyticFunctionRepository;
import com.opengamma.engine.analytics.MarketDataAnalyticValue;
import com.opengamma.engine.analytics.MarketDataAnalyticValueDefinitionFactory;
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
import com.opengamma.engine.security.EquityOptionSecurity;
import com.opengamma.engine.security.EquitySecurity;
import com.opengamma.engine.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.engine.security.InMemorySecurityMaster;
import com.opengamma.engine.security.OptionType;
import com.opengamma.engine.security.Security;
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
  private static final IdentificationDomain BBG_ID_DOMAIN = new IdentificationDomain("BbgId");
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
    viewDefinition.addValueDefinition(EquityOptionSecurity.EQUITY_OPTION_TYPE, new DiscountCurveValueDefinition());
    viewDefinition.addValueDefinition(EquityOptionSecurity.EQUITY_OPTION_TYPE, new GreeksResultValueDefinition());
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
    
    EquitySecurity aapl = new EquitySecurity("AAPL US Equity", "BbgId");
    //Security mtlqq_pk = new EquitySecurity("MTLQQ.PK", "BLOOMBERG");
    EquitySecurity ibm = new EquitySecurity("IBM US Equity", "BbgId");
    EquitySecurity gs = new EquitySecurity("GS US Equity", "BbgId");
    //Security aapl = new EquitySecurity("AAPL US Equity", "BbgId");
    secMaster.add(aapl);
    //secMaster.add(mtlqq_pk);
    secMaster.add(ibm);
    secMaster.add(gs);
    String[] tickers = new String[] {
        "APVJS.X Equity", "APVJN.X Equity", "AJLJV.X Equity",
        //"GMLV.X", "GMLW.X", "GMLA.X",
        "IBMJE.X Equity", "IBMJF.X Equity", "IBMJG.X Equity", "IBMJH.X Equity", "IBMJI.X Equity",
        "GPYVP.X Equity", "GPYVS.X Equity", "GPYVB.X Equity", "GPYJR.X Equity", "GPYJM.X Equity"};
    double[] strikes = new double[] {
        195.0, 170.0, 210.0,
        //1.0, 2.0, 5.0,
        125.0, 130.0, 135.0, 140.0, 145.0,
        180.0, 195.0, 210.0, 190.0, 165.0};
    Expiry aapl_ibm_gs_Expiry = new Expiry(_clock.zonedDateTime().withDate(2009, 11, 16).withTime(17, 00));
    //Expiry mtlqq_pkExpiry = new Expiry(_clock.zonedDateTime().withDate(2009, 12, 18).withTime(17, 00));
    Expiry[] expiries = new Expiry[] {
        aapl_ibm_gs_Expiry, aapl_ibm_gs_Expiry, aapl_ibm_gs_Expiry,
        //mtlqq_pkExpiry, mtlqq_pkExpiry, mtlqq_pkExpiry,
        aapl_ibm_gs_Expiry, aapl_ibm_gs_Expiry, aapl_ibm_gs_Expiry, aapl_ibm_gs_Expiry, aapl_ibm_gs_Expiry, 
        aapl_ibm_gs_Expiry, aapl_ibm_gs_Expiry, aapl_ibm_gs_Expiry, aapl_ibm_gs_Expiry, aapl_ibm_gs_Expiry};
    Security[] underlyings = new Security[] {
        aapl, aapl, aapl,
        //mtlqq_pk, mtlqq_pk, mtlqq_pk,
        ibm, ibm, ibm, ibm, ibm,
        gs, gs, gs, gs, gs };
    //OptionType[] types = new OptionType[] { OptionType.CALL, OptionType.CALL, OptionType.CALL, OptionType.CALL, OptionType.CALL, OptionType.CALL, OptionType.CALL, OptionType.CALL, OptionType.CALL, OptionType.CALL, OptionType.CALL, OptionType.PUT, OptionType.PUT, OptionType.PUT, OptionType.CALL, OptionType.CALL };
    List<Security> securities = new ArrayList<Security>();
    
    for (int i=0; i<tickers.length; i++) {
      DefaultSecurity security = new EuropeanVanillaEquityOptionSecurity(OptionType.CALL, strikes[i], expiries[i], underlyings[i].getIdentityKey(), Currency.getInstance("USD"));
      security.setIdentifiers(Collections.singleton(new DomainSpecificIdentifier(BBG_ID_DOMAIN, tickers[i])));
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
      for(AnalyticValueDefinition<?> definition : discountCurveFunction.getInputs()) {
        fldap.addDefinition(definition);
      }
      fldap.addDefinition(MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "AAPL US Equity"));
      fldap.addDefinition(MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "IBM US Equity"));
      fldap.addDefinition(MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "GS US Equity"));
      for(Security security : securities) {
        for(AnalyticValueDefinition<?> definition : volSurfaceFunction.getInputs(security)) {
          if (!ObjectUtils.equals(definition.getValue("TYPE"), "DISCOUNT_CURVE")) { // skip derived data.
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
    return MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", bbTicker);
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
      
      AnalyticValue value = new AnalyticValueImpl(strip.getStripValueDefinition(), dataFields) {
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
  
  private AnalyticValue<FudgeMsg> makeMarketDataValue(AnalyticValueDefinition<FudgeMsg> def, String field, Double value) {
    FudgeMsg msg = new FudgeMsg();
    msg.add(MarketDataAnalyticValue.INDICATIVE_VALUE_NAME, value);
    return new MarketDataAnalyticValue(def, msg);
  }
  
  private void populateOptions(InMemoryLKVSnapshotProvider snapshotProvider, boolean addRandom) {
    final double OPTION_SCALE_FACTOR = 5.0;
    final double UNDERLYING_SCALE_FACTOR = 5.0;
    AnalyticValueDefinition<FudgeMsg> apvjs_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "APVJS.X Equity");
    AnalyticValueDefinition<FudgeMsg> apvjn_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "APVJN.X Equity");
    AnalyticValueDefinition<FudgeMsg> ajljv_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "AJLJV.X Equity");
    
    //AnalyticValueDefinition<FudgeMsg> gmlv_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "GMLV.X Equity");
    //AnalyticValueDefinition<FudgeMsg> gmlw_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "GMLW.X Equity");
    //AnalyticValueDefinition<FudgeMsg> gmla_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "GMLA.X Equity");
    
    AnalyticValueDefinition<FudgeMsg> ibmje_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "IBMJE.X Equity");
    AnalyticValueDefinition<FudgeMsg> ibmjf_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "IBMJF.X Equity");
    AnalyticValueDefinition<FudgeMsg> ibmjg_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "IBMJG.X Equity");
    AnalyticValueDefinition<FudgeMsg> ibmjh_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "IBMJH.X Equity");
    AnalyticValueDefinition<FudgeMsg> ibmji_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "IBMJI.X Equity");
    
    AnalyticValueDefinition<FudgeMsg> gpyvp_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "GPYVP.X Equity");
    AnalyticValueDefinition<FudgeMsg> gpyvs_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "GPYVS.X Equity");
    AnalyticValueDefinition<FudgeMsg> gpyvb_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "GPYVB.X Equity");
    AnalyticValueDefinition<FudgeMsg> gpyjr_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "GPYJR.X Equity");
    AnalyticValueDefinition<FudgeMsg> gpyjm_x_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "GPYJM.X Equity");
    
    
    AnalyticValueDefinition<FudgeMsg> aapl_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "AAPL US Equity");
    //AnalyticValueDefinition<FudgeMsg> mtlqq_pk_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "MTLQQ.PK");
    AnalyticValueDefinition<FudgeMsg> ibm_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "IBM US Equity");
    AnalyticValueDefinition<FudgeMsg> gs_def = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition("BbgId", "GS US Equity");
    
    AnalyticValue<FudgeMsg> apvjs_x_val = makeMarketDataValue(apvjs_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 2.69);
    AnalyticValue<FudgeMsg> apvjn_x_val = makeMarketDataValue(apvjn_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 16.75);
    AnalyticValue<FudgeMsg> ajljv_x_val = makeMarketDataValue(ajljv_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 0.66);
    //AnalyticValue<FudgeMsg> gmlv_x_val = makeMarketDataValue(gmlv_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 0.05);
    //AnalyticValue<FudgeMsg> gmlw_x_val = makeMarketDataValue(gmlw_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 0.03);
    //AnalyticValue<FudgeMsg> gmla_x_val = makeMarketDataValue(gmla_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 0.01);
    AnalyticValue<FudgeMsg> ibmje_x_val = makeMarketDataValue(ibmje_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 3.3);
    AnalyticValue<FudgeMsg> ibmjf_x_val = makeMarketDataValue(ibmjf_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 1.0);
    AnalyticValue<FudgeMsg> ibmjg_x_val = makeMarketDataValue(ibmjg_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 0.24);
    AnalyticValue<FudgeMsg> ibmjh_x_val = makeMarketDataValue(ibmjh_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 0.05);
    AnalyticValue<FudgeMsg> ibmji_x_val = makeMarketDataValue(ibmji_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 0.05);
        
    AnalyticValue<FudgeMsg> gpyvp_x_val = makeMarketDataValue(gpyvp_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 2.03);
    AnalyticValue<FudgeMsg> gpyvs_x_val = makeMarketDataValue(gpyvs_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 10.09);
    AnalyticValue<FudgeMsg> gpyvb_x_val = makeMarketDataValue(gpyvb_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 24.15);
    AnalyticValue<FudgeMsg> gpyjr_x_val = makeMarketDataValue(gpyjr_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 2.55);
    AnalyticValue<FudgeMsg> gpyjm_x_val = makeMarketDataValue(gpyjm_x_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * OPTION_SCALE_FACTOR * 0.01)) * 20.85);
    
    AnalyticValue<FudgeMsg> aapl_val = makeMarketDataValue(aapl_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * UNDERLYING_SCALE_FACTOR * 0.01)) * 185.5);
    //AnalyticValue<FudgeMsg> mtlqq_pk_val = makeMarketDataValue(mtlqq_pk_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * UNDERLYING_SCALE_FACTOR * 0.01)) * 0.7110);
    AnalyticValue<FudgeMsg> ibm_val = makeMarketDataValue(ibm_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * UNDERLYING_SCALE_FACTOR * 0.01)) * 126.56);
    AnalyticValue<FudgeMsg> gs_val = makeMarketDataValue(gs_def, HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction.PRICE_FIELD_NAME, (1 + (Math.random() * UNDERLYING_SCALE_FACTOR * 0.01)) * 185.44);
    
    snapshotProvider.addValue(apvjs_x_val);
    snapshotProvider.addValue(apvjn_x_val);
    snapshotProvider.addValue(ajljv_x_val);
    
    //snapshotProvider.addValue(gmlv_x_val);
    //snapshotProvider.addValue(gmlw_x_val);
    //snapshotProvider.addValue(gmla_x_val);
    
    snapshotProvider.addValue(ibmje_x_val);
    snapshotProvider.addValue(ibmjf_x_val);
    snapshotProvider.addValue(ibmjg_x_val);
    snapshotProvider.addValue(ibmjh_x_val);
    snapshotProvider.addValue(ibmji_x_val);
   
    snapshotProvider.addValue(gpyvp_x_val);
    snapshotProvider.addValue(gpyvs_x_val);
    snapshotProvider.addValue(gpyvb_x_val);
    snapshotProvider.addValue(gpyjr_x_val);
    snapshotProvider.addValue(gpyjm_x_val);
  
    snapshotProvider.addValue(aapl_val);
    //snapshotProvider.addValue(mtlqq_pk_val);
    snapshotProvider.addValue(ibm_val);
    snapshotProvider.addValue(gs_val);
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
