/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class HistoricalShockMarketDataProviderTest {

  private static final ValueSpecification SPEC1 =
      new ValueSpecification("valueName1",
                             new ComputationTargetSpecification(ComputationTargetType.CURRENCY, UniqueId.of("id", "1")),
                             ValueProperties.with(ValuePropertyNames.FUNCTION, "function1").get());
  private static final ValueSpecification SPEC2 =
      new ValueSpecification("valueName2",
                             new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO, UniqueId.of("id", "2")),
                             ValueProperties.with(ValuePropertyNames.FUNCTION, "function2").get());
  private static final ValueSpecification SPEC3 =
      new ValueSpecification("valueName3",
                             new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("id", "3")),
                             ValueProperties.with(ValuePropertyNames.FUNCTION, "function3").get());
  private static final ValueSpecification SPEC4 =
      new ValueSpecification("valueName4",
                             new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("id", "4")),
                             ValueProperties.with(ValuePropertyNames.FUNCTION, "function4").get());

  @Test
  public void permissionProvider() {
    MarketDataProvider historicalProvider1 = mock(MarketDataProvider.class);
    MarketDataProvider historicalProvider2 = mock(MarketDataProvider.class);
    MarketDataProvider baseProvider = mock(MarketDataProvider.class);
    MarketDataPermissionProvider pp1 = mock(MarketDataPermissionProvider.class);
    MarketDataPermissionProvider pp2 = mock(MarketDataPermissionProvider.class);
    MarketDataPermissionProvider pp3 = mock(MarketDataPermissionProvider.class);
    when(historicalProvider1.getPermissionProvider()).thenReturn(pp1);
    when(historicalProvider2.getPermissionProvider()).thenReturn(pp2);
    when(baseProvider.getPermissionProvider()).thenReturn(pp3);
    Set<ValueSpecification> specs = ImmutableSet.of(SPEC1, SPEC2, SPEC3, SPEC4);
    when(pp1.checkMarketDataPermissions(UserPrincipal.getTestUser(), specs)).thenReturn(Collections.singleton(SPEC1));
    when(pp2.checkMarketDataPermissions(UserPrincipal.getTestUser(), specs)).thenReturn(Collections.singleton(SPEC2));
    when(pp3.checkMarketDataPermissions(UserPrincipal.getTestUser(), specs)).thenReturn(Collections.singleton(SPEC3));
    HistoricalShockMarketDataProvider provider =
        new HistoricalShockMarketDataProvider(historicalProvider1, historicalProvider2, baseProvider);
    assertEquals(ImmutableSet.of(SPEC1, SPEC2, SPEC3),
                 provider.getPermissionProvider().checkMarketDataPermissions(UserPrincipal.getTestUser(), specs));
  }

  @Test
  public void subscribe() {
    Provider provider1 = new Provider();
    Provider provider2 = new Provider();
    Provider baseProvider = new Provider();
    HistoricalShockMarketDataProvider shockProvider = new HistoricalShockMarketDataProvider(provider1, provider2, baseProvider);
    MarketDataListener listener = mock(MarketDataListener.class);
    shockProvider.addListener(listener);
    provider1.valueChanged(SPEC1);
    verify(listener).valuesChanged(Collections.singleton(SPEC1));
    provider1.valueChanged(SPEC2);
    verify(listener).valuesChanged(Collections.singleton(SPEC2));
    provider2.valueChanged(SPEC3);
    verify(listener).valuesChanged(Collections.singleton(SPEC3));
    baseProvider.valueChanged(SPEC4);
    verify(listener).valuesChanged(Collections.singleton(SPEC4));
    shockProvider.removeListener(listener);
    provider1.valueChanged(SPEC1);
    verifyNoMoreInteractions(listener);
  }

  private static class Provider extends AbstractHistoricalMarketDataProvider {

    private Provider() {
      super(mock(HistoricalTimeSeriesSource.class), mock(HistoricalTimeSeriesResolver.class));
    }

    @Override
    protected LocalDate getHistoricalResolutionDate(MarketDataSpecification marketDataSpec) {
      throw new UnsupportedOperationException("getHistoricalResolutionDate not implemented");
    }

    @Override
    public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
      throw new UnsupportedOperationException("snapshot not implemented");
    }

    @Override
    public void valueChanged(ValueSpecification specification) {
      super.valueChanged(specification);
    }
  }
}
