/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.id.UniqueIdentifier;

/**
 * Defines a {@code UniqueIdentifier} as a Fudge type using a String.
 * <p>
 * A {@code UniqueIdentifier} is typically encoded as a sub-message in Fudge with three separate strings.
 * This class allows the objects to be sent as a single formatted string using
 * {@link UniqueIdentifier#toString()} and {@link UniqueIdentifier#parse(String)}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class UniqueIdentifierSecondaryType extends SecondaryFieldType<UniqueIdentifier, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final UniqueIdentifierSecondaryType INSTANCE = new UniqueIdentifierSecondaryType();

  /**
   * Creates an instance.
   */
  private UniqueIdentifierSecondaryType() {
    super(FudgeWireType.STRING, UniqueIdentifier.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(final UniqueIdentifier uniqueId) {
    return uniqueId.toString();
  }

  @Override
  public UniqueIdentifier primaryToSecondary(final String string) {
    return UniqueIdentifier.parse(string);
  }

}
