/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaldata.rest;

import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.DEFAULT_HISTORICALDATASOURCE_NAME;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.historicaldata.HistoricalDataSource;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful backend for {@link RemoteHistoricalDataSource}.
 */
@Path("historicalDataSource")
public class HistoricalDataSourceService {

  /**
   * Map of sources by name.
   */
  private final ConcurrentMap<String, HistoricalDataSourceResource> _dataSourceResourceMap = new ConcurrentHashMap<String, HistoricalDataSourceResource>();
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance using the specified Fudge context.
   * @param fudgeContext  the Fudge context, not null
   */
  public HistoricalDataSourceService(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * @return the Fudge context, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the map of data sources.
   * @return the map, unmodifiable, not null
   */
  protected ConcurrentMap<String, HistoricalDataSourceResource> getDataSourceResourceMap() {
    return _dataSourceResourceMap;
  }

  // -------------------------------------------------------------------------
  /**
   * Adds a data source resource by name.
   * @param name  the name, not null
   * @param resource  the resource, not null
   */
  protected void addHistoricalDataSource(final String name, final HistoricalDataSourceResource resource) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(resource, "resource");
    getDataSourceResourceMap().put(name, resource);
  }

  /**
   * Adds a data source by name.
   * @param name  the name, not null
   * @param source  the source, not null
   */
  protected void addHistoricalDataSource(final String name, final HistoricalDataSource source) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(source, "source");
    addHistoricalDataSource(name, new HistoricalDataSourceResource(getFudgeContext(), source));
  }

  /**
   * Adds a data source using the default name.
   * @param source  the source, not null
   */
  public void setHistoricalDataSource(final HistoricalDataSource source) {
    addHistoricalDataSource(DEFAULT_HISTORICALDATASOURCE_NAME, source);
  }

  /**
   * Adds a map of data sources.
   * @param sources  the source map, not null
   */
  public void setHistoricalDataSourceMap(Map<String, HistoricalDataSource> sources) {
    final ConcurrentMap<String, HistoricalDataSourceResource> map = getDataSourceResourceMap();
    map.clear();
    for (Map.Entry<String, HistoricalDataSource> entry : sources.entrySet()) {
      addHistoricalDataSource(entry.getKey(), entry.getValue());
    }
  }

  // -------------------------------------------------------------------------
  /**
   * RESTful method to find a data source by name.
   * @param name  the name from the URI, not null
   * @return the resource, null if not found
   */
  @Path("{name}")
  public HistoricalDataSourceResource findHistoricalDataSource(@PathParam("name") String name) {
    return getDataSourceResourceMap().get(name);
  }

}
