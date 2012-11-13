/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.temptarget.TempTarget;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * RESTful resource for a {@link TempTargetRepository}
 */
@Path("tempTarget")
public class DataTempTargetRepositoryResource extends DataTempTargetSourceResource {

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param underlying the underlying source, not null
   */
  public DataTempTargetRepositoryResource(final TempTargetRepository underlying) {
    super(underlying);
  }

  @Override
  protected TempTargetRepository getUnderlying() {
    return (TempTargetRepository) super.getUnderlying();
  }

  public static URI uriLocateOrStore(final URI baseUri) {
    return UriBuilder.fromUri(baseUri).path("/target").build();
  }

  @POST
  @Path("target")
  @Consumes(FudgeRest.MEDIA)
  public Response locateOrStore(final FudgeMsg request) {
    final FudgeContext context = OpenGammaFudgeContext.getInstance();
    final TempTarget target = new FudgeDeserializer(context).fudgeMsgToObject(TempTarget.class, request);
    final UniqueId uid = getUnderlying().locateOrStore(target);
    return responseOk(new FudgeSerializer(context).objectToFudgeMsg(uid));
  }

}
