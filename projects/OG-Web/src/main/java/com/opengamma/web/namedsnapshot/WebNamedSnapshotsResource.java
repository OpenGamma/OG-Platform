/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.namedsnapshot;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchSortOrder;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;

/**
 * RESTful resource for all snapshot documents.
 * <p>
 * The snapshot documents resource represents all the data for one element type in the snapshot master.
 * 
 */
@Path("/snapshots")
public class WebNamedSnapshotsResource extends AbstractWebNamedSnapshotResource {

  /**
   * Creates the resource.
   * @param snapshotMaster  the snapshot master, not null
   */
  public WebNamedSnapshotsResource(final MarketDataSnapshotMaster snapshotMaster) {
    super(snapshotMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.CONFIG)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("type") String type,
      @QueryParam("snapshotId") List<String> snapshotIdStrs,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    MarketDataSnapshotSearchSortOrder so = buildSortOrder(sort, MarketDataSnapshotSearchSortOrder.NAME_ASC);
    FlexiBean out = search(pr, so, name, type, snapshotIdStrs, uriInfo);
    return getFreemarker().build(HTML_DIR + "namedsnapshots.ftl", out);
  }

  private FlexiBean search(PagingRequest request, MarketDataSnapshotSearchSortOrder so, String name, 
      String typeName, List<String> snapshotIdStrs, UriInfo uriInfo) {
    FlexiBean out = createRootData();

    MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    typeName = StringUtils.trimToNull(typeName);
    if (typeName != null) {
      Class<? extends NamedSnapshot> typeClazz = data().getTypeMap().get(typeName);
      searchRequest.setType(typeClazz);
    }
    searchRequest.setPagingRequest(request);
    searchRequest.setSortOrder(so);
    searchRequest.setName(StringUtils.trimToNull(name));
    out.put("searchRequest", searchRequest);
    out.put("type", typeName);
    for (String snapshotIdStr : snapshotIdStrs) {
      searchRequest.addMarketDataSnapshotId(ObjectId.parse(snapshotIdStr));
    }

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      MarketDataSnapshotSearchResult searchResult = data().getSnapshotMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("snapshotxml") String xml) {
    xml = StringUtils.trimToNull(xml);

    try {
      final NamedSnapshot snapshot = parseXML(xml, NamedSnapshot.class);
      MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument(snapshot);
      MarketDataSnapshotDocument added = data().getSnapshotMaster().add(doc);
      URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
      return Response.seeOther(uri).build();
    } catch (Exception ex) {
      FlexiBean out = createRootData();
      out.put("snapshotXml", StringEscapeUtils.escapeJava(StringUtils.defaultString(xml)));
      out.put("err_snapshotXmlMsg", StringUtils.defaultString(ex.getMessage()));
      String html = getFreemarker().build(HTML_DIR + "namedsnapshot-add.ftl", out);
      return Response.ok(html).build();
    }
  }



  //-------------------------------------------------------------------------
  @Path("{snapshotId}")
  public Object findSnapshotHTML(@Subscribe @PathParam("snapshotId") String idStr, @Context HttpHeaders headers) {
    data().setUriSnapshotId(idStr);
    UniqueId oid = UniqueId.parse(idStr);
    try {
      MarketDataSnapshotDocument doc = data().getSnapshotMaster().get(oid);
      data().setSnapshot(doc);
    } catch (DataNotFoundException ex) {
      MarketDataSnapshotHistoryRequest historyRequest = new MarketDataSnapshotHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      MarketDataSnapshotHistoryResult historyResult = data().getSnapshotMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setSnapshot(historyResult.getFirstDocument());
    }
    return new WebNamedSnapshotResource(this);
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
    out.put("type", "");
    out.put("snapshotTypes", getSnapshotTypesProvider().getTypeSet());
    out.put("snapshotDescriptionMap", getSnapshotTypesProvider().getDescriptionMap());
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for snapshot.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebNamedSnapshotData data) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebNamedSnapshotsResource.class);
    return builder.build();
  }

}
