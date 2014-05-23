/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.google.common.collect.MapMaker;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for accessing a historical time-series resolver.
 */
public class DataHistoricalTimeSeriesResolverResource extends AbstractDataResource {

  private final HistoricalTimeSeriesResolver _underlying;
  private final FudgeContext _fudgeContext;
  private final Map<Resolve, HistoricalTimeSeriesResolutionResult> _cache = new MapMaker().weakValues().makeMap();

  public DataHistoricalTimeSeriesResolverResource(final HistoricalTimeSeriesResolver underlying, final FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected HistoricalTimeSeriesResolver getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * URL construction state.
   */
  public final class Resolve {

    private final ExternalIdBundle _identifierBundle;
    private final LocalDate _identifierValidityDate;
    private final String _dataSource;
    private final String _dataProvider;
    private final String _dataField;
    private final String _resolutionKey;

    private Resolve(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource, final String dataProvider, final String dataField,
        final String resolutionKey) {
      _identifierBundle = identifierBundle;
      _identifierValidityDate = identifierValidityDate;
      _dataSource = dataSource;
      _dataProvider = dataProvider;
      _dataField = dataField;
      _resolutionKey = resolutionKey;
    }

    @Path("id/{id}")
    public Resolve id(@PathParam("id") final String id) {
      if (_identifierBundle == null) {
        return new Resolve(ExternalIdBundle.of(ExternalId.parse(id)), _identifierValidityDate, _dataSource, _dataProvider, _dataField, _resolutionKey);
      } else {
        return new Resolve(_identifierBundle.withExternalId(ExternalId.parse(id)), _identifierValidityDate, _dataSource, _dataProvider, _dataField, _resolutionKey);
      }
    }

    @Path("identifierValidityDate/{date}")
    public Resolve identifierValidityDate(@PathParam("date") final String date) {
      return new Resolve(_identifierBundle, LocalDate.parse(date), _dataSource, _dataProvider, _dataField, _resolutionKey);
    }

    @Path("dataSource/{source}")
    public Resolve dataSource(@PathParam("source") final String source) {
      return new Resolve(_identifierBundle, _identifierValidityDate, source, _dataProvider, _dataField, _resolutionKey);
    }

    @Path("dataProvider/{provider}")
    public Resolve dataProvider(@PathParam("provider") final String provider) {
      return new Resolve(_identifierBundle, _identifierValidityDate, _dataSource, provider, _dataField, _resolutionKey);
    }

    @Path("dataField/{field}")
    public Resolve dataField(@PathParam("field") final String field) {
      return new Resolve(_identifierBundle, _identifierValidityDate, _dataSource, _dataProvider, field, _resolutionKey);
    }

    @Path("resolutionKey/{key}")
    public Resolve resolutionKey(@PathParam("key") final String key) {
      return new Resolve(_identifierBundle, _identifierValidityDate, _dataSource, _dataProvider, _dataField, key);
    }

    private HistoricalTimeSeriesResolutionResult resolve() {
      HistoricalTimeSeriesResolutionResult hts = _cache.get(this);
      if (hts == null) {
        hts = getUnderlying().resolve(_identifierBundle, _identifierValidityDate, _dataSource, _dataProvider, _dataField, _resolutionKey);
        if (hts == null) {
          return null;
        }
        _cache.put(this, hts);
      }
      return hts;
    }

    @GET
    public Response get() {
      final HistoricalTimeSeriesResolutionResult hts = resolve();
      if (hts == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      final FudgeSerializer fsc = new FudgeSerializer(getFudgeContext());
      final MutableFudgeMsg response = fsc.newMessage();
      fsc.addToMessageWithClassHeaders(response, "info", null, hts.getHistoricalTimeSeriesInfo(), ManageableHistoricalTimeSeriesInfo.class);
      if (hts.getAdjuster() != null) {
        response.add("adjustment", hts.getAdjuster().getAdjustment(_identifierBundle).toString());
      }
      return responseOk(response);
    }

    @GET
    @Path("adjustment")
    public Response adjustment(@QueryParam("id") List<String> idStrs) {
      final HistoricalTimeSeriesResolutionResult hts = resolve();
      if (hts == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      final ExternalIdBundle bundle = ExternalIdBundle.parse(idStrs);
      final MutableFudgeMsg response = getFudgeContext().newMessage();
      response.add("adjustment", hts.getAdjuster().getAdjustment(bundle).toString());
      return responseOk(response);
    }

  }

  @Path("resolve")
  public Resolve resolve() {
    return new Resolve(null, null, null, null, null, null);
  }

}
