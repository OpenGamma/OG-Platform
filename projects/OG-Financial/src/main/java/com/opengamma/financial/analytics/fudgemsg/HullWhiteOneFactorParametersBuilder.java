/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Period;

import com.opengamma.financial.analytics.parameters.HullWhiteOneFactorParameters;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Fudge builder for {@link HullWhiteOneFactorParameters}
 */
@FudgeBuilderFor(HullWhiteOneFactorParameters.class)
public class HullWhiteOneFactorParametersBuilder extends AbstractFudgeBuilder<HullWhiteOneFactorParameters> {
  /** The unique id field */
  private static final String UNIQUE_ID = "uniqueId";
  /** The currency field */
  private static final String CURRENCY = "currency";
  /** The mean reversion id field */
  private static final String MEAN_REVERSION_ID = "meanReversionId";
  /** The initial volatility id field */
  private static final String INITIAL_VOLATILITY_ID = "initialVolatilityId";
  /** The tenor field */
  private static final String TENOR = "tenor";
  /** The volatility parameter id field */
  private static final String VOLATILITY_PARAMETER_ID = "volatilityParameterId";

  @Override
  public HullWhiteOneFactorParameters buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final UniqueId uniqueId = deserializer.fieldValueToObject(UniqueId.class, message.getByName(UNIQUE_ID));
    final Currency currency = deserializer.fieldValueToObject(Currency.class, message.getByName(CURRENCY));
    final ExternalId meanReversionId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(MEAN_REVERSION_ID));
    final ExternalId initialVolatilityId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(INITIAL_VOLATILITY_ID));
    final SortedMap<Tenor, ExternalId> volatilityTermStructure = new TreeMap<>();
    final List<FudgeField> tenors = message.getAllByName(TENOR);
    final List<FudgeField> volatilityParameterIds = message.getAllByName(VOLATILITY_PARAMETER_ID);
    final int n = tenors.size();
    if (n != volatilityParameterIds.size()) {
      throw new IllegalStateException("Did not have one volatility parameter id per tenor");
    }
    for (int i = 0; i < n; i++) {
      final Tenor tenor = Tenor.of(Period.parse((String) tenors.get(i).getValue()));
      final ExternalId volatilityParameterId = deserializer.fieldValueToObject(ExternalId.class, volatilityParameterIds.get(i));
      volatilityTermStructure.put(tenor, volatilityParameterId);
    }
    final HullWhiteOneFactorParameters parameters = new HullWhiteOneFactorParameters(currency, meanReversionId, initialVolatilityId,
        volatilityTermStructure);
    parameters.setUniqueId(uniqueId);
    return parameters;
  }

  @Override
  protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final HullWhiteOneFactorParameters object) {
    serializer.addToMessage(message, UNIQUE_ID, null, object.getUniqueId());
    serializer.addToMessage(message, CURRENCY, null, object.getCurrency());
    serializer.addToMessage(message, MEAN_REVERSION_ID, null, object.getMeanReversionId());
    serializer.addToMessage(message, INITIAL_VOLATILITY_ID, null, object.getInitialVolatilityId());
    for (final Map.Entry<Tenor, ExternalId> entry : object.getVolatilityTermStructure().entrySet()) {
      message.add(TENOR, entry.getKey().getPeriod().toString());
      serializer.addToMessage(message, VOLATILITY_PARAMETER_ID, null, entry.getValue());
    }
  }

}
