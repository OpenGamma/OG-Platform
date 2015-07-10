/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Market data factory that requests data from one or more live data sources.
 */
public class LiveMarketDataFactory implements MarketDataFactory<LiveMarketDataSpecification> {

  /** Sources of live data, keyed by data source name. */
  private final Map<String, LiveDataManager> _liveDataManagerBySource;

  /**
   * @param providers meta data about the live data sources
   * @param jmsConnector for connecting to the live data sources
   */
  public LiveMarketDataFactory(Collection<LiveDataMetaDataProvider> providers, JmsConnector jmsConnector) {
    ImmutableMap.Builder<String, LiveDataManager> builder = ImmutableMap.builder();

    for (LiveDataMetaDataProvider provider : providers) {
      LiveDataClient liveDataClient = MarketDataUtils.createLiveDataClient(provider, jmsConnector);
      builder.put(provider.metaData().getDescription(), new DefaultLiveDataManager(liveDataClient));
    }
    _liveDataManagerBySource = builder.build();
  }

  @Override
  public Class<LiveMarketDataSpecification> getSpecificationType() {
    return LiveMarketDataSpecification.class;
  }

  @Override
  public MarketDataSource create(LiveMarketDataSpecification spec) {
    String dataSourceName = spec.getDataSource();
    LiveDataManager liveDataManager = _liveDataManagerBySource.get(dataSourceName);

    if (liveDataManager == null) {
      throw new IllegalArgumentException("Unsupported live data source: " + dataSourceName);
    }
    LDClient liveDataClient = new LDClient(liveDataManager);
    return new DataSource(liveDataClient, dataSourceName);
  }

  /**
   * Data source that requests live data from a {@link LDClient}.
   */
  private static class DataSource implements MarketDataSource {

    /** The live data client. */
    private final LDClient _liveDataClient;

    /** The name of the live data source. */
    private final String _dataSourceName;

    public DataSource(LDClient liveDataClient, String dataSourceName) {
      _dataSourceName = dataSourceName;
      _liveDataClient = ArgumentChecker.notNull(liveDataClient, "liveDataClient");
    }

    @Override
    public Map<MarketDataRequest, Result<?>> get(Set<MarketDataRequest> requests) {
      Set<ExternalIdBundle> requestIds = new HashSet<>();

      for (MarketDataRequest request : requests) {
        requestIds.add(request.getId());
      }
      _liveDataClient.subscribe(requestIds);
      _liveDataClient.waitForSubscriptions();
      ImmutableLiveDataResults results = _liveDataClient.retrieveLatestData();
      ImmutableMap.Builder<MarketDataRequest, Result<?>> builder = ImmutableMap.builder();

      for (MarketDataRequest request : requests) {
        LiveDataResult liveDataResult = results.get(request.getId());

        if (liveDataResult != null) {
          builder.put(request, liveDataResult.getValue(request.getFieldName()));
        } else {
          builder.put(request, Result.failure(FailureStatus.MISSING_DATA,
                                              "No live data available for {} from {}",
                                              request, _dataSourceName));
        }
      }
      return builder.build();
    }
  }
}
