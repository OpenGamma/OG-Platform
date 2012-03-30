/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameter;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureParameter;

/**
 * 
 */
@FudgeBuilderFor(SmileDeltaTermStructureParameter.class)
public class SmileDeltaTermStructureParameterBuilder extends AbstractFudgeBuilder<SmileDeltaTermStructureParameter> {
  private static final String T_DATA_FIELD_NAME = "Time data";
  private static final String DELTA_DATA_FIELD_NAME = "Delta data";
  private static final String VOLATILITY_DATA_FIELD_NAME = "Volatility data";
  
  @Override
  public SmileDeltaTermStructureParameter buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final double[] t = deserializer.fieldValueToObject(double[].class, message.getByName(T_DATA_FIELD_NAME));
    final double[][] delta = deserializer.fieldValueToObject(double[][].class, message.getByName(DELTA_DATA_FIELD_NAME));
    final double[][] volatility = deserializer.fieldValueToObject(double[][].class, message.getByName(VOLATILITY_DATA_FIELD_NAME));
    final int n = t.length;
    final SmileDeltaParameter[] smiles = new SmileDeltaParameter[n];
    for (int i = 0; i < n; i++) {
      smiles[i] = new SmileDeltaParameter(t[i], delta[i], volatility[i]);
    }
    return new SmileDeltaTermStructureParameter(smiles);
  }

  @Override
  protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final SmileDeltaTermStructureParameter object) {
    final SmileDeltaParameter[] smiles = object.getVolatilityTerm();    
    final int n = smiles.length;
    final double[] t = new double[n];
    final double[][] delta = new double[n][];
    final double[][] volatility = new double[n][];
    for (int i = 0; i < n; i++) {
      t[i] = smiles[i].getTimeToExpiry();
      delta[i] = smiles[i].getDelta();
      volatility[i] = smiles[i].getVolatility();
    }
    serializer.addToMessage(message, T_DATA_FIELD_NAME, null, t);
    serializer.addToMessage(message, DELTA_DATA_FIELD_NAME, null, delta);
    serializer.addToMessage(message, VOLATILITY_DATA_FIELD_NAME, null, volatility);
  }

}
