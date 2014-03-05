/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeMapWrapper;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for time-series.
 * <p>
 * The time-series resource receives and processes RESTful calls to the time-series source.
 */
@Path("htsSource")
public class DataHistoricalTimeSeriesSourceResource extends AbstractDataResource {

  /**
   * The time-series source.
   */
  private final HistoricalTimeSeriesSource _htsSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param htsSource  the underlying time-series source, not null
   */
  public DataHistoricalTimeSeriesSourceResource(final HistoricalTimeSeriesSource htsSource) {
    ArgumentChecker.notNull(htsSource, "htsSource");
    _htsSource = htsSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-series source.
   * 
   * @return the time-series source, not null
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _htsSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("hts/{htsId}")
  public Response get(
      @PathParam("htsId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("start") String startStr,
      @QueryParam("includeStart")  boolean includeStart,
      @QueryParam("end") String endStr,
      @QueryParam("includeEnd") boolean includeEnd,
      @QueryParam("maxPoints") Integer maxPoints) {
    final UniqueId uniqueId = ObjectId.parse(idStr).atVersion(version);
    final LocalDate start = (startStr != null ? LocalDate.parse(startStr) : null);
    final LocalDate end = (endStr != null ? LocalDate.parse(endStr) : null);
    final HistoricalTimeSeries result;
    if (start == null && end == null && maxPoints == null) {
      result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(uniqueId);
    } else if (maxPoints != null) {
      result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd, maxPoints);
    } else {
      result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
    }
    return responseOkObject(result);
  }
  
  @GET
  @Path("htsMeta/externalIdBundle/{htsId}")
  public Response getExternalIdBundle(
      @PathParam("htsId") String idStr,
      @QueryParam("version") String version) {
    final UniqueId uniqueId = ObjectId.parse(idStr).atVersion(version);
    final ExternalIdBundle idBundle = getHistoricalTimeSeriesSource().getExternalIdBundle(uniqueId);
    return responseOkObject(idBundle);
  }

  @GET
  @Path("htsSearches/single")
  public Response searchSingle(
      @QueryParam("id") List<String> idStrs,
      @QueryParam("idValidityDate") String idValidityDateStr,
      @QueryParam("dataSource") String dataSource,
      @QueryParam("dataProvider") String dataProvider,
      @QueryParam("dataField") String dataField,
      @QueryParam("start") String startStr,
      @QueryParam("includeStart")  boolean includeStart,
      @QueryParam("end") String endStr,
      @QueryParam("includeEnd") boolean includeEnd,
      @QueryParam("maxPoints") Integer maxPoints) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(idStrs);
    final LocalDate start = (startStr != null ? LocalDate.parse(startStr) : null);
    final LocalDate end = (endStr != null ? LocalDate.parse(endStr) : null);
    final HistoricalTimeSeries result;
    if (idValidityDateStr != null) {
      final LocalDate idValidityDate = ("ALL".equals(idValidityDateStr) ? null : LocalDate.parse(idValidityDateStr));
      if (start == null && end == null && maxPoints == null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, idValidityDate, dataSource, dataProvider, dataField);
      } else if (maxPoints != null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, idValidityDate, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, maxPoints);
      } else {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, idValidityDate, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd);
      }
    } else {
      if (start == null && end == null && maxPoints == null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, dataSource, dataProvider, dataField);
      } else if (maxPoints != null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, maxPoints);
      } else {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd);
      }
    }
    return responseOkObject(result);
  }

  @GET
  @Path("htsSearches/resolve")
  public Response searchResolve(
      @QueryParam("id") List<String> idStrs,
      @QueryParam("idValidityDate") String idValidityDateStr,
      @QueryParam("dataField") String dataField,
      @QueryParam("resolutionKey") String resolutionKey,
      @QueryParam("start") String startStr,
      @QueryParam("includeStart")  boolean includeStart,
      @QueryParam("end") String endStr,
      @QueryParam("includeEnd") boolean includeEnd,
      @QueryParam("maxPoints") Integer maxPoints) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(idStrs);
    final LocalDate start = (startStr != null ? LocalDate.parse(startStr) : null);
    final LocalDate end = (endStr != null ? LocalDate.parse(endStr) : null);
    final HistoricalTimeSeries result;
    if (idValidityDateStr != null) {
      final LocalDate idValidityDate = ("ALL".equals(idValidityDateStr) ? null : LocalDate.parse(idValidityDateStr));
      if (start == null && end == null && maxPoints == null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, idValidityDate, resolutionKey);
      } else if (maxPoints != null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, idValidityDate, resolutionKey, start, includeStart, end, includeEnd, maxPoints);
      } else {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, idValidityDate, resolutionKey, start, includeStart, end, includeEnd);
      }
    } else {
      if (start == null && end == null && maxPoints == null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, resolutionKey);
      } else if (maxPoints != null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, resolutionKey, start, includeStart, end, includeEnd, maxPoints);
      } else {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, resolutionKey, start, includeStart, end, includeEnd);
      }
    }
    return responseOkObject(result);
  }

  @SuppressWarnings("unchecked")
  @POST
  @Path("htsSearches/bulk")
  public Response searchBulk(FudgeMsgEnvelope request) {
    // non-ideal variant using POST
    FudgeMsg msg = request.getMessage();
    FudgeDeserializer deserializationContext = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    Set<ExternalIdBundle> identifierSet = deserializationContext.fudgeMsgToObject(Set.class, msg.getMessage("id"));
    String dataSource = msg.getString("dataSource");
    String dataProvider = msg.getString("dataProvider");
    String dataField = msg.getString("dataField");
    LocalDate start = deserializationContext.fieldValueToObject(LocalDate.class, msg.getByName("start"));
    boolean inclusiveStart = msg.getBoolean("includeStart");
    LocalDate end = deserializationContext.fieldValueToObject(LocalDate.class, msg.getByName("end"));
    boolean includeEnd = msg.getBoolean("includeEnd");
    
    Map<ExternalIdBundle, HistoricalTimeSeries> result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        identifierSet, dataSource, dataProvider, dataField, start, inclusiveStart, end, includeEnd);
    return responseOkObject(FudgeMapWrapper.of(result));
  }

  //-------------------------------------------------------------------------
  /**
   * For debugging purposes only.
   * 
   * @return some debug information about the state of this resource object
   */
  @GET
  @Path("debugInfo")
  public FudgeMsgEnvelope getDebugInfo() {
    final MutableFudgeMsg message = OpenGammaFudgeContext.getInstance().newMessage();
    message.add("fudgeContext", OpenGammaFudgeContext.getInstance().toString());
    message.add("historicalTimeSeriesSource", getHistoricalTimeSeriesSource().toString());
    return new FudgeMsgEnvelope(message);
  }

  //-------------------------------------------------------------------------
  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("hts/{htsId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }
  
  public static URI uriExternalIdBundleGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsMeta/externalIdBundle/{htsId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  public static URI uriGet(URI baseUri, UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("hts/{htsId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    if (start != null) {
      bld.queryParam("start", start);
      bld.queryParam("includeStart", includeStart);
    }
    if (end != null) {
      bld.queryParam("end", end);
      bld.queryParam("includeEnd", includeEnd);
    }
    if (maxPoints != null) {
      bld.queryParam("maxPoints", maxPoints);
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Workaround for {@link UriBuilder#queryParam} that will not escape strings that contain valid escaped sequences. For example, "%3FFoo" will be left as-is since "%3F" is a valid escape whereas
   * "%3GFoo" will be escaped to "%253GFoo". If the string contains a "%" then we will escape it in advance and the builder will leave it alone. Otherwise we'll let the builder deal with the string.
   * 
   * @param bundle the identifiers to convert
   * @return the array of, possibly encoded, identifier strings
   */
  private static Object[] identifiers(final ExternalIdBundle bundle) {
    final List<String> identifiers = bundle.toStringList();
    final String[] array = new String[identifiers.size()];
    identifiers.toArray(array);
    try {
      for (int i = 0; i < array.length; i++) {
        if (array[i].indexOf('%') >= 0) {
          array[i] = URLEncoder.encode(array[i], "UTF-8").replace('+', ' ');
        }
      }
    } catch (UnsupportedEncodingException e) {  // CSIGNORE
      throw new OpenGammaRuntimeException("Caught", e);
    }
    return array;
  }

  public static URI uriSearchSingle(
      URI baseUri, ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/single");
    bld.queryParam("id", identifiers(identifierBundle));
    if (dataSource != null) {
      bld.queryParam("dataSource", dataSource);
    }
    if (dataProvider != null) {
      bld.queryParam("dataProvider", dataProvider);
    }
    if (dataField != null) {
      bld.queryParam("dataField", dataField);
    }
    if (start != null) {
      bld.queryParam("start", start);
      bld.queryParam("includeStart", includeStart);
    }
    if (end != null) {
      bld.queryParam("end", end);
      bld.queryParam("includeEnd", includeEnd);
    }
    if (maxPoints != null) {
      bld.queryParam("maxPoints", maxPoints);
    }
    return bld.build();
  }

  public static URI uriSearchSingle(
      URI baseUri, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/single");
    bld.queryParam("id", identifiers(identifierBundle));
    bld.queryParam("idValidityDate", (identifierValidityDate != null ? identifierValidityDate : "ALL"));
    if (dataSource != null) {
      bld.queryParam("dataSource", dataSource);
    }
    if (dataProvider != null) {
      bld.queryParam("dataProvider", dataProvider);
    }
    if (dataField != null) {
      bld.queryParam("dataField", dataField);
    }
    if (start != null) {
      bld.queryParam("start", start);
      bld.queryParam("includeStart", includeStart);
    }
    if (end != null) {
      bld.queryParam("end", end);
      bld.queryParam("includeEnd", includeEnd);
    }
    if (maxPoints != null) {
      bld.queryParam("maxPoints", maxPoints);
    }
    return bld.build();
  }

  public static URI uriSearchResolve(
      URI baseUri, ExternalIdBundle identifierBundle, String dataField, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/resolve");
    bld.queryParam("id", identifiers(identifierBundle));
    if (dataField != null) {
      bld.queryParam("dataField", dataField);
    }
    if (resolutionKey != null) {
      bld.queryParam("resolutionKey", resolutionKey);
    }
    if (start != null) {
      bld.queryParam("start", start);
      bld.queryParam("includeStart", includeStart);
    }
    if (end != null) {
      bld.queryParam("end", end);
      bld.queryParam("includeEnd", includeEnd);
    }
    if (maxPoints != null) {
      bld.queryParam("maxPoints", maxPoints);
    }
    return bld.build();
  }

  public static URI uriSearchResolve(
      URI baseUri, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataField, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/resolve");
    bld.queryParam("id", identifiers(identifierBundle));
    bld.queryParam("idValidityDate", (identifierValidityDate != null ? identifierValidityDate : "ALL"));
    if (dataField != null) {
      bld.queryParam("dataField", dataField);
    }
    if (resolutionKey != null) {
      bld.queryParam("resolutionKey", resolutionKey);
    }
    if (start != null) {
      bld.queryParam("start", start);
      bld.queryParam("includeStart", includeStart);
    }
    if (end != null) {
      bld.queryParam("end", end);
      bld.queryParam("includeEnd", includeEnd);
    }
    if (maxPoints != null) {
      bld.queryParam("maxPoints", maxPoints);
    }
    return bld.build();
  }

  public static URI uriSearchBulk(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/bulk");
    return bld.build();
  }

  public static FudgeMsg uriSearchBulkData(
      Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    FudgeSerializer serializationContext = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    MutableFudgeMsg msg = serializationContext.newMessage();
    serializationContext.addToMessage(msg, "id", null, identifierSet);
    serializationContext.addToMessage(msg, "dataSource", null, dataSource);
    serializationContext.addToMessage(msg, "dataProvider", null, dataProvider);
    serializationContext.addToMessage(msg, "dataField", null, dataField);
    serializationContext.addToMessage(msg, "start", null, start);
    serializationContext.addToMessage(msg, "includeStart", null, includeStart);
    serializationContext.addToMessage(msg, "end", null, end);
    serializationContext.addToMessage(msg, "includeEnd", null, includeEnd);
    return msg;
  }

}
