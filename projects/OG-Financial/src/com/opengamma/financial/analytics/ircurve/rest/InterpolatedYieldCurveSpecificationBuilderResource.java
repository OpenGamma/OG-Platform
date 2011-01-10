/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import javax.time.calendar.LocalDate;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;

/**
 * 
 */
public class InterpolatedYieldCurveSpecificationBuilderResource {

  private final InterpolatedYieldCurveSpecificationBuilder _underlying;
  private final FudgeContext _fudgeContext;

  public InterpolatedYieldCurveSpecificationBuilderResource(final InterpolatedYieldCurveSpecificationBuilder underlying, final FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected InterpolatedYieldCurveSpecificationBuilder getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @POST
  @Path("{curveDate}")
  public FudgeMsgEnvelope buildCurve(@PathParam("curveDate") String curveDateString, final FudgeMsgEnvelope payload) {
    final LocalDate curveDate = LocalDate.parse(curveDateString);
    final FudgeDeserializationContext dctx = new FudgeDeserializationContext(getFudgeContext());
    final YieldCurveDefinition curveDefinition = dctx.fieldValueToObject(YieldCurveDefinition.class, payload.getMessage().getByName("definition"));
    final InterpolatedYieldCurveSpecification curveSpecification = getUnderlying().buildCurve(curveDate, curveDefinition);
    final FudgeSerializationContext sctx = new FudgeSerializationContext(getFudgeContext());
    final MutableFudgeFieldContainer msg = sctx.newMessage();
    sctx.objectToFudgeMsgWithClassHeaders(msg, "specification", null, curveSpecification, InterpolatedYieldCurveSpecification.class);
    return new FudgeMsgEnvelope(msg);
  }

}
