/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.analytics.volatility.surface.FunctionalVolatilitySurfaceData;

/**
 * Builder for converting {@link FunctionalVolatilitySurfaceData} instances to and from Fudge messages.
 */
@FudgeBuilderFor(FunctionalVolatilitySurfaceData.class)
public class FunctionalVolatilitySurfaceDataFudgeBuilder implements FudgeBuilder<FunctionalVolatilitySurfaceData> {
  private static final String SURFACE_FIELD = "volatilitySurface";
  private static final String X_LABEL_FIELD = "xLabel";
  private static final String X_MINIMUM_FIELD = "xMinimum";
  private static final String X_MAXIMUM_FIELD = "xMaximum";
  private static final String X_SAMPLES_FIELD = "xSamples";
  private static final String Y_LABEL_FIELD = "yLabel";
  private static final String Y_MINIMUM_FIELD = "yMinimum";
  private static final String Y_MAXIMUM_FIELD = "yMaximum";
  private static final String Y_SAMPLES_FIELD = "ySamples";
  private static final String Z_MINIMUM_FIELD = "zMinimum";
  private static final String Z_MAXIMUM_FIELD = "zMaximum";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FunctionalVolatilitySurfaceData object) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(message, SURFACE_FIELD, null, object.getSurface(), VolatilitySurface.class);
    message.add(X_LABEL_FIELD, object.getXLabel());
    message.add(X_MINIMUM_FIELD, object.getXMinimum());
    message.add(X_MAXIMUM_FIELD, object.getXMaximum());
    message.add(X_SAMPLES_FIELD, object.getNXSamples());
    message.add(Y_LABEL_FIELD, object.getYLabel());
    message.add(Y_MINIMUM_FIELD, object.getYMinimum());
    message.add(Y_MAXIMUM_FIELD, object.getYMaximum());
    message.add(Y_SAMPLES_FIELD, object.getNYSamples());
    message.add(Z_MINIMUM_FIELD, object.getZMinimum());
    message.add(Z_MAXIMUM_FIELD, object.getZMaximum());
    return message;
  }

  @Override
  public FunctionalVolatilitySurfaceData buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final VolatilitySurface surface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(SURFACE_FIELD));
    final String xLabel = message.getString(X_LABEL_FIELD);
    final double xMinimum = message.getDouble(X_MINIMUM_FIELD);
    final double xMaximum = message.getDouble(X_MAXIMUM_FIELD);
    final int nX = message.getInt(X_SAMPLES_FIELD);
    final String yLabel = message.getString(Y_LABEL_FIELD);
    final double yMinimum = message.getDouble(Y_MINIMUM_FIELD);
    final double yMaximum = message.getDouble(Y_MAXIMUM_FIELD);
    final int nY = message.getInt(Y_SAMPLES_FIELD);
    final double zMinimum = message.getDouble(Z_MINIMUM_FIELD);
    final double zMaximum = message.getDouble(Z_MAXIMUM_FIELD);
    return new FunctionalVolatilitySurfaceData(surface, xLabel, xMinimum, xMaximum, nX, yLabel, yMinimum, yMaximum, nY, zMinimum, zMaximum);
  }
}
