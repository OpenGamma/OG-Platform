/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.region.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * 
 */
public class RegionSourceResource {

  private final RegionSource _underlying;
  private final FudgeContext _fudgeContext;

  public RegionSourceResource(final RegionSource underlying, final FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected RegionSource getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeSerializer getFudgeSerializer() {
    return new FudgeSerializer(getFudgeContext());
  }

  private FudgeMsgEnvelope serializeRegion(final Region region) {
    if (region == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializer serializer = getFudgeSerializer();
    final MutableFudgeMsg response = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(response, "region", null, region);
    return new FudgeMsgEnvelope(response);
  }

  @GET
  @Path("highestLevelRegionIdentifier/{externalId}")
  public FudgeMsgEnvelope getHighestLevelRegion(@PathParam("externalId") String externalIdString) {
    final ExternalId externalId = ExternalId.parse(externalIdString);
    return serializeRegion(getUnderlying().getHighestLevelRegion(externalId));
  }

  @GET
  @Path("highestLevelRegionBundle")
  public FudgeMsgEnvelope getHighestLevelRegion(@QueryParam("id") List<String> bundleStrings) {
    final List<ExternalId> externalIds = new ArrayList<ExternalId>(bundleStrings.size());
    for (String bundleString : bundleStrings) {
      externalIds.add(ExternalId.parse(bundleString));
    }
    return serializeRegion(getUnderlying().getHighestLevelRegion(ExternalIdBundle.of(externalIds)));
  }

  @GET
  @Path("regionUID/{uniqueId}")
  public FudgeMsgEnvelope getRegion(@PathParam("uniqueId") String uniqueIdString) {
    final UniqueId uniqueId = UniqueId.parse(uniqueIdString);
    return serializeRegion(getUnderlying().getRegion(uniqueId));
  }

  @GET
  @Path("regionOID/{objectId}/{version}/{correction}")
  public FudgeMsgEnvelope getRegion(@PathParam("objectId") String objectIdString, @PathParam("version") String versionString, @PathParam("correction") String correctionString) {
    final ObjectId objectId = ObjectId.parse(objectIdString);
    final VersionCorrection versionCorrection = VersionCorrection.parse(versionString, correctionString);
    return serializeRegion(getUnderlying().getRegion(objectId, versionCorrection));
  }

  @GET
  @Path("regions/{version}/{correction}")
  public FudgeMsgEnvelope getRegion(@PathParam("version") String versionString, @PathParam("correction") String correctionString, @QueryParam("id") List<String> bundleStrings) {
    final List<ExternalId> externalIds = new ArrayList<ExternalId>(bundleStrings.size());
    for (String bundleString : bundleStrings) {
      externalIds.add(ExternalId.parse(bundleString));
    }
    final FudgeSerializer serializer = getFudgeSerializer();
    final MutableFudgeMsg response = serializer.newMessage();
    for (Region region : getUnderlying().getRegions(ExternalIdBundle.of(externalIds), VersionCorrection.parse(versionString, correctionString))) {
      serializer.addToMessageWithClassHeaders(response, "region", null, region);
    }
    return new FudgeMsgEnvelope(response);
  }

}
