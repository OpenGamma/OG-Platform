/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.volatility.sabr.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@FudgeBuilderFor(SABRFittedSurfaces.class)
public final class SABRFittedSurfacesBuilder extends AbstractFudgeBuilder<SABRFittedSurfaces> {
  private static final String ALPHA_SURFACE_NAME = "AlphaSurface";
  private static final String BETA_SURFACE_NAME = "BetaSurface";
  private static final String NU_SURFACE_NAME = "NuSurface";
  private static final String RHO_SURFACE_NAME = "RhoSurface";
  private static final String INVERSE_JACOBIANS_PAIRS = "pairs";
  private static final String INVERSE_JACOBIANS_MATRICES = "matrices";
  private static final String DAYCOUNT_NAME = "DayCountName";
  private static final String CURRENCY_NAME = "Currency";

  @Override
  public SABRFittedSurfaces buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final VolatilitySurface alphaSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(ALPHA_SURFACE_NAME));
    final VolatilitySurface betaSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(BETA_SURFACE_NAME));
    final VolatilitySurface nuSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(NU_SURFACE_NAME));
    final VolatilitySurface rhoSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(RHO_SURFACE_NAME));
    final Currency currency = deserializer.fieldValueToObject(Currency.class, message.getByName(CURRENCY_NAME));
    final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(deserializer.fieldValueToObject(String.class, message.getByName(DAYCOUNT_NAME)));
    final List<FudgeField> pairFields = message.getAllByName(INVERSE_JACOBIANS_PAIRS);
    final List<FudgeField> matricesFields = message.getAllByName(INVERSE_JACOBIANS_MATRICES);
    if (pairFields.size() != matricesFields.size()) {
      throw new OpenGammaRuntimeException("Should never happen");
    }
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<DoublesPair, DoubleMatrix2D>();
    for (int i = 0; i < pairFields.size(); i++) {
      final DoubleMatrix2D matrix = deserializer.fieldValueToObject(DoubleMatrix2D.class, matricesFields.get(i));
      inverseJacobians.put((DoublesPair) deserializer.fieldValueToObject(Pair.class, pairFields.get(i)), matrix);
    }
    return new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, inverseJacobians, currency, dayCount);
  }

  @Override
  protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final SABRFittedSurfaces object) {
    serializer.addToMessage(message, ALPHA_SURFACE_NAME, null, object.getAlphaSurface());
    serializer.addToMessage(message, BETA_SURFACE_NAME, null, object.getBetaSurface());
    serializer.addToMessage(message, NU_SURFACE_NAME, null, object.getNuSurface());
    serializer.addToMessage(message, RHO_SURFACE_NAME, null, object.getRhoSurface());
    serializer.addToMessage(message, CURRENCY_NAME, null, object.getCurrency());
    serializer.addToMessage(message, DAYCOUNT_NAME, null, object.getDayCount().getConventionName());
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = object.getInverseJacobians();
    for (final Map.Entry<DoublesPair, DoubleMatrix2D> entry : inverseJacobians.entrySet()) {
      message.add(INVERSE_JACOBIANS_PAIRS, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(entry.getKey()), entry.getKey().getClass()));
      message.add(INVERSE_JACOBIANS_MATRICES, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(entry.getValue()), entry.getValue().getClass()));
    }
  }

}
