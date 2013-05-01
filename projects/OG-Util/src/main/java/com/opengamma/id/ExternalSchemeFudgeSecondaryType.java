/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.id;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Defines an {@code ExternalScheme} as a Fudge type using a String.
 * <p>
 * An {@code ExternalScheme} is typically encoded as a sub-message in Fudge with two separate strings.
 * This class allows the objects to be sent as a single formatted string using
 * {@link ExternalScheme#toString()} and {@link ExternalScheme#of(String)}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class ExternalSchemeFudgeSecondaryType extends SecondaryFieldType<ExternalScheme, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final ExternalSchemeFudgeSecondaryType INSTANCE = new ExternalSchemeFudgeSecondaryType();

  /**
   * Creates an instance.
   */
  private ExternalSchemeFudgeSecondaryType() {
    super(FudgeWireType.STRING, ExternalScheme.class);
  }

  // -------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(final ExternalScheme externalId) {
    return externalId.toString();
  }

  @Override
  public ExternalScheme primaryToSecondary(final String string) {
    return ExternalScheme.of(string);
  }

}
