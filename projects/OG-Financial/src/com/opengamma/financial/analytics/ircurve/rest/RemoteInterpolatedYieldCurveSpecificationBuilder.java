/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class RemoteInterpolatedYieldCurveSpecificationBuilder implements InterpolatedYieldCurveSpecificationBuilder {

  private final RestClient _restClient;
  private final RestTarget _targetBase;

  public RemoteInterpolatedYieldCurveSpecificationBuilder(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(baseTarget, "baseTarget");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }

  protected FudgeContext getFudgeContext() {
    return getRestClient().getFudgeContext();
  }

  protected RestClient getRestClient() {
    return _restClient;
  }

  protected RestTarget getTargetBase() {
    return _targetBase;
  }

  @Override
  public InterpolatedYieldCurveSpecification buildCurve(LocalDate curveDate, YieldCurveDefinition curveDefinition) {
    ArgumentChecker.notNull(curveDate, "curveDate");
    ArgumentChecker.notNull(curveDefinition, "curveDefinition");
    final RestTarget target = getTargetBase().resolve(curveDate.toString());
    final FudgeSerializationContext sctx = new FudgeSerializationContext(getFudgeContext());
    final MutableFudgeFieldContainer defnMsg = sctx.newMessage();
    sctx.objectToFudgeMsgWithClassHeaders(defnMsg, "definition", null, curveDefinition, YieldCurveDefinition.class);
    final FudgeMsgEnvelope specMsg = getRestClient().post(target, defnMsg);
    if (specMsg == null) {
      return null;
    }
    final FudgeDeserializationContext dctx = new FudgeDeserializationContext(getFudgeContext());
    return dctx.fieldValueToObject(InterpolatedYieldCurveSpecification.class, specMsg.getMessage().getByName("specification"));
  }

}
