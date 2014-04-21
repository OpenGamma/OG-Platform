/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.test;

import static com.opengamma.bbg.replay.BloombergTick.FIELDS_KEY;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;

import com.bloomberglp.blpapi.SessionOptions;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.cache.MongoDBValueCachingReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.util.mongo.MongoConnector;
import com.opengamma.util.mongo.MongoConnectorFactoryBean;
import com.opengamma.util.test.TestProperties;

/**
 * Test utilities for Bloomberg.
 */
public final class BloombergTestUtils {

  /**
   * Restricted constructor.
   */
  private BloombergTestUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a Bloomberg connector for testing.
   * 
   * @return the connector, not null
   */
  public static BloombergConnector getBloombergConnector() {
    return new BloombergConnector("BloombergTestUtils", getSessionOptions());
  }

  /**
   * Creates a Bloomberg connector for testing.
   * 
   * @return the connector, not null
   */
  public static BloombergConnector getBloombergBipeConnector() {
    return new BloombergConnector("BloombergTestUtils", getSessionOptions("bbgBpipeServer.host", "bbgBpipeServer.port"));
  }

  /**
   * Creates a Bloomberg reference data provider for testing.
   * This must be started before use by the caller.
   * This must be closed after use by the caller.
   * It is typically also wrapped in a caching provider.
   * 
   * @return the provider, not null
   */
  public static BloombergReferenceDataProvider getBloombergReferenceDataProvider() {
    BloombergConnector bbgConnector = BloombergTestUtils.getBloombergConnector();
    return new BloombergReferenceDataProvider(bbgConnector);
  }

  private static SessionOptions getSessionOptions() {
    return getSessionOptions("bbgServer.host", "bbgServer.port");
  }

  /**
   * Creates Bloomberg session options for testing.
   * 
   * @return the session options, not null
   */
  private static SessionOptions getSessionOptions(String bbgServerHostName, String bbgServerPortName) {
    SessionOptions options = new SessionOptions();
    Properties properties = TestProperties.getTestProperties();
    String serverHost = properties.getProperty(bbgServerHostName);
    if (StringUtils.isBlank(serverHost)) {
      throw new OpenGammaRuntimeException(bbgServerHostName + " is missing in tests.properties");
    }
    String serverPort = properties.getProperty(bbgServerPortName);
    if (StringUtils.isBlank(serverPort)) {
      throw new OpenGammaRuntimeException(bbgServerPortName + " is missing in tests.properties");
    }
    options.setServerHost(serverHost);
    options.setServerPort(Integer.parseInt(serverPort));
    return options;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a Mongo connector for testing.
   * 
   * @return the connector, not null
   */
  public static MongoConnector getMongoConnector() {
    Properties testProperties = TestProperties.getTestProperties();
    String mongoHost = testProperties.getProperty("mongoServer.host");
    int mongoPort = Integer.parseInt(testProperties.getProperty("mongoServer.port"));
    MongoConnectorFactoryBean mongoFactory = new MongoConnectorFactoryBean();
    mongoFactory.setName("BloombergTestUtils");
    mongoFactory.setHost(mongoHost);
    mongoFactory.setPort(mongoPort);
    mongoFactory.setDatabaseName("testReferenceData");
    mongoFactory.setCollectionSuffix("bloomberg-security-loader-test-context");
    return mongoFactory.getObjectCreating();
  }

  /**
   * Creates a Mongo caching reference data provider for testing.
   * 
   * @param bbgProvider  the Bloomberg provider to wrap, not null
   * @return the provider, not null
   */
  public static ReferenceDataProvider getMongoCachingReferenceDataProvider(BloombergReferenceDataProvider bbgProvider) {
    MongoConnector mongoConnector = BloombergTestUtils.getMongoConnector();
    return new MongoDBValueCachingReferenceDataProvider(bbgProvider, mongoConnector);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a random tick.
   * 
   * @param random  the source of randomness, not null
   * @param fudgeMsgFactory  the Fudge message factory, not null
   * @return the message, not null
   */
  public static MutableFudgeMsg makeRandomStandardTick(Random random, FudgeMsgFactory fudgeMsgFactory) {
    MutableFudgeMsg result = fudgeMsgFactory.newMessage();
    MutableFudgeMsg bbgTickAsFudgMsg = fudgeMsgFactory.newMessage();
    bbgTickAsFudgMsg.add("BID", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("ASK", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("BEST_BID", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("BEST_ASK", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("IND_BID_FLAG", false);
    bbgTickAsFudgMsg.add("IND_ASK_FLAG", false);
    bbgTickAsFudgMsg.add("ASK_SIZE_TDY", String.valueOf(random.nextInt()));
    bbgTickAsFudgMsg.add("BID_SIZE_TDY", String.valueOf(random.nextInt()));
    bbgTickAsFudgMsg.add("BID_TDY", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("ASK_TDY", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("ASK_SIZE", String.valueOf(random.nextInt()));
    bbgTickAsFudgMsg.add("BID_SIZE", String.valueOf(random.nextInt()));
    bbgTickAsFudgMsg.add("LAST_PRICE", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("LAST_TRADE", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("VOLUME", String.valueOf(random.nextInt()));
    bbgTickAsFudgMsg.add("HIGH", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("LOW", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("OPEN", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("OPEN_TDY", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("VOLUME_TDY", "17925");
    bbgTickAsFudgMsg.add("LAST_TRADE_TDY", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("HIGH_TDY", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("LOW_TDY", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("EXCH_CODE_LAST", "D");
    bbgTickAsFudgMsg.add("LAST_PX_LOCAL_EXCH_SOURCE_RT", "UD");
    bbgTickAsFudgMsg.add("API_MACHINE", "n166");
    bbgTickAsFudgMsg.add("TRADING_DT_REALTIME", "2009-12-08+00:00");
    bbgTickAsFudgMsg.add("EQY_TURNOVER_REALTIME", "211460.515625");
    bbgTickAsFudgMsg.add("RT_API_MACHINE", "n166");
    bbgTickAsFudgMsg.add("RT_PRICING_SOURCE", "US");
    bbgTickAsFudgMsg.add("IS_DELAYED_STREAM", true);
    bbgTickAsFudgMsg.add("MKTDATA_EVENT_TYPE", "SUMMARY");
    bbgTickAsFudgMsg.add("PREV_SES_LAST_PRICE", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("RT_PX_CHG_NET_1D", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("RT_PX_CHG_PCT_1D", String.valueOf(random.nextDouble()));
    bbgTickAsFudgMsg.add("SES_START", "14:30:00.000+00:00");
    bbgTickAsFudgMsg.add("SES_END", "21:30:00.000+00:00");
    result.add(FIELDS_KEY, bbgTickAsFudgMsg);
    return result;
  }

  /**
   * Gets an example equity option ticker directly from Bloomberg reference data.
   * 
   * @return the ticker, not null
   */
  public static String getSampleEquityOptionTicker() {
    BloombergReferenceDataProvider rdp = new BloombergReferenceDataProvider(getBloombergConnector());
    rdp.start();
    
    Set<ExternalId> options = BloombergDataUtils.getOptionChain(rdp, "AAPL US Equity");
    assertEquals(false, options.isEmpty());
    ExternalId aaplOptionId = options.iterator().next();
    
    rdp.stop();
    
    return aaplOptionId.getValue();
  }

}
