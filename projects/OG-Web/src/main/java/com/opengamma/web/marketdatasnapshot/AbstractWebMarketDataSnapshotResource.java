/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.marketdatasnapshot;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.live.LiveMarketDataProviderFactory;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.WebHomeUris;

/**
 * Abstract base class for RESTful market data snapshot resources.
 * 
 */
public abstract class AbstractWebMarketDataSnapshotResource extends AbstractPerRequestWebResource {
    
  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "marketdatasnapshots/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "marketdatasnapshots/json/";
  
  /**
   * Logger.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractWebMarketDataSnapshotResource.class);
  
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  /**
   * The backing bean.
   */
  private final WebMarketDataSnapshotData _data;

  /**
   * Creates the resource.
   * @param marketdataSnapshotMaster  the market data snapshot master, not null
   * @param configMaster  the config master, not null
   * @param liveMarketDataProviderFactory the live market data provider factory, Either this or marketDataSpecificationRepository must be set
   * @param marketDataSpecificationRepository the market data specification repository, not null
   * @param configSource the config source, not null
   * @param targetResolver the computation target resolver, not null
   * @param viewProcessor the view processor, not null
   * @param htsSource the historical timeseries source, not null
   */
  protected AbstractWebMarketDataSnapshotResource(final MarketDataSnapshotMaster marketdataSnapshotMaster, final ConfigMaster configMaster, 
      final LiveMarketDataProviderFactory liveMarketDataProviderFactory, final NamedMarketDataSpecificationRepository marketDataSpecificationRepository, final ConfigSource configSource,
      final ComputationTargetResolver targetResolver, final ViewProcessor viewProcessor, final HistoricalTimeSeriesSource htsSource) {
    ArgumentChecker.notNull(marketdataSnapshotMaster, "marketdataSnapshotMaster");
    ArgumentChecker.notNull(configMaster, "configMaster");
//    ArgumentChecker.notNull(marketDataSpecificationRepository, "marketDataSpecificationRepository");
    ArgumentChecker.notNull(configSource, "configSource");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(htsSource, "htsSource");
    ArgumentChecker.isFalse(liveMarketDataProviderFactory == null && marketDataSpecificationRepository == null, "liveMarketDataProviderFactory or marketDataSpecificationRepository must be set");
    
    _data = new WebMarketDataSnapshotData();
    data().setMarketDataSnapshotMaster(marketdataSnapshotMaster);
    data().setConfigMaster(configMaster);
    data().setMarketDataSpecificationRepository(marketDataSpecificationRepository);
    data().setLiveMarketDataProviderFactory(liveMarketDataProviderFactory);
    data().setConfigSource(configSource);
    data().setComputationTargetResolver(targetResolver);
    data().setViewProcessor(viewProcessor);
    data().setViewProcessor(viewProcessor);
    data().setHistoricalTimeSeriesSource(htsSource);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebMarketDataSnapshotResource(final AbstractWebMarketDataSnapshotResource parent) {
    super(parent);
    _data = parent._data;
  }
  
  /**
   * Setter used to inject the URIInfo.
   * This is a roundabout approach, because Spring and JSR-311 injection clash.
   * DO NOT CALL THIS METHOD DIRECTLY.
   * @param uriInfo  the URI info, not null
   */
  @Context
  public void setUriInfo(final UriInfo uriInfo) {
    data().setUriInfo(uriInfo);
  }
  
  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = getFreemarker().createRootData();
    out.put("homeUris", new WebHomeUris(data().getUriInfo()));
    out.put("uris", new WebMarketDataSnapshotUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the backing bean, not null
   */
  protected WebMarketDataSnapshotData data() {
    return _data;
  }
  
  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
}
