/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.Map;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeObjectDictionary;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;

public class ModelInterestRateCurve {
  
  public static FudgeBuilder<InterpolatedDiscountCurve> INTERPOLATED_DISCOUNT_CURVE = new InterpolatedDiscountCurveBuilder ();
  
  private ModelInterestRateCurve () {
  }
  
  public static class InterpolatedDiscountCurveBuilder extends FudgeBuilderBase<InterpolatedDiscountCurve> {
    
    public static final String INTERPOLATORS_FIELD_NAME = "interpolators";
    public static final String RATES_FIELD_NAME = "rates";
    
    private InterpolatedDiscountCurveBuilder () {
    }
    
    @Override
    protected void buildMessage (final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final InterpolatedDiscountCurve object) {
      context.objectToFudgeMsg(message, RATES_FIELD_NAME, null, object.getData ());
      context.objectToFudgeMsg(message, INTERPOLATORS_FIELD_NAME, null, object.getInterpolators ());
    }

    @SuppressWarnings("unchecked")
    @Override
    public InterpolatedDiscountCurve buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      return new InterpolatedDiscountCurve (
          context.fieldValueToObject (Map.class, message.getByName (RATES_FIELD_NAME)),
          context.fieldValueToObject (Map.class, message.getByName (INTERPOLATORS_FIELD_NAME)));
    }
    
  }
  
  /* package */ static void addBuilders (final FudgeObjectDictionary dictionary) {
    dictionary.addBuilder (InterpolatedDiscountCurve.class, INTERPOLATED_DISCOUNT_CURVE);
  }
  
}