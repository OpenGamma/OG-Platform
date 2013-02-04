/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.FixedMarketDataAvailabilityProvider;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.test.TestLiveDataClient;

/**
 * Test LiveDataSnapshotProvider.
 */
@Test
public class LiveMarketDataProviderTest {

  private static final String _marketDataRequirement = MarketDataRequirementNames.MARKET_VALUE;

  protected ValueSpecification constructSpecification(final String ticker) {
    return ValueSpecification.of(_marketDataRequirement, ComputationTargetType.PRIMITIVE, UniqueId.of("testdomain", ticker), ValueProperties.with(ValuePropertyNames.FUNCTION, "MarketData").get());
  }

  public void snapshotting() {
    final ValueSpecification test1Specification = constructSpecification("test1");
    final ValueSpecification test2Specification = constructSpecification("test2");
    final ValueSpecification test3Specification = constructSpecification("test3");

    final TestLiveDataClient client = new TestLiveDataClient();
    final FixedMarketDataAvailabilityProvider availabilityProvider = new FixedMarketDataAvailabilityProvider();
    availabilityProvider.addAvailableData(test1Specification);
    availabilityProvider.addAvailableData(test2Specification);
    availabilityProvider.addAvailableData(test3Specification);
    final LiveMarketDataProvider provider =
        new LiveMarketDataProvider(client, availabilityProvider, new InMemorySecuritySource(), UserPrincipal.getTestUser());

    provider.subscribe(test1Specification);
    provider.subscribe(test2Specification);

    provider.subscribe(test3Specification);
    provider.subscribe(test3Specification);
    provider.subscribe(test3Specification);

    final MutableFudgeMsg msg1 = new FudgeContext().newMessage();
    msg1.add(_marketDataRequirement, 52.07);

    final MutableFudgeMsg msg2 = new FudgeContext().newMessage();
    msg2.add(_marketDataRequirement, 52.15);

    final MutableFudgeMsg msg3a = new FudgeContext().newMessage();
    msg3a.add(_marketDataRequirement, 52.16);
    final MutableFudgeMsg msg3b = new FudgeContext().newMessage();
    msg3b.add(_marketDataRequirement, 52.17);

    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), ExternalId.of("testdomain", "test1")), msg1);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), ExternalId.of("testdomain", "test2")), msg2);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), ExternalId.of("testdomain", "test3")), msg3a);
    client.marketDataReceived(new LiveDataSpecification(client.getDefaultNormalizationRuleSetId(), ExternalId.of("testdomain", "test3")), msg3b);

    final MarketDataSnapshot snapshot = provider.snapshot(null);
    snapshot.init(Collections.<ValueSpecification>emptySet(), 0, TimeUnit.MILLISECONDS);

    final Double test1Value = (Double) snapshot.query(test1Specification);
    assertNotNull(test1Value);
    assertEquals(52.07, test1Value, 0.000001);

    final Double test2Value = (Double) snapshot.query(test2Specification);
    assertNotNull(test2Value);
    assertEquals(52.15, test2Value, 0.000001);

    final Double test3Value = (Double) snapshot.query(test3Specification);
    assertNotNull(test3Value);
    assertEquals(52.17, test3Value, 0.000001);

    assertNull(snapshot.query(constructSpecification("invalidticker")));
  }


}
