/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.region.rest;

import java.util.ArrayList;
import java.util.Collection;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestRuntimeException;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides remote access to a {@link RegionSource}.
 */
public class RemoteRegionSource implements RegionSource {

  private final RestClient _restClient;
  private final RestTarget _targetBase;

  public RemoteRegionSource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(baseTarget, "baseTarget");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }

  protected RestClient getRestClient() {
    return _restClient;
  }

  protected RestTarget getTargetBase() {
    return _targetBase;
  }

  @Override
  public Region getHighestLevelRegion(final ExternalId externalId) {
    final RestTarget target = getTargetBase().resolveBase("highestLevelRegionIdentifier").resolve(externalId.toString());
    try {
      return getRestClient().getSingleValue(Region.class, target, "region");
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public Region getHighestLevelRegion(final ExternalIdBundle bundle) {
    final RestTarget target = getTargetBase().resolveBase("highestLevelRegionBundle").resolveQuery("id", bundle.toStringList());
    try {
      return getRestClient().getSingleValue(Region.class, target, "region");
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public Region getRegion(final UniqueId uniqueId) {
    final RestTarget target = getTargetBase().resolveBase("regionUID").resolve(uniqueId.toString());
    final Region region;
    try {
      region = getRestClient().getSingleValue(Region.class, target, "region");
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
    if (region == null) {
      throw new DataNotFoundException(target.toString());
    }
    return region;
  }

  @Override
  public Region getRegion(final ObjectId objectId, final VersionCorrection versionCorrection) {
    final RestTarget target = getTargetBase().resolveBase("regionOID").resolveBase(objectId.toString()).resolveBase(versionCorrection.getVersionAsOfString()).resolve(
        versionCorrection.getCorrectedToString());
    final Region region;
    try {
      region = getRestClient().getSingleValue(Region.class, target, "region");
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
    if (region == null) {
      throw new DataNotFoundException(target.toString());
    }
    return region;
  }

  @Override
  public Collection<? extends Region> getRegions(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    final FudgeMsg msg = getRestClient().getMsg(
        getTargetBase().resolveBase("regions").resolveBase(versionCorrection.getVersionAsOfString()).resolveBase(versionCorrection.getCorrectedToString()).resolveQuery("id", bundle.toStringList()));
    if (msg == null) {
      throw new OpenGammaRuntimeException("Invalid server response");
    }
    final Collection<Region> result = new ArrayList<Region>(msg.getNumFields());
    final FudgeDeserializer fd = getRestClient().getFudgeDeserializer();
    for (FudgeField field : msg.getAllByName("region")) {
      result.add(fd.fieldValueToObject(Region.class, field));
    }
    return result;
  }

}
