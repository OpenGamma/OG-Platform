/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.i18n;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Fudge builder for {@code Country}.
 */
@FudgeBuilderFor(Country.class)
public final class CountryFudgeBuilder implements FudgeBuilder<Country> {

  /** Field name. */
  public static final String COUNTRY_FIELD_NAME = "country";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Country object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FudgeSerializer.addClassHeader(msg, Country.class);
    serializer.addToMessage(msg, COUNTRY_FIELD_NAME, null, object.getCode());
    return msg;
  }

  @Override
  public Country buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final String countryStr = msg.getString(COUNTRY_FIELD_NAME);
    if (countryStr == null) {
      throw new IllegalArgumentException("Fudge message is not a Country - field 'country' is not present");
    }
    return Country.of(countryStr);
  }

}
