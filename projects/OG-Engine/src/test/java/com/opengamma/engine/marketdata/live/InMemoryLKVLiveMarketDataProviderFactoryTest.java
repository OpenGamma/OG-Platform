/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class InMemoryLKVLiveMarketDataProviderFactoryTest {

  private final MarketDataProviderFactory _factory;
  private final LiveMarketDataProvider _defaultProvider;
  private final LiveMarketDataProvider _provider1;
  private final LiveMarketDataProvider _provider2;
  private final UserPrincipal _user;

  public InMemoryLKVLiveMarketDataProviderFactoryTest() {
    _user = UserPrincipal.getLocalUser();
    LiveDataFactory defaultFactory = mock(LiveDataFactory.class);
    LiveDataFactory factory1 = mock(LiveDataFactory.class);
    LiveDataFactory factory2 = mock(LiveDataFactory.class);
    _factory = new InMemoryLKVLiveMarketDataProviderFactory(defaultFactory, ImmutableMap.of("1", factory1, "2", factory2, "default", defaultFactory));
    _defaultProvider = mock(LiveMarketDataProvider.class);
    stub(defaultFactory.create(_user)).toReturn(_defaultProvider);
    _provider1 = mock(LiveMarketDataProvider.class);
    stub(factory1.create(_user)).toReturn(_provider1);
    _provider2 = mock(LiveMarketDataProvider.class);
    stub(factory2.create(_user)).toReturn(_provider2);
  }

  @Test
  public void createDefault() {
    MarketDataProvider provider = _factory.create(_user, LiveMarketDataSpecification.LIVE_SPEC);
    assertEquals(_defaultProvider, provider);
  }

  @Test
  public void createNamed() {
    MarketDataProvider provider1 = _factory.create(_user, LiveMarketDataSpecification.of("1"));
    assertEquals(provider1, _provider1);
    MarketDataProvider provider2 = _factory.create(_user, LiveMarketDataSpecification.of("2"));
    assertEquals(provider2, _provider2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void createMissing() {
    _factory.create(_user, LiveMarketDataSpecification.of("3"));
  }
}
