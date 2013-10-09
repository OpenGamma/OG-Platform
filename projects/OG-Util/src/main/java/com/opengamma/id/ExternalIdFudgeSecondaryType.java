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
 * Defines an {@code ExternalId} as a Fudge type using a String.
 * <p>
 * An {@code ExternalId} is typically encoded as a sub-message in Fudge with two separate strings.
 * This class allows the objects to be sent as a single formatted string using
 * {@link ExternalId#toString()} and {@link ExternalId#parse(String)}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class ExternalIdFudgeSecondaryType extends SecondaryFieldType<ExternalId, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final ExternalIdFudgeSecondaryType INSTANCE = new ExternalIdFudgeSecondaryType();

  /**
   * Creates an instance.
   */
  private ExternalIdFudgeSecondaryType() {
    super(FudgeWireType.STRING, ExternalId.class);
  }

  // -------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(final ExternalId externalId) {
    return externalId.toString();
  }

  @Override
  public ExternalId primaryToSecondary(final String string) {
    return ExternalId.parse(string);
  }

}
