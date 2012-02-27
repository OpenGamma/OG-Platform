/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;

/**
 * Converts Frequency instances to/from a Fudge string type.
 */
public final class FrequencyFudgeSecondaryType extends SecondaryFieldType<Frequency, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final FrequencyFudgeSecondaryType INSTANCE = new FrequencyFudgeSecondaryType();

  private FrequencyFudgeSecondaryType() {
    super(FudgeWireType.STRING, Frequency.class);
  }

  @Override
  public String secondaryToPrimary(final Frequency object) {
    return object.getConventionName();
  }

  @Override
  public Frequency primaryToSecondary(final String string) {
    final Object temp = SimpleFrequencyFactory.INSTANCE.getFrequency(string);
    if (temp == null) {
      @SuppressWarnings("unused")
      final int i = 0;
    }
    return SimpleFrequencyFactory.INSTANCE.getFrequency(string);
  }

}
