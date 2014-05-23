/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.client.JmsLiveDataClient;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.test.LiveDataClientTestUtils;
import com.opengamma.livedata.test.StandardRulesUtils;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.INTEGRATION)
public class BloombergBpipeLiveDataServerTest {

  private final static UserPrincipal TEST_USER = UserPrincipal.getTestUser();

  private BloombergLiveDataServer _server;
  private BloombergReferenceDataProvider _refernceDataProvider;
  private JmsLiveDataClient _liveDataClient;

  @BeforeClass
  public void setUpClass() throws Exception {
    BloombergReferenceDataProvider referenceDataProvider = new BloombergReferenceDataProvider(BloombergTestUtils.getBloombergBipeConnector());
    referenceDataProvider.start();
    _refernceDataProvider = referenceDataProvider;

    FudgeMessageSender fudgeMessageSender = new FudgeMessageSender() {
      @Override
      public void send(FudgeMsg message) {
        // do nothing
      }

      @Override
      public FudgeContext getFudgeContext() {
        return OpenGammaFudgeContext.getInstance();
      }
    };
    BloombergLiveDataServer server = new BloombergLiveDataServer(BloombergTestUtils.getBloombergBipeConnector(),
        _refernceDataProvider,
        EHCacheUtils.createCacheManager(),
        fudgeMessageSender);
    DistributionSpecificationResolver distributionSpecificationResolver = server.getDefaultDistributionSpecificationResolver();
    server.setDistributionSpecificationResolver(distributionSpecificationResolver);

    server.start();
    _server = server;
    if (StandardLiveDataServer.ConnectionStatus.NOT_CONNECTED.equals(_server.getConnectionStatus()) == true) {
      Thread.sleep(1000);
    }
    _liveDataClient = LiveDataClientTestUtils.getJmsClient(_server);
  }

  @AfterClass
  public void tearDownClass() {
    _liveDataClient.stop();
    if (_refernceDataProvider != null) {
      _refernceDataProvider.stop();
    }
    if (_server != null) {
      _server.stop();
    }

  }

  //-------------------------------------------------------------------------
  @Test
  public void testSnapshot() {
    LiveDataSubscriptionResponse snapshotResponse = snapshot("IBM US Equity");

    assertNotNull(snapshotResponse);
    assertEquals(LiveDataSubscriptionResult.SUCCESS, snapshotResponse.getSubscriptionResult());
    StandardRulesUtils.validateOpenGammaMsg(snapshotResponse.getSnapshot().getFields());
  }

  private LiveDataSubscriptionResponse snapshot(String ticker) {
    LiveDataSpecification requestedSpecification = new LiveDataSpecification(
        _liveDataClient.getDefaultNormalizationRuleSetId(),
        ExternalSchemes.bloombergTickerSecurityId(ticker));
    return _liveDataClient.snapshot(TEST_USER, requestedSpecification, 3000);
  }

}
