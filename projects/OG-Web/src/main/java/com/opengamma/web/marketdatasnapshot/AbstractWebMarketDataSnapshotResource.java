/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.marketdatasnapshot;

import org.fudgemsg.FudgeContext;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.live.LiveMarketDataProviderFactory;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful market data snapshot resources.
 */
@SuppressWarnings("deprecation")
public abstract class AbstractWebMarketDataSnapshotResource
    extends AbstractPerRequestWebResource<WebMarketDataSnapshotData> {

  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "marketdatasnapshots/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "marketdatasnapshots/json/";

  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();

  /**
   * Creates the resource.
   * 
   * @param marketdataSnapshotMaster  the market data snapshot master, not null
   * @param configMaster  the config master, not null
   * @param liveMarketDataProviderFactory  the live market data provider factory, Either this or marketDataSpecificationRepository must be set
   * @param marketDataSpecificationRepository  the market data specification repository, not null
   * @param configSource  the config source, not null
   * @param targetResolver  the computation target resolver, not null
   * @param viewProcessor  the view processor, not null
   * @param htsSource  the historical timeseries source, not null
   * @param volatilityCubeDefinitionSource  the volatility cube definition source, not null
   */
  protected AbstractWebMarketDataSnapshotResource(final MarketDataSnapshotMaster marketdataSnapshotMaster, final ConfigMaster configMaster,
      final LiveMarketDataProviderFactory liveMarketDataProviderFactory, final NamedMarketDataSpecificationRepository marketDataSpecificationRepository, final ConfigSource configSource,
      final ComputationTargetResolver targetResolver, final ViewProcessor viewProcessor, final HistoricalTimeSeriesSource htsSource,
      final VolatilityCubeDefinitionSource volatilityCubeDefinitionSource) {
    super(new WebMarketDataSnapshotData());
    ArgumentChecker.notNull(marketdataSnapshotMaster, "marketdataSnapshotMaster");
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(configSource, "configSource");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(htsSource, "htsSource");
    ArgumentChecker.isFalse(liveMarketDataProviderFactory == null && marketDataSpecificationRepository == null, "liveMarketDataProviderFactory or marketDataSpecificationRepository must be set");
    ArgumentChecker.notNull(volatilityCubeDefinitionSource, "volatilityCubeDefinitionSource");
    
    data().setMarketDataSnapshotMaster(marketdataSnapshotMaster);
    data().setConfigMaster(configMaster);
    data().setMarketDataSpecificationRepository(marketDataSpecificationRepository);
    data().setLiveMarketDataProviderFactory(liveMarketDataProviderFactory);
    data().setConfigSource(configSource);
    data().setComputationTargetResolver(targetResolver);
    data().setViewProcessor(viewProcessor);
    data().setViewProcessor(viewProcessor);
    data().setHistoricalTimeSeriesSource(htsSource);
    data().setVolatilityCubeDefinitionSource(volatilityCubeDefinitionSource);
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebMarketDataSnapshotResource(final AbstractWebMarketDataSnapshotResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * 
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    out.put("uris", new WebMarketDataSnapshotUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

}
