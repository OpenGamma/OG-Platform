/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting {@link YieldCurveDefinition} instances to/from Fudge messages.
 */
@FudgeBuilderFor(YieldCurveDefinition.class)
public class YieldCurveDefinitionFudgeBuilder implements FudgeBuilder<YieldCurveDefinition> {
  private static final String CURRENCY_FIELD = "currency";
  private static final String REGION_FIELD = "region";
  private static final String NAME_FIELD = "name";
  private static final String INTERPOLATOR_NAME_FIELD = "interpolatorName";
  private static final String LEFT_EXTRAPOLATOR_NAME_FIELD = "leftExtrapolatorName";
  private static final String RIGHT_EXTRAPOLATOR_NAME_FIELD = "rightExtrapolatorName";
  private static final String INTERPOLATE_YIELDS_FIELD = "interpolateYields";
  private static final String STRIP_FIELD = "strip";
  private static final String UNIQUE_ID_FIELD = "uniqueId";

  private static final String DEFAULT_LEFT_EXTRAPOLATOR_NAME = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private static final String DEFAULT_RIGHT_EXTRAPOLATOR_NAME = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private static final boolean DEFAULT_INTERPOLATE_YIELD_VALUE = true;

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final YieldCurveDefinition object) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, CURRENCY_FIELD, null, object.getCurrency());
    if (object.getRegionId() != null) {
      serializer.addToMessage(message, REGION_FIELD, null, object.getRegionId());
    }
    message.add(NAME_FIELD, object.getName());
    message.add(INTERPOLATOR_NAME_FIELD, object.getInterpolatorName());
    message.add(LEFT_EXTRAPOLATOR_NAME_FIELD, object.getLeftExtrapolatorName());
    message.add(RIGHT_EXTRAPOLATOR_NAME_FIELD, object.getRightExtrapolatorName());
    message.add(INTERPOLATE_YIELDS_FIELD, object.isInterpolateYields());
    for (final FixedIncomeStrip strip : object.getStrips()) {
      serializer.addToMessage(message, STRIP_FIELD, null, strip);
    }
    serializer.addToMessageWithClassHeaders(message, UNIQUE_ID_FIELD, null, object.getUniqueId(), UniqueId.class);
    return message;
  }

  @Override
  public YieldCurveDefinition buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Currency currency = deserializer.fieldValueToObject(Currency.class, message.getByName(CURRENCY_FIELD));
    ExternalId region = null;
    if (message.hasField(REGION_FIELD)) {
      region = deserializer.fieldValueToObject(ExternalId.class, message.getByName(REGION_FIELD));
    }
    final String name = message.getString(NAME_FIELD);
    final String interpolatorName = message.getString(INTERPOLATOR_NAME_FIELD);
    final List<FudgeField> allByOrdinal = message.getAllByName(STRIP_FIELD);
    final SortedSet<FixedIncomeStrip> strips = new TreeSet<FixedIncomeStrip>();
    for (final FudgeField field : allByOrdinal) {
      final FixedIncomeStrip strip = deserializer.fieldValueToObject(FixedIncomeStrip.class, field);
      strips.add(strip);
    }
    final String leftExtrapolatorName;
    if (message.hasField(LEFT_EXTRAPOLATOR_NAME_FIELD)) {
      leftExtrapolatorName = message.getString(LEFT_EXTRAPOLATOR_NAME_FIELD);
    } else {
      leftExtrapolatorName = DEFAULT_LEFT_EXTRAPOLATOR_NAME;
    }
    final String rightExtrapolatorName;
    if (message.hasField(RIGHT_EXTRAPOLATOR_NAME_FIELD)) {
      rightExtrapolatorName = message.getString(RIGHT_EXTRAPOLATOR_NAME_FIELD);
    } else {
      rightExtrapolatorName = DEFAULT_RIGHT_EXTRAPOLATOR_NAME;
    }
    final boolean interpolateYields;
    if (message.hasField(INTERPOLATE_YIELDS_FIELD)) {
      interpolateYields = message.getBoolean(INTERPOLATE_YIELDS_FIELD);
    } else {
      interpolateYields = DEFAULT_INTERPOLATE_YIELD_VALUE;
    }
    final YieldCurveDefinition curveDefinition = new YieldCurveDefinition(currency, region, name, interpolatorName,
        leftExtrapolatorName, rightExtrapolatorName, interpolateYields, strips);
    final FudgeField uniqueId = message.getByName(UNIQUE_ID_FIELD);
    if (uniqueId != null) {
      curveDefinition.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
    }
    return curveDefinition;
  }

}
