/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;
import java.util.Set;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.resolver.JmsTopicNameResolveRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BloombergJmsTopicNameResolverTest {

  private BloombergLiveDataServer _server;

  @BeforeClass
  public void setUpClass() {
    _server = BloombergLiveDataServerUtils.startTestServer(BloombergJmsTopicNameResolverTest.class);
  }

  @AfterClass
  public void tearDownClass() {
    BloombergLiveDataServerUtils.stopTestServer(_server);
  }

  //-------------------------------------------------------------------------
  @Test(enabled = false)
  private void testResolve(NormalizationRuleSet rules) {
    
    ReferenceDataProvider rdp = _server.getReferenceDataProvider();
    BloombergIdResolver idResolver = new BloombergIdResolver(rdp);
    
    BloombergJmsTopicNameResolver topicNameResolver = new BloombergJmsTopicNameResolver(rdp);
    
    ExternalId aaplEquity = idResolver.resolve(
        ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("AAPL US Equity")));
    String spec = topicNameResolver.resolve(new JmsTopicNameResolveRequest(aaplEquity, rules));
    assertEquals("LiveData.Bloomberg.Equity.NASDAQ GS.AAPL" + rules.getJmsTopicSuffix(), spec);
    
    ExternalId usDomesticBond = idResolver.resolve(
        ExternalIdBundle.of(ExternalSchemes.cusipSecurityId("607059AT9")));
    spec = topicNameResolver.resolve(new JmsTopicNameResolveRequest(usDomesticBond, rules));
    assertEquals("LiveData.Bloomberg.Bond.MOBIL CORP.607059AT9" + rules.getJmsTopicSuffix(), spec);
    
    ExternalId globalBond = idResolver.resolve(
        ExternalIdBundle.of(ExternalSchemes.cusipSecurityId("4581X0AD0")));
    spec = topicNameResolver.resolve(new JmsTopicNameResolveRequest(globalBond, rules));
    assertEquals("LiveData.Bloomberg.Bond.INTER-AMERICAN DEVEL BK.US4581X0AD07" + rules.getJmsTopicSuffix(), spec);
    
    Set<ExternalId> options = BloombergDataUtils.getOptionChain(rdp, "AAPL US Equity");
    assertFalse(options.isEmpty());
    ExternalId aaplOptionId = options.iterator().next();
    ExternalId aaplOption = idResolver.resolve(ExternalIdBundle.of(aaplOptionId));
    spec = topicNameResolver.resolve(new JmsTopicNameResolveRequest(aaplOption, rules));
    assertTrue(spec.startsWith("LiveData.Bloomberg.EquityOption.AAPL US."));
    assertTrue(spec.endsWith(rules.getJmsTopicSuffix()));
    
    // bulk request
    Map<JmsTopicNameResolveRequest, String> request2TopicName = topicNameResolver.resolve(
        Sets.newHashSet(
            new JmsTopicNameResolveRequest(aaplEquity, rules),
            new JmsTopicNameResolveRequest(usDomesticBond, rules)));
    assertEquals(2, request2TopicName.size());
    assertEquals("LiveData.Bloomberg.Equity.NASDAQ GS.AAPL" + rules.getJmsTopicSuffix(),
        request2TopicName.get(new JmsTopicNameResolveRequest(aaplEquity, rules)));
    assertEquals("LiveData.Bloomberg.Bond.MOBIL CORP.607059AT9" + rules.getJmsTopicSuffix(),
        request2TopicName.get(new JmsTopicNameResolveRequest(usDomesticBond, rules)));
  }

  @Test
  public void emptyNormalization() {
    testResolve(new NormalizationRuleSet(""));    
  }

  @Test
  public void nonEmptyNormalization() {
    testResolve(new NormalizationRuleSet("Test"));
  }

}
