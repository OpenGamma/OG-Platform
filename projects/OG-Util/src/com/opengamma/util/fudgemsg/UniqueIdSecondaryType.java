/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.id.UniqueId;

/**
 * Defines a {@code UniqueId} as a Fudge type using a String.
 * <p>
 * A {@code UniqueId} is typically encoded as a sub-message in Fudge with three separate strings.
 * This class allows the objects to be sent as a single formatted string using
 * {@link UniqueId#toString()} and {@link UniqueId#parse(String)}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class UniqueIdSecondaryType extends SecondaryFieldType<UniqueId, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final UniqueIdSecondaryType INSTANCE = new UniqueIdSecondaryType();

  /**
   * Creates an instance.
   */
  private UniqueIdSecondaryType() {
    super(FudgeWireType.STRING, UniqueId.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(final UniqueId uniqueId) {
    return uniqueId.toString();
  }

  @Override
  public UniqueId primaryToSecondary(final String string) {
    return UniqueId.parse(string);
  }

}
