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

import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.InterpolatedVolatilitySurface;
import com.opengamma.math.interpolation.Interpolator2D;

public class ModelVolatilitySurface {
  
  public static FudgeBuilder<ConstantVolatilitySurface> CONSTANT_VOLATILITY_SURFACE = new ConstantVolatilitySurfaceBuilder ();
  public static FudgeBuilder<InterpolatedVolatilitySurface> INTERPOLATED_VOLATILITY_SURFACE = new InterpolatedVolatilitySurfaceBuilder ();
  
  private ModelVolatilitySurface () {
  }
  
  public static class ConstantVolatilitySurfaceBuilder extends FudgeBuilderBase<ConstantVolatilitySurface> {
    
    public static final String SIGMA_FIELD_NAME = "sigma";
    
    @Override
    protected void buildMessage (final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final ConstantVolatilitySurface object) {
      message.add (SIGMA_FIELD_NAME, null, object.getSigma ());
    }
    
    @Override
    public ConstantVolatilitySurface buildObject (final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      return new ConstantVolatilitySurface (message.getFieldValue (Double.class, message.getByName (SIGMA_FIELD_NAME)));
    }
    
  }
  
  public static class InterpolatedVolatilitySurfaceBuilder extends FudgeBuilderBase<InterpolatedVolatilitySurface> {
    
    public static final String DATA_FIELD_NAME = "data";
    public static final String INTERPOLATOR_FIELD_NAME = "interpolator";
    
    @Override
    protected void buildMessage (final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final InterpolatedVolatilitySurface object) {
      context.objectToFudgeMsg (message, DATA_FIELD_NAME, null, object.getData ());
      context.objectToFudgeMsg (message, INTERPOLATOR_FIELD_NAME, null, object.getInterpolator ());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public InterpolatedVolatilitySurface buildObject (final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      return new InterpolatedVolatilitySurface (context.fieldValueToObject (Map.class, message.getByName (DATA_FIELD_NAME)), context.fieldValueToObject (Interpolator2D.class, message.getByName (INTERPOLATOR_FIELD_NAME)));
    }
    
  }
  
  /* package */ static void addBuilders (final FudgeObjectDictionary dictionary) {
    dictionary.addBuilder (ConstantVolatilitySurface.class, CONSTANT_VOLATILITY_SURFACE);
    dictionary.addBuilder (InterpolatedVolatilitySurface.class, INTERPOLATED_VOLATILITY_SURFACE);
  }
  
}