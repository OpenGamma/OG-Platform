/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.financial.analytics.ircurve.StripInstrumentType;

/**
 * Converts Frequency instances to/from a Fudge string type.
 */
public final class StripInstrumentTypeFudgeSecondaryType extends SecondaryFieldType<StripInstrumentType, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final StripInstrumentTypeFudgeSecondaryType INSTANCE = new StripInstrumentTypeFudgeSecondaryType();

  private StripInstrumentTypeFudgeSecondaryType() {
    super(FudgeWireType.STRING, StripInstrumentType.class);
  }

  @Override
  public String secondaryToPrimary(StripInstrumentType object) {
    return object.name();
  }

  @Override
  public StripInstrumentType primaryToSecondary(final String string) {
    return StripInstrumentType.valueOf(string);
  }

}
