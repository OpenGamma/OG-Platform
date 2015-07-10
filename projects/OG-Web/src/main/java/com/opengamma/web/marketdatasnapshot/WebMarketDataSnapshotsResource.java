/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.marketdatasnapshot;

import static org.threeten.bp.temporal.ChronoUnit.SECONDS;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.live.LiveMarketDataProviderFactory;
import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter.Mode;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.tool.marketdata.MarketDataSnapshotSaver;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchSortOrder;
import com.opengamma.master.marketdatasnapshot.impl.MarketDataSnapshotSearchIterator;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;

/**
 * RESTful resource for all market data snapshot documents.
 * <p>
 * The market data snapshot documents resource represents the whole of a position master.
 * 
 */
@SuppressWarnings("deprecation")
@Path("/datasnapshots")
public class WebMarketDataSnapshotsResource extends AbstractWebMarketDataSnapshotResource {
  
  private static final String CUSTOM_DATE_SUFFIX = "_CustomDate";
  private static final Logger s_logger = LoggerFactory.getLogger(WebMarketDataSnapshotsResource.class);
  /** Time format: HH:mm:ss */
  private static final DateTimeFormatter VALUATION_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final DateTimeFormatter HISORICAL_SNAPSHOT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  
  /**
   * Creates the resource.
   * @param marketSnapshotMaster  the market data snapshot master, not null
   * @param configMaster  the config master, not null
   * @param liveMarketDataProviderFactory the live market data provider factory, Either this or marketDataSpecificationRepository must be set
   * @param marketDataSpecificationRepository the market data specification repository
   * @param configSource the config source, not null
   * @param targetResolver the computation target resolver, not null
   * @param viewProcessor the view processor, not null
   * @param htsSource the historical timeseries source, not null
   * @param volatilityCubeDefinitionSource the volatility cube definition source, not null
   */
  public WebMarketDataSnapshotsResource(final MarketDataSnapshotMaster marketSnapshotMaster, final ConfigMaster configMaster, 
      final LiveMarketDataProviderFactory liveMarketDataProviderFactory, final NamedMarketDataSpecificationRepository marketDataSpecificationRepository, final ConfigSource configSource,
      final ComputationTargetResolver targetResolver, final ViewProcessor viewProcessor, final HistoricalTimeSeriesSource htsSource,
      final VolatilityCubeDefinitionSource volatilityCubeDefinitionSource) {
    super(marketSnapshotMaster, configMaster, liveMarketDataProviderFactory, marketDataSpecificationRepository, configSource, targetResolver, viewProcessor, htsSource, volatilityCubeDefinitionSource);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.MARKET_DATA_SNAPSHOT)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("snapshotId") List<String> snapshotIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    MarketDataSnapshotSearchSortOrder so = buildSortOrder(sort, MarketDataSnapshotSearchSortOrder.NAME_ASC);
    FlexiBean out = search(pr, so, name, snapshotIdStrs, uriInfo);
    return getFreemarker().build(HTML_DIR + "snapshots.ftl", out);
  }

  private FlexiBean search(PagingRequest request, MarketDataSnapshotSearchSortOrder so, String name, 
      List<String> snapshotIdStrs, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setPagingRequest(request);
    searchRequest.setSortOrder(so);
    searchRequest.setName(StringUtils.trimToNull(name));
    searchRequest.setIncludeData(false);
    out.put("searchRequest", searchRequest);
    for (String snapshotIdStr : snapshotIdStrs) {
      searchRequest.addMarketDataSnapshotId(ObjectId.parse(snapshotIdStr));
    }
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      MarketDataSnapshotSearchResult searchResult = data().getMarketDataSnapshotMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    } 
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{snapshotId}")
  public WebMarketDataSnapshotResource findSnapshot(@Subscribe @PathParam("snapshotId") String idStr) {
    data().setUriSnapshotId(idStr);
    UniqueId oid = UniqueId.parse(idStr);
    try {
      MarketDataSnapshotDocument doc = data().getMarketDataSnapshotMaster().get(oid);
      data().setSnapshot(doc);
    } catch (DataNotFoundException ex) {
      MarketDataSnapshotHistoryRequest historyRequest = new MarketDataSnapshotHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      MarketDataSnapshotHistoryResult historyResult = data().getMarketDataSnapshotMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setSnapshot(historyResult.getFirstDocument());
    }
    return new WebMarketDataSnapshotResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    out.put("searchRequest", searchRequest);
    out.put("views", getViewNames());
    out.put("liveDataSources", getLiveDataSources());
    out.put("timeseriesresolverkeys", getTimeSeriesResolverKeys());
    out.put("snapshots", getSnapshots());
    return out;
  }
  
  private List<ManageableMarketDataSnapshot> getSnapshots() {
    MarketDataSnapshotSearchRequest snapshotSearchRequest = new MarketDataSnapshotSearchRequest();
    snapshotSearchRequest.setIncludeData(false);
    
    List<ManageableMarketDataSnapshot> snapshots = Lists.newArrayList();
    for (MarketDataSnapshotDocument doc : MarketDataSnapshotSearchIterator.iterable(data().getMarketDataSnapshotMaster(), snapshotSearchRequest)) {
      snapshots.add(doc.getSnapshot());
    }
    return snapshots;
  }

  private List<String> getTimeSeriesResolverKeys() {
    ConfigSearchRequest<HistoricalTimeSeriesRating> request =
        new ConfigSearchRequest<HistoricalTimeSeriesRating>(HistoricalTimeSeriesRating.class);
    final List<String> keyNames = Lists.newArrayList();
    for (ConfigDocument doc : ConfigSearchIterator.iterable(data().getConfigMaster(), request)) {
      keyNames.add(doc.getName());
    }
    return keyNames;
  }

  private List<String> getLiveDataSources() {
    List<String> liveDataSources;
    if (data().getLiveMarketDataProviderFactory() != null) {
      liveDataSources = data().getLiveMarketDataProviderFactory().getProviderNames();
    } else if (data().getMarketDataSpecificationRepository() != null) {
      liveDataSources = data().getMarketDataSpecificationRepository().getNames();
    } else {
      liveDataSources = ImmutableList.of();
    }
    return liveDataSources;
  }

  private List<String> getViewNames() {
    ConfigMaster configMaster = data().getConfigMaster();
    ConfigSearchRequest<ViewDefinition> request = new ConfigSearchRequest<ViewDefinition>();
    request.setType(ViewDefinition.class);
    
    List<String> viewNames = Lists.newArrayList();
    for (ConfigDocument doc : ConfigSearchIterator.iterable(configMaster, request)) {
      ViewDefinition viewDefintion = (ViewDefinition) doc.getValue().getValue();
      if (viewDefintion.getName() != null) {
        viewNames.add(viewDefintion.getName());
      }
    }
    Collections.sort(viewNames);
    return viewNames;
  }
  
  
  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("name") String name,
      @FormParam("view") String view,
      @FormParam("valuationTime") String valuationTime,
      @FormParam("tsResolverKeys") List<String> tsResolverKeys,
      @FormParam("userSnapshotIds") List<String> userSnapshotIds,
      @FormParam("liveDataSources") List<String> liveDataSources,
      MultivaluedMap<String, String> formParameters) {
    name = StringUtils.trimToNull(name);
    view = StringUtils.trimToNull(view);
    valuationTime = StringUtils.trimToNull(valuationTime);
    
    Instant valuationInstant = null;
    if (valuationTime != null) {
      try {
        final LocalTime time = LocalTime.parse(valuationTime, VALUATION_TIME_FORMATTER);
        valuationInstant = ZonedDateTime.now().with(time.truncatedTo(SECONDS)).toInstant();
      } catch (Exception ex) {
        s_logger.warn("Invalid valuation time {}", valuationTime);
      }
    } else {
      valuationInstant = Instant.now();
    }
    
    final List<MarketDataSpecification> marketDataSpecs = getMarketDataSpecs(tsResolverKeys, userSnapshotIds, liveDataSources, formParameters);
    if (view == null || marketDataSpecs.isEmpty() || valuationInstant == null) {
      FlexiBean out = createRootData();
      if (valuationInstant == null) {
        out.put("err_valutionTimeInvalid", true);
      }
      if (view == null) { 
        out.put("err_viewMissing", true);
      } else {
        out.put("selectedView", view);
      }
      if (marketDataSpecs.isEmpty()) {
        out.put("err_marketDataSpecsMissing", true);
      } else {
        out.put("selectedTSResolverMap", getTsResolverKeyInputs(tsResolverKeys, formParameters));
        out.put("selectedUserSnapshotIds", userSnapshotIds);
        out.put("selectedLiveDataSources", liveDataSources);
      }
      
      //include other inputs
      MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
      searchRequest.setName(name);
      out.put("searchRequest", searchRequest);
      out.put("valuationTime", valuationTime);
      String html = getFreemarker().build(HTML_DIR + "snapshot-add.ftl", out);
      return Response.ok(html).build();
    }
   
    MarketDataSnapshotDocument snapshot = null;
    try {
      snapshot = createSnapshot(name, view, valuationInstant, marketDataSpecs);
    } catch (Exception ex) {
      s_logger.error("Unable to create market data snapshot");
      throw new OpenGammaRuntimeException("Unable to create market data snapshot");
    }
    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(snapshot.getUniqueId().toLatest().toString()).build();
    return Response.seeOther(uri).build();
  }

  private Map<String, String> getTsResolverKeyInputs(List<String> tsResolverKeys, MultivaluedMap<String, String> formParameters) {
    Map<String, String> result = Maps.newHashMap();
    for (String tsResolverKey : tsResolverKeys) {
      List<String> tsDates = formParameters.get(tsResolverKey + CUSTOM_DATE_SUFFIX);
      if (!tsDates.isEmpty()) {
        result.put(tsResolverKey, Iterables.getFirst(tsDates, null));
      }
    }
    return result;
  }

  private List<MarketDataSpecification> getMarketDataSpecs(List<String> tsResolverKeys, List<String> userSnapshotIds, List<String> liveDataSources, MultivaluedMap<String, String> formParameters) {
    List<MarketDataSpecification> marketDataSpecs = Lists.newArrayList();
    for (String liveDataSource : liveDataSources) {
      liveDataSource = StringUtils.trimToNull(liveDataSource);
      if (liveDataSource != null) {
        marketDataSpecs.add(LiveMarketDataSpecification.of(liveDataSource));
      }
    }
    for (String tsResolverKey : tsResolverKeys) {
      List<String> tsDates = formParameters.get(tsResolverKey + CUSTOM_DATE_SUFFIX);
      if (tsDates.isEmpty()) {
        marketDataSpecs.add(new LatestHistoricalMarketDataSpecification(tsResolverKey));
      } else {
        String tsDate = StringUtils.trimToNull(Iterables.getFirst(tsDates, null));
        if (tsDate != null) {
          LocalDate snapshotDate = LocalDate.parse(tsDate, HISORICAL_SNAPSHOT_DATE_FORMATTER);
          marketDataSpecs.add(new FixedHistoricalMarketDataSpecification(tsResolverKey, snapshotDate));
        }
      }
    }
    
    for (String userSnapshotId : userSnapshotIds) {
      userSnapshotId = StringUtils.trimToNull(userSnapshotId);
      if (userSnapshotId != null) {
        try {
          marketDataSpecs.add(UserMarketDataSpecification.of(UniqueId.parse(userSnapshotId)));
        } catch (IllegalArgumentException ex) {
          s_logger.warn("Illegal format in snapshot {}, ignorning... ", userSnapshotId);
        }
      }
    }
    return marketDataSpecs;
  }

  private MarketDataSnapshotDocument createSnapshot(String name, final String viewDefinitionName, final Instant valuationInstant, 
      final List<MarketDataSpecification> marketDataSpecs) throws InterruptedException {
    MarketDataSnapshotSaver saver = MarketDataSnapshotSaver.of(data().getComputationTargetResolver(), data().getHistoricalTimeSeriesSource(), 
        data().getViewProcessor(), data().getConfigMaster(), data().getMarketDataSnapshotMaster(), data().getVolatilityCubeDefinitionSource(), Mode.STRUCTURED, null);
    return saver.createSnapshot(name, viewDefinitionName, valuationInstant, marketDataSpecs);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for snapshots.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebMarketDataSnapshotData data) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebMarketDataSnapshotsResource.class);
    return builder.build();
  }
 
}
