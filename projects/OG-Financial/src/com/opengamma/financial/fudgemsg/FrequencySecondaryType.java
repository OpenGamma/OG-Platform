/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;

/**
 * Converts Frequency instances to/from a Fudge string type.
 */
public final class FrequencySecondaryType extends SecondaryFieldType<Frequency, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final FrequencySecondaryType INSTANCE = new FrequencySecondaryType();

  private FrequencySecondaryType() {
    super(StringFieldType.INSTANCE, Frequency.class);
  }

  @Override
  public String secondaryToPrimary(Frequency object) {
    return object.getConventionName();
  }

  @Override
  public Frequency primaryToSecondary(final String string) {
    return SimpleFrequencyFactory.INSTANCE.getFrequency(string);
  }

}
