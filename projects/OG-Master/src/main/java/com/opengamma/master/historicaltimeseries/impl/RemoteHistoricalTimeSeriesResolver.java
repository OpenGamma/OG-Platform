/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.threeten.bp.LocalDate;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to a {@link HistoricalTimeSeriesResolver}.
 */
public class RemoteHistoricalTimeSeriesResolver extends AbstractRemoteClient implements HistoricalTimeSeriesResolver {


  private final ChangeManager _changeManager;

  public RemoteHistoricalTimeSeriesResolver(final URI baseUri) {
    super(baseUri);
    _changeManager = new BasicChangeManager();
  }

  public RemoteHistoricalTimeSeriesResolver(final URI baseUri, ChangeManager changeManager) {
    super(baseUri);
    _changeManager = changeManager;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  private class Adjuster implements HistoricalTimeSeriesAdjuster {
    
    private final URI _base;
    private final ExternalIdBundle _bundle;
    private final String _adjustment;

    public Adjuster(final URI base, final ExternalIdBundle bundle, final String adjustment) {
      _base = base;
      _bundle = bundle;
      _adjustment = adjustment;
    }

    @Override
    public HistoricalTimeSeries adjust(final ExternalIdBundle securityIdBundle, final HistoricalTimeSeries timeSeries) {
      return getAdjustment(securityIdBundle).adjust(timeSeries);
    }

    private String getAdjustmentString(final ExternalIdBundle securityIdBundle) {
      if (_bundle.equals(securityIdBundle)) {
        return _adjustment;
      } else {
        final URI uri = UriBuilder.fromUri(_base).path("adjustment").queryParam("id", securityIdBundle.toStringList().toArray()).build();
        final FudgeMsg response = accessRemote(uri).get(FudgeMsg.class);
        return response.getString("adjustment");
      }
    }

    @Override
    public HistoricalTimeSeriesAdjustment getAdjustment(final ExternalIdBundle securityIdBundle) {
      return HistoricalTimeSeriesAdjustment.parse(getAdjustmentString(securityIdBundle));
    }

  }

  @Override
  public HistoricalTimeSeriesResolutionResult resolve(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
      final String dataSource, final String dataProvider, final String dataField, final String resolutionKey) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    ArgumentChecker.notNull(dataField, "dataField");
    try {
      final FudgeDeserializer fdc = new FudgeDeserializer(getFudgeContext());
      final UriBuilder uri = UriBuilder.fromUri(getBaseUri()).path("resolve");
      for (ExternalId id : identifierBundle) {
        uri.segment("id", id.toString());
      }
      if (identifierValidityDate != null) {
        uri.segment("identifierValidityDate", identifierValidityDate.toString());
      }
      if (dataSource != null) {
        uri.segment("dataSource", dataSource);
      }
      if (dataProvider != null) {
        uri.segment("dataProvider", dataProvider);
      }
      if (dataField != null) {
        uri.segment("dataField", dataField);
      }
      if (resolutionKey != null) {
        uri.segment("resolutionKey", resolutionKey);
      }
      final URI req = uri.build();
      final FudgeMsg response = accessRemote(req).get(FudgeMsg.class);
      final String adjustment = response.getString("adjustment");
      return new HistoricalTimeSeriesResolutionResult(
          fdc.fieldValueToObject(ManageableHistoricalTimeSeriesInfo.class, response.getByName("info")),
          (adjustment != null) ? new Adjuster(req, identifierBundle, adjustment) : null);
    } catch (UniformInterfaceException404NotFound e) {
      return null;
    }
  }

}
