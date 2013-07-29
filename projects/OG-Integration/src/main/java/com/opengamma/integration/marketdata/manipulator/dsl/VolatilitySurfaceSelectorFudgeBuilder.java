/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;
import java.util.regex.Pattern;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Sets;

@FudgeBuilderFor(VolatilitySurfaceSelector.class)
public class VolatilitySurfaceSelectorFudgeBuilder implements FudgeBuilder<VolatilitySurfaceSelector> {

  /** Field name for Fudge message. */
  private static final String CALC_CONFIG_NAMES = "calcConfigNames";
  /** Field name for Fudge message. */
  private static final String NAMES = "names";
  /** Field name for Fudge message. */
  private static final String INSTRUMENT_TYPES = "instrumentTypes";
  /** Field name for Fudge message. */
  private static final String QUOTE_TYPES = "quoteTypes";
  /** Field name for Fudge message. */
  private static final String QUOTE_UNITS = "quoteUnits";
  /** Field name for Fudge message. */
  private static final String NAME_MATCH_PATTERN = "nameMatchPattern";
  /** Field name for Fudge message. */
  private static final String NAME_LIKE_PATTERN = "nameLikePattern";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, VolatilitySurfaceSelector selector) {
    MutableFudgeMsg msg = serializer.newMessage();
    addSetToMessage(serializer, msg, CALC_CONFIG_NAMES, selector.getCalcConfigNames());
    addSetToMessage(serializer, msg, NAMES, selector.getNames());
    addSetToMessage(serializer, msg, INSTRUMENT_TYPES, selector.getInstrumentTypes());
    addSetToMessage(serializer, msg, QUOTE_TYPES, selector.getQuoteTypes());
    addSetToMessage(serializer, msg, QUOTE_UNITS, selector.getQuoteUnits());
    if (selector.getNameMatchPattern() != null) {
      serializer.addToMessage(msg, NAME_MATCH_PATTERN, null, selector.getNameMatchPattern().pattern());
    }
    if (selector.getNameLikePattern() != null) {
      serializer.addToMessage(msg, NAME_LIKE_PATTERN, null, selector.getNameLikePattern().pattern());
    }
    return msg;
  }

  @Override
  public VolatilitySurfaceSelector buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    Set<String> calcConfigNames = buildStringSet(deserializer, msg.getMessage(CALC_CONFIG_NAMES));
    Set<String> names = buildStringSet(deserializer, msg.getMessage(NAMES));
    Set<String> instrumentTypes = buildStringSet(deserializer, msg.getMessage(INSTRUMENT_TYPES));
    Set<String> quoteTypes = buildStringSet(deserializer, msg.getMessage(QUOTE_TYPES));
    Set<String> quoteUnits = buildStringSet(deserializer, msg.getMessage(QUOTE_UNITS));

    Pattern nameMatchPattern;
    if (msg.hasField(NAME_MATCH_PATTERN)) {
      String nameMatchStr = deserializer.fieldValueToObject(String.class, msg.getByName(NAME_MATCH_PATTERN));
      nameMatchPattern = Pattern.compile(nameMatchStr);
    } else {
      nameMatchPattern = null;
    }

    Pattern nameLikePattern;
    if (msg.hasField(NAME_LIKE_PATTERN)) {
      String nameLikeStr = deserializer.fieldValueToObject(String.class, msg.getByName(NAME_LIKE_PATTERN));
      nameLikePattern = Pattern.compile(nameLikeStr);
    } else {
      nameLikePattern = null;
    }
    return new VolatilitySurfaceSelector(calcConfigNames, names, nameMatchPattern, nameLikePattern, instrumentTypes, quoteTypes, quoteUnits);
  }

  private void addSetToMessage(FudgeSerializer serializer, MutableFudgeMsg msg, String fieldName, Set<String> strs) {
    if (strs == null) {
      return;
    }
    MutableFudgeMsg setMsg = serializer.newMessage();
    for (String str : strs) {
      serializer.addToMessage(setMsg, null, null, str);
    }
    serializer.addToMessage(msg, fieldName, null, setMsg);
  }

  private Set<String> buildStringSet(FudgeDeserializer deserializer, FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    Set<String> strs = Sets.newHashSet();
    for (FudgeField field : msg) {
      strs.add(deserializer.fieldValueToObject(String.class, field));
    }
    return strs;
  }
}
