/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget.rest;

import java.net.URI;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.temptarget.TempTarget;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to a {@link TempTargetRepository}. Repository use can be high during graph construction when it is used to collapse targets. A caching layer on top of this to match
 * previously registered targets will usually be beneficial.
 */
public class RemoteTempTargetRepository extends RemoteTempTargetSource implements TempTargetRepository {

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteTempTargetRepository(final URI baseUri) {
    super(baseUri);
  }

  // TempTargetRepository

  @Override
  public UniqueId locateOrStore(final TempTarget target) {
    final URI uri = DataTempTargetRepositoryResource.uriLocateOrStore(getBaseUri());
    try {
      final FudgeContext context = getFudgeContext();
      final FudgeSerializer fsc = new FudgeSerializer(context);
      final FudgeDeserializer fdc = new FudgeDeserializer(context);
      final FudgeMsg response = accessRemote(uri).post(FudgeMsg.class, FudgeSerializer.addClassHeader(fsc.objectToFudgeMsg(target), target.getClass(), TempTarget.class));
      return fdc.fudgeMsgToObject(UniqueId.class, response);
    } catch (final UniformInterfaceException404NotFound e) {
      return null;
    }
  }

}
