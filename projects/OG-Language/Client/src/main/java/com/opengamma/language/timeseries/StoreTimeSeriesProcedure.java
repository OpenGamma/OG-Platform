/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.timeseries;

import java.util.Arrays;
import java.util.List;

import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.UniqueId;
import com.opengamma.language.client.ContextRemoteClient;
import com.opengamma.language.client.MasterID;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Stores a time series into the {@link HistoricalTimeSeriesMaster}.
 */
public class StoreTimeSeriesProcedure extends AbstractProcedureInvoker.SingleResult implements PublishedProcedure {

  /**
   * Default instance.
   */
  public static final StoreTimeSeriesProcedure INSTANCE = new StoreTimeSeriesProcedure();

  private final MetaProcedure _meta;

  private static final int TIMESERIES = 0;
  private static final int NAME = 1;
  private static final int IDENTIFIER = 2;
  private static final int DATA_FIELD = 3;
  private static final int DATA_SOURCE = 4;
  private static final int DATA_PROVIDER = 5;
  private static final int OBSERVATION_TIME = 6;
  private static final int MASTER = 7;

  private static List<MetaParameter> parameters() {
    final MetaParameter timeseries = new MetaParameter("timeseries", JavaTypeInfo.builder(LocalDateDoubleTimeSeries.class).get());
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).get());
    final MetaParameter identifier = new MetaParameter("identifier", JavaTypeInfo.builder(ExternalIdBundle.class).get());
    final MetaParameter dataField = new MetaParameter("dataField", JavaTypeInfo.builder(String.class).get());
    final MetaParameter dataSource = new MetaParameter("dataSource", JavaTypeInfo.builder(String.class).get());
    final MetaParameter dataProvider = new MetaParameter("dataProvider", JavaTypeInfo.builder(String.class).get());
    final MetaParameter observationTime = new MetaParameter("observationTime", JavaTypeInfo.builder(String.class).get());
    final MetaParameter master = new MetaParameter("master", JavaTypeInfo.builder(MasterID.class).defaultValue(MasterID.SESSION).get());
    return Arrays.asList(timeseries, name, identifier, dataField, dataSource, dataProvider, observationTime, master);
  }

  private StoreTimeSeriesProcedure(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaProcedure(Categories.TIMESERIES, "StoreTimeSeries", getParameters(), this));
  }

  protected StoreTimeSeriesProcedure() {
    this(new DefinitionAnnotater(StoreTimeSeriesProcedure.class));
  }

  protected static UniqueId invoke(final HistoricalTimeSeriesMaster master, final LocalDateDoubleTimeSeries timeSeries, final String name, final ExternalIdBundle identifier, final String dataField,
      final String dataSource, final String dataProvider, final String observationTime) {
    final HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest();
    for (ExternalId externalId : identifier) {
      searchRequest.addExternalId(externalId);
    }
    searchRequest.setDataField(dataField);
    searchRequest.setDataSource(dataSource);
    searchRequest.setDataProvider(dataProvider);
    searchRequest.setObservationTime(observationTime);
    final HistoricalTimeSeriesInfoSearchResult searchResult = master.search(searchRequest);
    HistoricalTimeSeriesInfoDocument document;
    final List<HistoricalTimeSeriesInfoDocument> documents = searchResult.getDocuments();
    if (documents.isEmpty()) {
      // New time-series
      final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
      info.setName(name);
      info.setExternalIdBundle(ExternalIdBundleWithDates.of(ExternalIdBundle.of(identifier)));
      info.setDataField(dataField);
      info.setDataSource(dataSource);
      info.setDataProvider(dataProvider);
      info.setObservationTime(observationTime);
      document = master.add(new HistoricalTimeSeriesInfoDocument(info));
    } else if (documents.size() != 1) {
      // Update to existing one
      throw new InvokeInvalidArgumentException(IDENTIFIER, "Found " + documents.size() + " matching time-series");
    } else {
      document = documents.get(0);
    }
    if (!name.equals(document.getInfo().getName())) {
      // Update the name
      document.getInfo().setName(name);
      document = master.update(document);
    }
    return master.correctTimeSeriesDataPoints(document, timeSeries);
  }

  public static final UniqueId invoke(final SessionContext sessionContext, final LocalDateDoubleTimeSeries timeSeries, final String name, final ExternalIdBundle identifier, final String dataField,
      final String dataSource,
      final String dataProvider, final String observationTime, final MasterID master) {
    final RemoteClient client = ContextRemoteClient.get(sessionContext, master);
    final HistoricalTimeSeriesMaster tsMaster;
    try {
      tsMaster = client.getHistoricalTimeSeriesMaster();
    } catch (UnsupportedOperationException e) {
      throw new InvokeInvalidArgumentException(MASTER, e);
    }
    return invoke(tsMaster, timeSeries, name, identifier, dataField, dataSource, dataProvider, observationTime);
  }

  // AbstractProcedureInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (LocalDateDoubleTimeSeries) parameters[TIMESERIES], (String) parameters[NAME], (ExternalIdBundle) parameters[IDENTIFIER], (String) parameters[DATA_FIELD],
        (String) parameters[DATA_SOURCE], (String) parameters[DATA_PROVIDER], (String) parameters[OBSERVATION_TIME], (MasterID) parameters[MASTER]);
  }

  // PublishedProcedure

  @Override
  public MetaProcedure getMetaProcedure() {
    return _meta;
  }

}
