/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithIdentifier;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting InterpolatedYieldCurveSpecification instances to/from Fudge messages.
 */
@FudgeBuilderFor(InterpolatedYieldCurveSpecification.class)
public class InterpolatedYieldCurveSpecificationFudgeBuilder implements FudgeBuilder<InterpolatedYieldCurveSpecification> {
  private static final String CURVE_DATE_FIELD = "curveDate";
  private static final String NAME_FIELD = "name";
  private static final String CURRENCY_FIELD = "currency";
  private static final String REGION_FIELD = "region";
  private static final String INTERPOLATOR_FIELD = "interpolator";
  private static final String INTERPOLATE_YIELDS_FIELD = "interpolateYields";
  private static final String RESOLVED_STRIPS_FIELD = "resolvedStrips";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final InterpolatedYieldCurveSpecification object) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, CURVE_DATE_FIELD, null, object.getCurveDate());
    message.add(NAME_FIELD, object.getName());
    serializer.addToMessage(message, CURRENCY_FIELD, null, object.getCurrency());
    serializer.addToMessage(message, REGION_FIELD, null, object.getRegion());
    serializer.addToMessage(message, INTERPOLATOR_FIELD, null, object.getInterpolator());
    message.add(INTERPOLATE_YIELDS_FIELD, object.interpolateYield());
    for (final FixedIncomeStripWithIdentifier resolvedStrip : object.getStrips()) {
      serializer.addToMessage(message, RESOLVED_STRIPS_FIELD, null, resolvedStrip);
    }
    return message;
  }

  @Override
  public InterpolatedYieldCurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final LocalDate curveDate = deserializer.fieldValueToObject(LocalDate.class, message.getByName(CURVE_DATE_FIELD));
    final String name = message.getString(NAME_FIELD);
    final Currency currency = deserializer.fieldValueToObject(Currency.class, message.getByName(CURRENCY_FIELD));
    final ExternalId region = deserializer.fieldValueToObject(ExternalId.class, message.getByName(REGION_FIELD));
    final Interpolator1D interpolator = deserializer.fieldValueToObject(Interpolator1D.class, message.getByName(INTERPOLATOR_FIELD));
    final List<FudgeField> resolvedStripFields = message.getAllByName(RESOLVED_STRIPS_FIELD);
    final List<FixedIncomeStripWithIdentifier> resolvedStrips = new ArrayList<FixedIncomeStripWithIdentifier>();
    for (final FudgeField resolvedStripField : resolvedStripFields) {
      resolvedStrips.add(deserializer.fieldValueToObject(FixedIncomeStripWithIdentifier.class, resolvedStripField));
    }
    if (message.hasField(INTERPOLATE_YIELDS_FIELD)) {
      final boolean interpolateYield = message.getBoolean(INTERPOLATE_YIELDS_FIELD);
      return new InterpolatedYieldCurveSpecification(curveDate, name, currency, interpolator, interpolateYield, resolvedStrips, region);
    }
    return new InterpolatedYieldCurveSpecification(curveDate, name, currency, interpolator, resolvedStrips, region);
  }

}
