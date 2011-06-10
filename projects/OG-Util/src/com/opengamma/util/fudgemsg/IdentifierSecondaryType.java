/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.id.Identifier;

/**
 * Defines an Identifier as a Fudge type using a String.
 * <p>
 * An Identifier is typically encoded as a sub-message in Fudge with three separate strings.
 * This class allows the objects to be sent as a single formatted string using
 * {@link Identifier#toString()} and {@link Identifier#parse(String)}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class IdentifierSecondaryType extends SecondaryFieldType<Identifier, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final IdentifierSecondaryType INSTANCE = new IdentifierSecondaryType();

  /**
   * Creates an instance.
   */
  private IdentifierSecondaryType() {
    super(FudgeWireType.STRING, Identifier.class);
  }

  // -------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(final Identifier identifier) {
    return identifier.toString();
  }

  @Override
  public Identifier primaryToSecondary(final String string) {
    return Identifier.parse(string);
  }

}
