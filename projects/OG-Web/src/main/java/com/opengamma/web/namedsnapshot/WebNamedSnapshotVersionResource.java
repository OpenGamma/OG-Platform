/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.namedsnapshot;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;

/**
 * RESTful resource for a version of a snapshot.
 */
@Path("/snapshots/{snapshotId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebNamedSnapshotVersionResource extends AbstractWebNamedSnapshotResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebNamedSnapshotVersionResource(final AbstractWebNamedSnapshotResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "namedsnapshotversion.ftl", out);
  }


  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    MarketDataSnapshotDocument latestDoc = data().getSnapshot();
    MarketDataSnapshotDocument versionedSnapshot = data().getVersioned();
    out.put("latestSnapshotDoc", latestDoc);
    out.put("latestSnapshot", latestDoc.getNamedSnapshot());
    out.put("snapshotDoc", versionedSnapshot);
    out.put("snapshot", versionedSnapshot.getNamedSnapshot());
    out.put("snapshotDescription", getSnapshotTypesProvider().getDescription(versionedSnapshot.getNamedSnapshot().getClass()));
    out.put("snapshotXml", StringEscapeUtils.escapeJavaScript(createBeanXML(versionedSnapshot.getNamedSnapshot())));
    out.put("deleted", !latestDoc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebNamedSnapshotData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideVersionId  the override version id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebNamedSnapshotData data, final UniqueId overrideVersionId) {
    String snapshotId = data.getBestSnapshotUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebNamedSnapshotVersionResource.class).build(snapshotId, versionId);
  }

}
