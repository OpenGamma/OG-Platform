/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.threeten.bp.Duration;

/**
 * Converts Durations to/from a Fudge string type.
 */
public final class DurationFudgeSecondaryType extends SecondaryFieldType<Duration, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final DurationFudgeSecondaryType INSTANCE = new DurationFudgeSecondaryType();

  private DurationFudgeSecondaryType() {
    super(FudgeWireType.STRING, Duration.class);
  }

  @Override
  public String secondaryToPrimary(Duration object) {
    return object.toString();
  }

  @Override
  public Duration primaryToSecondary(final String string) {
    return Duration.parse(string);
  }

}
