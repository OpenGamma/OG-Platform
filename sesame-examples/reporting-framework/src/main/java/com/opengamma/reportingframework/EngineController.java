/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.reportingframework;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.column;
import static com.opengamma.sesame.config.ConfigBuilder.columns;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static com.opengamma.sesame.config.ConfigBuilder.nonPortfolioOutput;
import static com.opengamma.sesame.config.ConfigBuilder.nonPortfolioOutputs;
import static com.opengamma.sesame.config.ConfigBuilder.output;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.joda.beans.ser.JodaBeanSer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.reportingframework.EngineControllerRequest;
import com.opengamma.reportingframework.SecurityLoaderTest;
import com.opengamma.sesame.ConfigDbMarketExposureSelectorFn;
import com.opengamma.sesame.DefaultCurveNodeConverterFn;
import com.opengamma.sesame.DefaultDiscountingMulticurveBundleFn;
import com.opengamma.sesame.DefaultDiscountingMulticurveBundleResolverFn;
import com.opengamma.sesame.DefaultHistoricalTimeSeriesFn;
//import com.opengamma.sesame.DefaultMulticurveBundleInputFn;
//import com.opengamma.sesame.MulticurveBundleInputFn;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.RootFinderConfiguration;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.ViewColumn;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.ResultItem;
import com.opengamma.sesame.engine.ResultRow;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapFn;
import com.opengamma.sesame.irs.InterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.InterestRateSwapFn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.server.FunctionServer;
import com.opengamma.sesame.server.FunctionServerRequest;
import com.opengamma.sesame.server.IndividualCycleOptions;
import com.opengamma.sesame.server.RemoteFunctionServer;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;


/**
 * Tests that a view can be run against a remote server.
 * The tests cover the validation of a successful PV result
 * and a the curve bundle used to price the swap.
 */
public class EngineController {

  private static final String CURVE_RESULT = "Curve Bundle";
  private static final String CURVE_INPUT_RESULT = "Curve Bundle Inputs";
  private FunctionServer _functionServer;
  private IndividualCycleOptions _cycleOptions;
  private ConfigLink<ExposureFunctions> _exposureConfig;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
  private ConfigLink<CurveConstructionConfiguration> _curveConstructionConfiguration;
  private static List<ManageableSecurity> _inputs = new ArrayList<>();
  private MongoClient _client;
  private ZonedDateTime _valuationDate;
  private static final String _dbName = "test";
  private static MessageConsumer _consumer;
  private Properties _prop;
  //private int _securityID = 1000;
  //private InMemorySecurityMaster _secMaster = new InMemorySecurityMaster();
  //private InMemoryPositionMaster _posMaster = new InMemoryPositionMaster();
  
  private static final Logger s_logger = LoggerFactory.getLogger(EngineController.class);
  
  
  public static void main(String[] args) {
	  EngineController calculator = new EngineController();
	  DB db = calculator.setUp(args);
	  while(true) {
		  try {
			  calculator.loadTrades();
			  if(_inputs == null || _inputs.size() == 0) {
				  Thread.sleep(500);
				  continue;
			  }
			  Results results = calculator.testSwapPVAndBucketedPV01Execution();
			  calculator.parseAndSaveResults(results, db);
			  //calculator.closeDBConnection();
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
	  }
  }
  
  public void parseAndSaveResults(Results results, DB db) {	  
	  List<String> columnNames = results.getColumnNames();
	  HashMap columnNameIndexMap = new HashMap();
	  for (String columnName: columnNames) {
		  int index = results.getColumnIndex(columnName);
	      columnNameIndexMap.put(index, columnName);
	  }
	    
	  DBCollection riskResult = db.getCollection("TradeResults");
	  DBCollection securityData = db.getCollection("SecurityData");
	  DBCollection fullSecurityData = db.getCollection("FullSecurityData");
	  DBCollection bucketedPV01 = db.getCollection("BucketedPV01");
	  DBCollection marketData = db.getCollection("MarketData");
	    
	  List<DBObject> security_docs = new ArrayList<>();
	  List<DBObject> fullSecurityDocs = new ArrayList<>();
	  List<DBObject> riskresult_docs = new ArrayList<>();
	  List<DBObject> bucketedPV01_docs = new ArrayList<>();
	  List<DBObject> marketData_docs = new ArrayList<>();
	    
	  ZonedDateTime marketDataDate = (ZonedDateTime) _valuationDate;
	  GregorianCalendar marketDataGregCal = new GregorianCalendar(marketDataDate.getYear(), marketDataDate.getMonthValue(), marketDataDate.getDayOfMonth());
	  Result<?> inputsResult = results.getNonPortfolioResults().get(CURVE_INPUT_RESULT).getResult();
	  if (inputsResult.isSuccess()) {
	    @SuppressWarnings("unchecked")
      Map<String, Map<ExternalIdBundle, Double>> value =
	        (Map<String, Map<ExternalIdBundle, Double>>) inputsResult.getValue();
	    
	    for (Map.Entry<String, Map<ExternalIdBundle, Double>> marketDataMap: value.entrySet()) {
	    	String curveName = marketDataMap.getKey();
	    	Map<ExternalIdBundle, Double> marketDataValues = marketDataMap.getValue();
	    	for (Map.Entry<ExternalIdBundle, Double> marketDataValue: marketDataValues.entrySet()) {
	    		BasicDBObject marketDataDoc = new BasicDBObject();
	    		marketDataDoc.append("MarketDataDate", marketDataGregCal.getTime());
	    		marketDataDoc.append("CurveName", curveName);
	    		marketDataDoc.append("Ticker", Iterables.getOnlyElement(marketDataValue.getKey()).getValue());
	    		marketDataDoc.append("Value", marketDataValue.getValue());
	    		marketData_docs.add(marketDataDoc);
	    	} 
	    } 
	  }
	    
	    for (ResultRow row : results.getRows()) {
	      BasicDBObject security_doc = new BasicDBObject();
	      BasicDBObject fullSecurityDoc = new BasicDBObject();
	      BasicDBObject riskresult_doc = new BasicDBObject();
	      BasicDBObject bucketedPV01_doc = new BasicDBObject();
	      
	      ExternalId secID = null;
	      InterestRateSwapSecurity irs = null;
	      if(row.getInput() instanceof InterestRateSwapSecurity) {
	        irs = (InterestRateSwapSecurity)row.getInput();
	        
	        String jsonSecurity = JodaBeanSer.COMPACT.jsonWriter().write(irs);
	        //System.out.println("JSON: " + jsonSecurity);
	        fullSecurityDoc = (BasicDBObject)JSON.parse(jsonSecurity);
	        fullSecurityDoc.append("External_ID", irs.externalIdBundle().get().getExternalIds().first().getValue());
	        
	        secID = irs.getExternalIdBundle().getExternalIds().first();
	        InterestRateSwapLeg payLeg = irs.getPayLeg();
	        InterestRateSwapLeg receiveLeg = irs.getReceiveLeg();
	        if (payLeg instanceof FixedInterestRateSwapLeg) {
	        	security_doc.append("Fixed_Rate", ((FixedInterestRateSwapLeg)payLeg).getRate().getInitialRate());
	        }
	        else {
	        	security_doc.append("Fixed_Rate", ((FixedInterestRateSwapLeg)receiveLeg).getRate().getInitialRate());
	        }
	        security_doc.append("Effective_Date", irs.getEffectiveDate().toString());
	        security_doc.append("Pay_Leg_Notional", payLeg.getNotional().getInitialAmount());
	        security_doc.append("Pay_Leg_Notional_Currency", payLeg.getNotional().getCurrency().toString());
	        security_doc.append("Receive_Leg_Notional", receiveLeg.getNotional().getInitialAmount());
	        security_doc.append("Receive_Leg_Notional_Currency", receiveLeg.getNotional().getCurrency().toString());
	        security_doc.append("Trade_Type", irs.getSecurityType());
	        //security_doc.append("Trade_ID", tradeSecurityMap.get(secID).toString());
	        security_doc.append("External_ID", irs.externalIdBundle().get().getExternalIds().first().getValue());
	        
	        riskresult_doc.append("TimeStamp", new Date());
	        riskresult_doc.append("SnapShot", "EOD");
	        GregorianCalendar gregCal = new GregorianCalendar(_valuationDate.getYear(), _valuationDate.getMonthValue(), _valuationDate.getDayOfMonth());
	        riskresult_doc.append("ValuationDate", gregCal.getTime());
	        security_doc.append("ValuationDate", gregCal.getTime());
	        //riskresult_doc.append("Trade_ID", tradeSecurityMap.get(secID).toString());
	        riskresult_doc.append("External_ID", irs.externalIdBundle().get().getExternalIds().first().getValue());
	        //System.out.println("External ID: " + irs.externalIdBundle().get().getExternalIds().first().getValue());
	      }
	      int resultItemIndex = 0;
	      for (ResultItem cell : row.getItems()) {
	        Result<?> result = cell.getResult();
	        if (result.isSuccess()) {
	          //PV
	          if(result.getValue() instanceof MultipleCurrencyAmount) {
	            CurrencyAmount[] currencyAmounts = ((MultipleCurrencyAmount)result.getValue()).getCurrencyAmounts();
	            for(CurrencyAmount currencyAmount : currencyAmounts) {
	              riskresult_doc.append(((String)columnNameIndexMap.get(resultItemIndex)).replaceAll("\\s","_"), currencyAmount.getAmount());
	            }
	          }
	          //BucketedPV01
	          else if(result.getValue() instanceof BucketedCurveSensitivities) {
	        	  Map sensitivities = ((BucketedCurveSensitivities)result.getValue()).getSensitivities();
		            Iterator entryIterator = ((BucketedCurveSensitivities)result.getValue()).getSensitivities().entrySet().iterator();
		            while(entryIterator.hasNext()) {
		              Entry entry = (Entry)entryIterator.next();
		              Pair pair = (Pair)entry.getKey();
		              DoubleLabelledMatrix1D matrix = (DoubleLabelledMatrix1D)sensitivities.get(pair);
		              for (int i=0;i<matrix.getLabels().length;i++) {
		            	  bucketedPV01_doc.append(matrix.getLabels()[i].toString(), matrix.getValues()[i]);
		              }
		            }
		            bucketedPV01_doc.append("External_ID", irs.externalIdBundle().get().getExternalIds().first().getValue());
		            GregorianCalendar gregCal = new GregorianCalendar(_valuationDate.getYear(), _valuationDate.getMonthValue(), _valuationDate.getDayOfMonth());
		            bucketedPV01_doc.append("ValuationDate", gregCal.getTime());
	          }
	          else {
	            System.out.println("Result value: " + result.getValue().getClass());
	          }
	        } else {
	          System.out.println("Failure message:" + result.getFailureMessage());
	          //System.out.println("String rep of the result: " + cell.toString());
	        }
	        resultItemIndex++;
	      }
	      //Do not use: use bulk write instead
	      //WriteResult writeResult = coll.insert(doc);
	      //System.out.println("Result: " + writeResult);
	      security_docs.add(security_doc);
	      fullSecurityDocs.add(fullSecurityDoc);
	      //System.out.println("Security doc: " + security_doc);
	      riskresult_docs.add(riskresult_doc);
	      bucketedPV01_docs.add(bucketedPV01_doc);
	    }
	    for (int index=0; index<security_docs.size(); index++) {
	    	
	    	//System.out.println("External ID: " + security_docs.get(index).get("External_ID"));
	    	//QueryBuilder queryBuilder = QueryBuilder.start().and(new BasicDBObject("External_ID", security_docs.get(index).get("External_ID")), new BasicDBObject("ValuationDate", security_docs.get(index).get("ValuationDate")));
	    	//DBObject query = queryBuilder.get();
	    	//WriteResult writeSecurityResult = securityData.update(query, security_docs.get(index), true, false);
	    	WriteResult writeSecurityResult = securityData.update(new BasicDBObject("External_ID", security_docs.get(index).get("External_ID")), security_docs.get(index), true, false);
	    	//s_logger.info("DB insert result: " + writeSecurityResult);
	    	
	    	//System.out.println("External ID: " + fullSecurityDocs.get(index).get("External_ID"));
	    	WriteResult writeFullSecurityResult = fullSecurityData.update(new BasicDBObject("External_ID", fullSecurityDocs.get(index).get("External_ID")), fullSecurityDocs.get(index), true, false);
	    	//s_logger.info("DB insert result: " + writeFullSecurityResult);
	    	
	    	WriteResult writeRiskResult = riskResult.update(new BasicDBObject("External_ID", riskresult_docs.get(index).get("External_ID")), riskresult_docs.get(index), true, false);
	    	//s_logger.info("DB insert result: " + writeRiskResult);
	    	
	    	WriteResult writeBucketedPV01Result = bucketedPV01.update(new BasicDBObject("External_ID", bucketedPV01_docs.get(index).get("External_ID")), bucketedPV01_docs.get(index), true, false);
	    	//s_logger.info("DB insert result: " + writeBucketedPV01Result);
	    }
	    //WriteResult writeRiskResult = riskResult.insert(riskresult_docs);
	    //WriteResult writeSecurityResult = securityData.insert(security_docs);
	    //WriteResult writeBucketedPV01Result = bucketedPV01.insert(bucketedPV01_docs);
	    
	    WriteResult writeMarketDataResult = marketData.insert(marketData_docs);
	    
	    //s_logger.info("DB insert result: " + writeRiskResult);
	    //s_logger.info("DB insert result: " + writeSecurityResult);
	    //s_logger.info("DB insert result: " + writeBucketedPV01Result);
	    //s_logger.info("DB insert result: " + writeMarketDataResult);
	    s_logger.info("Writing to DB complete...");
  }

  public DB setUp(String[] args) {
	  _prop = new Properties();
	  String propFileName = parseArguments(args, "-config");
      InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propFileName);
      try {
          if (inputStream == null) {
              throw new FileNotFoundException("Property File '" + propFileName + "' not found in the classpath");
          }
          _prop.load(inputStream);
      }
      catch(Exception e) {
    	  e.printStackTrace();
    	  System.exit(1);
      }
      String mode = _prop.getProperty("mode");
      String exposureFunction = _prop.getProperty("exposureFunction");
      String curveConstructionConfiguration = _prop.getProperty("curveConstructionConfiguration");
      ZonedDateTime currentTime = ZonedDateTime.now();
      _valuationDate = DateUtils.getUTCDate(2014, 7, 28, currentTime.getHour(), currentTime.getMinute());
	  //_valuationDate

    /* Create a RemoteFunctionServer to executes view requests RESTfully.*/
    _functionServer = new RemoteFunctionServer(URI.create(_prop.getProperty("riskEngineURL")));

    /* Single cycle options containing the market data specification and valuation time.
       The captureInputs flag (false by default) will return the data used to calculate
       the result. Note this can be a very large object. */
    _cycleOptions = IndividualCycleOptions.builder()
        .valuationTime(_valuationDate)
        .marketDataSpec(LiveMarketDataSpecification.of("Bloomberg"))
        // UserMarketDataSpecification.of()
        .captureInputs(false)  //creates the ViewInput object to allow mapping between marketdata and risk results
        .build();

    /* Configuration links matching the curve exposure function, currency matrix and curve bundle
       as named on the remote server. These are needed as specific arguments in the creation
       of the ViewConfig. */
    _exposureConfig = ConfigLink.resolvable(exposureFunction, ExposureFunctions.class);
    //_exposureConfig = ConfigLink.resolvable("JLB USD GBP Exposure Functions", ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable("BloombergLiveData", CurrencyMatrix.class);
    _curveConstructionConfiguration = ConfigLink.resolvable(curveConstructionConfiguration,
                                                            CurveConstructionConfiguration.class);
    //_curveConstructionConfiguration = ConfigLink.resolvable("JLB Simple USD Curve Construction Configuration",
     //       CurveConstructionConfiguration.class);

    /* Add a single Fixed vs Libor 3m Swap to the ManageableSecurity list */
    //ManageableSecurity sec = createFixedVsLibor3mSwap(0.015);
    //sec.addExternalId(ExternalId.parse("Radar~" + getExternalSecurityID()));
    //_inputs.add(sec);
    //ManageableSecurity sec2 = createFixedVsLibor3mSwap(0.017);
    //sec2.addExternalId(ExternalId.parse("Radar~" + getExternalSecurityID()));
    //_inputs.add(sec2);
    
    if(mode.equals("JMS")) {
    	initializeJMS();
    }
    return connectToMongoDB();
  }
  
  /*//private class ControllerMessageListerner implements MessageListener {
	  public void onMessage(Message message) {
		  try {
			  System.out.println("I got something!!!!!!");
			  String textMessage = ((TextMessage)message).getText();
			  ManageableSecurity security = JodaBeanSer.COMPACT.xmlReader().read(textMessage, ManageableSecurity.class);
			  _inputs.add(security);
		  }
		  catch(Exception e) {
			  e.printStackTrace();
		  }
	  }
 // } */
  
  public void loadTrades() {
	  if(_prop.getProperty("mode").equals("JMS")) {
		  try {
			  //System.out.println("Hello");
			  TextMessage message = (TextMessage)_consumer.receive();
			  //System.out.println("Message: " + message);
			  String textMessage = message.getText();
			  EngineControllerRequest request = JodaBeanSer.COMPACT.xmlReader().read(textMessage, EngineControllerRequest.class);
			  List<ManageableSecurity> securities = request.getRadarRequest();
			  _inputs = securities;
		  }
		  catch (Exception e) {
			  e.printStackTrace();
		  }
		  System.out.println("JMS Mode");
		  System.out.println(_inputs);
		  if(_inputs==null) {
			  System.out.println("inputs are null");
		  }
		  //System.exit(1);
	  }
	  else {
		  SecurityLoaderTest securityLoader = new SecurityLoaderTest();
		  _inputs = securityLoader.execute(_prop.getProperty("securityDataFile"));
	  }
  }
  
  public void initializeJMS() {
	  String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	  String subject = _prop.getProperty("JMSQueue");
	  try {
		  ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		  Connection connection = connectionFactory.createConnection();
		  connection.start();
	
		  // Creating session for sending messages
		  Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	
		  // Getting the queue 'TESTQUEUE'
		  Destination destination = session.createQueue(subject);
		
		  // MessageConsumer is used for receiving (consuming) messages
		  _consumer = session.createConsumer(destination);
		  //_consumer.setMessageListener(this);
		  System.out.println("ActiveMQ initialized");
	  }
	  catch(Exception e) {
		  e.printStackTrace();
	  }
  }
  
  /*public HashMap createTrades() {
	  HashMap tradeSecurityMap = new HashMap();
	  SimpleCounterparty counterparty = new SimpleCounterparty(ExternalId.parse("Radar~" + getExternalSecurityID()));
	  List<ManageableTrade> trades = new ArrayList<ManageableTrade>();
	  
	  for(ManageableSecurity manageableSecurity : _inputs) {
		  //manageableSecurity.addExternalId(ExternalId.parse("Radar~" + getExternalSecurityID()));
		  SecurityDocument secDoc = new SecurityDocument(manageableSecurity);
		  secDoc = _secMaster.add(secDoc);
		  System.out.println("Security object id: " + secDoc.getObjectId());
		  System.out.println("Security external id: " + secDoc.getSecurity().getExternalIdBundle().getExternalIds());
		  ExternalId secExID = secDoc.getSecurity().getExternalIdBundle().getExternalIds().first();
		  LocalDate tradeDate = LocalDate.of(2014, 8, 12);
		  ManageablePosition manageablePosition = new ManageablePosition(BigDecimal.ONE, secExID);
		  ManageableTrade manageableTrade = new ManageableTrade(BigDecimal.ONE, secExID, tradeDate, OffsetTime.now(), counterparty.getExternalId());
		  manageableTrade.setProviderId(ExternalId.parse("Radar~" + getExternalSecurityID()));
		  manageablePosition.addTrade(manageableTrade);
		  trades.add(manageableTrade);
		  PositionDocument posDoc = _posMaster.add(new PositionDocument(manageablePosition));
		  System.out.println("Position object id: " + posDoc.getObjectId());
		  ExternalId tradeID = posDoc.getPosition().getTrades().get(0).getProviderId();
		  System.out.println("Trade id: " + posDoc.getPosition().getTrades().get(0).getProviderId());
		  tradeSecurityMap.put(secExID, tradeID);
	  }
	  return tradeSecurityMap;
  }
  
  public String getExternalSecurityID() {
	  String securityID = Integer.toString(_securityID);
	  _securityID++;
	  return securityID;
  } */
  
  /*public void saveSecurities() {
	  
	  ToolContext context = ToolContextUtils.getToolContext(URL, ToolContext.class);
	  
	  SecurityMaster securityMaster = context.getSecurityMaster();
	  
	  for(ManageableSecurity manageableSecurity : _inputs) {
		  SecurityDocument secDoc = new SecurityDocument(manageableSecurity);
		  secDoc = securityMaster.add(secDoc);
		  System.out.println("Object id: " + secDoc.getObjectId());
	  }
  } */
  
  /*public void savePortfolio() {
	  
	  /*ToolContext context = ToolContextUtils.getToolContext(URL, ToolContext.class);
	  SecurityMaster securityMaster = context.getSecurityMaster();
	  UniqueId uniqueID = UniqueId.parse("DbSec~1033");  //DbSec~1034
	  SecurityDocument securityDocument = securityMaster.get(uniqueID);
	  ManageableSecurity manageableSecurity = securityDocument.getSecurity();*/
	  
	  
	  
	  /*ManageablePosition manageablePosition = new ManageablePosition(BigDecimal.ONE, );
	  
	  
	  PortfolioMaster portfolioMaster = context.getPortfolioMaster();
	  ManageablePortfolioNode rootNode = new ManageablePortfolioNode("Radar Portfolio Node");
	  rootNode.addPosition(null);
	  PortfolioDocument portDoc = new PortfolioDocument(new ManageablePortfolio("Radar Portfolio", rootNode));
	  portDoc = portfolioMaster.add(portDoc);
	  System.out.println("Portfolio id: " + portDoc.getObjectId());
  } */
  
  
  
  public Results testSwapPVAndBucketedPV01Execution() {
	       FunctionServerRequest<IndividualCycleOptions> request =
	           FunctionServerRequest.<IndividualCycleOptions>builder()
	             .viewConfig(createDoubleColumnViewConfig(OutputNames.PRESENT_VALUE, OutputNames.BUCKETED_PV01))
	               .inputs(_inputs)
	               .cycleOptions(_cycleOptions)
	               .build();
	       
	       s_logger.info("Running Risk....");
	       Results results = _functionServer.executeSingleCycle(request);
	       s_logger.info("Risk run complete! Parsing...");
	       return results;
  }

  /* Output specific view configuration for interest rate swaps */
  private ViewConfig createSingleColumnViewConfig(String output) {

    return
      configureView(
        "IRS Remote view",
        createViewColumn(output)
      );
  }
  
  /* Output specific double column view configuration for interest rate swaps */
  private ViewConfig createDoubleColumnViewConfig(String first, String second) {
     return
         configureView(
              "IRS Remote view",
//              columns(
            		  createViewColumn(first),
            		  createViewColumn(second)
//            		  ),
//              nonPortfolioOutputs(nonPortfolioOutput(
//                                  CURVE_INPUT_RESULT,
//                                  output(MulticurveBundleInputFn.MULTICURVE_BUNDLE_INPUT,
//                                      config(
//                                          arguments(
//                                      function(
//                                          RootFinderConfiguration.class,
//                                          argument("rootFinderAbsoluteTolerance", 1e-9),
//                                          argument("rootFinderRelativeTolerance", 1e-9),
//                                          argument("rootFinderMaxIterations", 10)),
//                                      function(DefaultCurveNodeConverterFn.class,
//                                          argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
//                                      function(DefaultHistoricalMarketDataFn.class,
//                                          argument("dataSource", "BLOOMBERG"),
//                                          argument("currencyMatrix", _currencyMatrixLink)),
//                                      function(DefaultMarketDataFn.class,
//                                          argument("dataSource", "BLOOMBERG"),
//                                          argument("currencyMatrix", _currencyMatrixLink)),
//                                      function(
//                                          DefaultHistoricalTimeSeriesFn.class,
//                                          argument("resolutionKey", "DEFAULT_TSS"),
//                                          argument("htsRetrievalPeriod", RetrievalPeriod.of((Period.ofYears(1))))),
//                                      function(
//                                          DefaultDiscountingMulticurveBundleResolverFn.class,
//                                          argument("curveConfig", _curveConstructionConfiguration)),
////                                      function(
////                                          DefaultMulticurveBundleInputFn.class,
////                                          argument("curveConfig", _curveConstructionConfiguration)),
//                                      function(
//                                          DefaultDiscountingMulticurveBundleFn.class,
//                                          argument("impliedCurveNames", StringSet.of())
//                                  )
//                                  )
//                                  )
//                                  )
//                                  )
//                                  ) 
          );
    }
  
    /* Shared column configuration */
    private ViewColumn createViewColumn(String output) {
      return column(output,
                    config(
                        arguments(
                            function(ConfigDbMarketExposureSelectorFn.class, argument("exposureConfig", _exposureConfig)),
                            function(
                                RootFinderConfiguration.class,
                                argument("rootFinderAbsoluteTolerance", 1e-9),
                                argument("rootFinderRelativeTolerance", 1e-9),
                                argument("rootFinderMaxIterations", 1000)),
                            function(DefaultCurveNodeConverterFn.class,
                                     argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                            function(DefaultHistoricalMarketDataFn.class,
                                     argument("dataSource", "BLOOMBERG"),
                                     argument("currencyMatrix", _currencyMatrixLink)),
                            function(DefaultMarketDataFn.class,
                                     argument("dataSource", "BLOOMBERG"),
                                     argument("currencyMatrix", _currencyMatrixLink)),
                            function(
                                DefaultHistoricalTimeSeriesFn.class,
                                argument("resolutionKey", "DEFAULT_TSS"),
                                argument("htsRetrievalPeriod", RetrievalPeriod.of((Period.ofYears(1))))),
                            function(
                                DefaultDiscountingMulticurveBundleFn.class,
                                argument("impliedCurveNames", StringSet.of()))),
                        implementations(InterestRateSwapFn.class, DiscountingInterestRateSwapFn.class,
                                        InterestRateSwapCalculatorFactory.class, DiscountingInterestRateSwapCalculatorFactory.class)
                    ),
                    output(output, InterestRateSwapSecurity.class)
      );
    }

    /* A non portfolio output view configuration to capture the curve inputs 
    private ViewConfig createCurveBundleInputConfig() {

    	return configureView(
    			"Curve Bundle Input only",
    			nonPortfolioOutput(
    					CURVE_INPUT_RESULT,
    					output(MulticurveBundleInputFn.MULTICURVE_BUNDLE_INPUT,
    							config(
    									arguments(
    							function(
    									RootFinderConfiguration.class,
    									argument("rootFinderAbsoluteTolerance", 1e-9),
    									argument("rootFinderRelativeTolerance", 1e-9),
    									argument("rootFinderMaxIterations", 1000)),
    							function(DefaultCurveNodeConverterFn.class,
    									argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
    							function(DefaultHistoricalMarketDataFn.class,
    									argument("dataSource", "BLOOMBERG"),
    									argument("currencyMatrix", _currencyMatrixLink)),
    							function(DefaultMarketDataFn.class,
    									argument("dataSource", "BLOOMBERG"),
    									argument("currencyMatrix", _currencyMatrixLink)),
    							function(
    									DefaultHistoricalTimeSeriesFn.class,
    									argument("resolutionKey", "DEFAULT_TSS"),
    									argument("htsRetrievalPeriod", RetrievalPeriod.of((Period.ofYears(1))))),
    							function(
    									DefaultDiscountingMulticurveBundleResolverFn.class,
    									argument("curveConfig", _curveConstructionConfiguration)),
    						  function(
                      DefaultMulticurveBundleInputFn.class,
                      argument("curveConfig", _curveConstructionConfiguration)),
    							function(
    									DefaultDiscountingMulticurveBundleFn.class,
    									argument("impliedCurveNames", StringSet.of())))))
    					));
    } */

  /* A non portfolio output view configuration to capture the build curves 
  private ViewConfig createCurveBundleConfig() {

    return configureView(
             "Curve Bundle only",
             nonPortfolioOutput(
                 CURVE_RESULT,
                  output(OutputNames.DISCOUNTING_MULTICURVE_BUNDLE,
                         config(
                             arguments(
                                 function(
                                     RootFinderConfiguration.class,
                                     argument("rootFinderAbsoluteTolerance", 1e-9),
                                     argument("rootFinderRelativeTolerance", 1e-9),
                                     argument("rootFinderMaxIterations", 1000)),
                                 function(DefaultCurveNodeConverterFn.class,
                                          argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                                 function(DefaultHistoricalMarketDataFn.class,
                                          argument("dataSource", "BLOOMBERG"),
                                          argument("currencyMatrix", _currencyMatrixLink)),
                                 function(DefaultMarketDataFn.class,
                                          argument("dataSource", "BLOOMBERG"),
                                          argument("currencyMatrix", _currencyMatrixLink)),
                                 function(
                                     DefaultHistoricalTimeSeriesFn.class,
                                     argument("resolutionKey", "DEFAULT_TSS"),
                                     argument("htsRetrievalPeriod", RetrievalPeriod.of((Period.ofYears(1))))),
                                 function(
                                     DefaultDiscountingMulticurveBundleResolverFn.class,
                                     argument("curveConfig", _curveConstructionConfiguration)),
                                 function(
                                     DefaultDiscountingMulticurveBundleFn.class,
                                     argument("impliedCurveNames", StringSet.of())))))
             ));
  } */
  
  /* public String getFileName(String[] args) {
	  String fileName = null;
	  for(int i=0; i<args.length; i++) {
		  switch(args[i]) {
	        case "-fileName": 
	          System.out.println("User name: " + args[i+1]);
	          fileName = args[i+1];
	      }
	  }
	  return fileName;
  } */
  
	public String parseArguments(String[] args, String argument) {
		for(int i=0; i<args.length; i++) {
			if(args[i].equals(argument)) {
		    	//System.out.println("User name: " + args[i+1]);
				return args[i+1];
			}
			}
		return null;
		}
  
  public DB connectToMongoDB() {
	    DB db = null;
	    try {
	      _client = new MongoClient(_prop.getProperty("dbServerName"));
	      s_logger.info("DB version: " + _client.getVersion());
	      db = _client.getDB(_dbName);
	      if (db == null) {
	    	  s_logger.info("Could not connect to database " + _dbName + "\nexiting...");
	    	  System.exit(1);
	      }
	    } catch(Exception e) {
	      e.printStackTrace();
	    }
	    return db;
	    }
	  
  public void closeDBConnection() {
	  _client.close();
  }
}
