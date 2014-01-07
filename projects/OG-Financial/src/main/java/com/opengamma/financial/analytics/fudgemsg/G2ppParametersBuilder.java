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

import com.opengamma.financial.analytics.parameters.G2ppParameters;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Fudge builder for {@link G2ppParameters}
 */
@FudgeBuilderFor(G2ppParameters.class)
public class G2ppParametersBuilder extends AbstractFudgeBuilder<G2ppParameters> {
  /** The unique id field */
  private static final String UNIQUE_ID = "uniqueId";
  /** The currency field */
  private static final String CURRENCY = "currency";
  /** The first mean reversion id field */
  private static final String FIRST_MEAN_REVERSION_ID = "firstMeanReversionId";
  /** The second mean reversion id field */
  private static final String SECOND_MEAN_REVERSION_ID = "secondMeanReversionId";
  /** The first initial volatility id field */
  private static final String FIRST_INITIAL_VOLATILITY_ID = "firstInitialVolatilityId";
  /** The second initial volatility id field */
  private static final String SECOND_INITIAL_VOLATILITY_ID = "secondInitialVolatilityId";
  /** The tenor field */
  private static final String TENOR = "tenor";
  /** The first volatility parameter id field */
  private static final String FIRST_VOLATILITY_PARAMETER_ID = "firstVolatilityParameterId";
  /** The second volatility parameter id field */
  private static final String SECOND_VOLATILITY_PARAMETER_ID = "secondVolatilityParameterId";
  /** The correlation id */
  private static final String CORRELATION_ID = "correlationId";

  @Override
  public G2ppParameters buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final UniqueId uniqueId = deserializer.fieldValueToObject(UniqueId.class, message.getByName(UNIQUE_ID));
    final Currency currency = deserializer.fieldValueToObject(Currency.class, message.getByName(CURRENCY));
    final ExternalId firstMeanReversionId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(FIRST_MEAN_REVERSION_ID));
    final ExternalId secondMeanReversionId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(SECOND_MEAN_REVERSION_ID));
    final ExternalId firstInitialVolatilityId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(FIRST_INITIAL_VOLATILITY_ID));
    final ExternalId secondInitialVolatilityId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(SECOND_INITIAL_VOLATILITY_ID));
    final SortedMap<Tenor, Pair<ExternalId, ExternalId>> volatilityTermStructure = new TreeMap<>();
    final List<FudgeField> tenors = message.getAllByName(TENOR);
    final List<FudgeField> firstVolatilityParameterIds = message.getAllByName(FIRST_VOLATILITY_PARAMETER_ID);
    final List<FudgeField> secondVolatilityParameterIds = message.getAllByName(SECOND_VOLATILITY_PARAMETER_ID);
    final int n = tenors.size();
    if (n != firstVolatilityParameterIds.size()) {
      throw new IllegalStateException("Did not have one first volatility parameter id per tenor");
    }
    if (n != secondVolatilityParameterIds.size()) {
      throw new IllegalStateException("Did not have one second volatility parameter id per tenor");
    }
    for (int i = 0; i < n; i++) {
      final Tenor tenor = Tenor.of(Period.parse((String) tenors.get(i).getValue()));
      final ExternalId firstVolatilityParameterId = deserializer.fieldValueToObject(ExternalId.class, firstVolatilityParameterIds.get(i));
      final ExternalId secondVolatilityParameterId = deserializer.fieldValueToObject(ExternalId.class, secondVolatilityParameterIds.get(i));
      volatilityTermStructure.put(tenor, Pairs.of(firstVolatilityParameterId, secondVolatilityParameterId));
    }
    final ExternalId correlationId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(CORRELATION_ID));
    final G2ppParameters parameters = new G2ppParameters(currency, firstMeanReversionId, secondMeanReversionId, firstInitialVolatilityId,
        secondInitialVolatilityId, volatilityTermStructure, correlationId);
    parameters.setUniqueId(uniqueId);
    return parameters;
  }

  @Override
  protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final G2ppParameters object) {
    serializer.addToMessage(message, UNIQUE_ID, null, object.getUniqueId());
    serializer.addToMessage(message, CURRENCY, null, object.getCurrency());
    serializer.addToMessage(message, FIRST_MEAN_REVERSION_ID, null, object.getFirstMeanReversionId());
    serializer.addToMessage(message, SECOND_MEAN_REVERSION_ID, null, object.getSecondMeanReversionId());
    serializer.addToMessage(message, FIRST_INITIAL_VOLATILITY_ID, null, object.getFirstInitialVolatilityId());
    serializer.addToMessage(message, SECOND_INITIAL_VOLATILITY_ID, null, object.getSecondInitialVolatilityId());
    for (final Map.Entry<Tenor, Pair<ExternalId, ExternalId>> entry : object.getVolatilityTermStructure().entrySet()) {
      message.add(TENOR, entry.getKey().getPeriod().toString());
      serializer.addToMessage(message, FIRST_VOLATILITY_PARAMETER_ID, null, entry.getValue().getFirst());
      serializer.addToMessage(message, SECOND_VOLATILITY_PARAMETER_ID, null, entry.getValue().getSecond());
    }
    serializer.addToMessage(message, CORRELATION_ID, null, object.getCorrelationId());
  }

}
