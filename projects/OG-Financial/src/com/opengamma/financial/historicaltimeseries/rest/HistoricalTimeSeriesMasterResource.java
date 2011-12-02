/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaltimeseries.rest;

import javax.time.calendar.LocalDate;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdFudgeBuilder;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * REST resource wrapper for a {@link HistoricalTimeSeriesMaster}.
 */
public class HistoricalTimeSeriesMasterResource {

  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * The underlying data master.
   */
  private final HistoricalTimeSeriesMaster _underlying;

  /**
   * Creates a resource to expose a source over REST.
   * @param fudgeContext  the context, not null
   * @param underlying the historical time-series master, not null
   */
  public HistoricalTimeSeriesMasterResource(final FudgeContext fudgeContext, final HistoricalTimeSeriesMaster underlying) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(underlying, "underlying");
    _fudgeContext = fudgeContext;
    _underlying = underlying;
  }

  /**
   * Returns the Fudge context.
   * 
   * @return the context
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Creates and returns a Fudge serializer based on the Fudge context.
   * 
   * @return the serializer
   */
  protected FudgeSerializer getFudgeSerializer() {
    return new FudgeSerializer(getFudgeContext());
  }

  /**
   * Creates and returns a Fudge deserializer based on the Fudge context.
   * 
   * @return the deserializer
   */
  protected FudgeDeserializer getFudgeDeserializer() {
    return new FudgeDeserializer(getFudgeContext());
  }

  /**
   * Returns the underlying master.
   * 
   * @return the master
   */
  protected HistoricalTimeSeriesMaster getUnderlying() {
    return _underlying;
  }

  protected FudgeMsgEnvelope encodeUidResponse(final UniqueId uid) {
    final FudgeSerializer serializer = getFudgeSerializer();
    final MutableFudgeMsg response = serializer.newMessage();
    response.add("uniqueId", UniqueIdFudgeBuilder.toFudgeMsg(serializer, uid));
    return new FudgeMsgEnvelope(response);
  }

  /**
   * Resource for time series objects referenced by OID.
   */
  public class TimeSeriesObjectResource {

    private final ObjectId _oid;
    private final LocalDate _from;
    private final LocalDate _to;

    public TimeSeriesObjectResource(final ObjectId oid) {
      this(oid, null, null);
    }

    public TimeSeriesObjectResource(final ObjectId oid, final LocalDate from, final LocalDate to) {
      _oid = oid;
      _from = from;
      _to = to;
    }

    @POST
    @Path("update")
    public FudgeMsgEnvelope update(final FudgeMsgEnvelope payload) {
      return encodeUidResponse(getUnderlying().updateTimeSeriesDataPoints(_oid, getFudgeDeserializer().fudgeMsgToObject(LocalDateDoubleTimeSeries.class, payload.getMessage())));
    }

    @POST
    @Path("correct")
    public FudgeMsgEnvelope correct(final FudgeMsgEnvelope payload) {
      return encodeUidResponse(getUnderlying().correctTimeSeriesDataPoints(_oid, getFudgeDeserializer().fudgeMsgToObject(LocalDateDoubleTimeSeries.class, payload.getMessage())));
    }

    @DELETE
    public FudgeMsgEnvelope delete(final FudgeMsgEnvelope payload) {
      return encodeUidResponse(getUnderlying().removeTimeSeriesDataPoints(_oid, _from, _to));
    }

  }

  /**
   * Resource for time series objects referenced by UID
   */
  public class TimeSeriesResource {

    private final UniqueId _uid;
    private final LocalDate _from;
    private final LocalDate _to;
    private final Integer _maxPoints;

    public TimeSeriesResource(final UniqueId uid) {
      this(uid, null, null, null);
    }

    private TimeSeriesResource(final UniqueId uid, final LocalDate from, final LocalDate to, final Integer maxPoints) {
      _uid = uid;
      _from = from;
      _to = to;
      _maxPoints = maxPoints;
    }

    @GET
    public FudgeMsgEnvelope get() {
      if ((_from != null) || (_to != null) || (_maxPoints != null)) {
        return getTimeSeries();
      }
      try {
        return new FudgeMsgEnvelope(getFudgeSerializer().objectToFudgeMsg(getUnderlying().get(_uid)));
      } catch (DataNotFoundException e) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    }

    @GET
    @Path("timeSeries")
    public FudgeMsgEnvelope getTimeSeries() {
      try {
        return new FudgeMsgEnvelope(getFudgeSerializer().objectToFudgeMsg(getUnderlying().getTimeSeries(_uid, HistoricalTimeSeriesGetFilter.ofRange(_from, _to, _maxPoints))));
      } catch (DataNotFoundException e) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    }

    @DELETE
    public FudgeMsgEnvelope delete() {
      try {
        if ((_from == null) && (_to == null)) {
          getUnderlying().remove(_uid);
        } else {
          // This type of request isn't generated by RemoteHistoricalTimeSeriesMaster but makes sense
          getUnderlying().removeTimeSeriesDataPoints(_uid, _from, _to);
        }
        return null;
      } catch (DataNotFoundException e) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    }

    @Path("from/{from}")
    public TimeSeriesResource from(@PathParam("from") final String fromString) {
      return new TimeSeriesResource(_uid, LocalDate.parse(fromString), _to, _maxPoints);
    }

    @Path("to/{to}")
    public TimeSeriesResource to(@PathParam("to") final String toString) {
      return new TimeSeriesResource(_uid, _from, LocalDate.parse(toString), _maxPoints);
    }
    
    @Path("maxPoints/{maxPoints}")
    public TimeSeriesResource maxPoints(@PathParam("maxPoints") final String maxString) {
      return new TimeSeriesResource(_uid, _from, _to, Integer.parseInt(maxString));
    }

  }

  @POST
  @Path("add")
  public FudgeMsgEnvelope add(final FudgeMsgEnvelope payload) {
    HistoricalTimeSeriesInfoDocument document = getFudgeDeserializer().fudgeMsgToObject(HistoricalTimeSeriesInfoDocument.class, payload.getMessage());
    document = getUnderlying().add(document);
    return encodeUidResponse(document.getUniqueId());
  }

  @POST
  @Path("search")
  public FudgeMsgEnvelope search(final FudgeMsgEnvelope payload) {
    final HistoricalTimeSeriesInfoSearchRequest request = getFudgeDeserializer().fudgeMsgToObject(HistoricalTimeSeriesInfoSearchRequest.class, payload.getMessage());
    final HistoricalTimeSeriesInfoSearchResult result = getUnderlying().search(request);
    return new FudgeMsgEnvelope(getFudgeSerializer().objectToFudgeMsg(result));
  }

  @POST
  @Path("update")
  public FudgeMsgEnvelope update(final FudgeMsgEnvelope payload) {
    // TODO: this is wrong; the target path should be timeSeries/uniqueId/... with a PUT operation
    final HistoricalTimeSeriesInfoDocument request = getFudgeDeserializer().fudgeMsgToObject(HistoricalTimeSeriesInfoDocument.class, payload.getMessage());
    final HistoricalTimeSeriesInfoDocument result = getUnderlying().update(request);
    return new FudgeMsgEnvelope(getFudgeSerializer().objectToFudgeMsg(result));
  }

  @Path("timeSeries/{uniqueId}")
  public TimeSeriesResource timeSeries(@PathParam("uniqueId") final String uidString) {
    return new TimeSeriesResource(UniqueId.parse(uidString));
  }

  @Path("timeSeriesObject/{objectId}")
  public TimeSeriesObjectResource timeSeriesObject(@PathParam("objectId") final String oidString) {
    return new TimeSeriesObjectResource(ObjectId.parse(oidString));
  }

  /**
   * For debugging purposes only.
   * 
   * @return some debug information about the state of this resource object; e.g. which underlying objects is it connected to.
   */
  @GET
  @Path("debugInfo")
  public FudgeMsgEnvelope getDebugInfo() {
    final MutableFudgeMsg message = getFudgeContext().newMessage();
    message.add("fudgeContext", getFudgeContext().toString());
    message.add("historicalTimeSeriesMaster", getUnderlying().toString());
    return new FudgeMsgEnvelope(message);
  }

}
