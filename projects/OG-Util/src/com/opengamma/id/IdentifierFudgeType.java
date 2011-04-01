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
 * Defines an Identifier as a Fudge type using a String.
 * <p>
 * An Identifier is typically encoded as a sub-message in Fudge with three separate strings.
 * This class allows the objects to be sent as a single formatted string using
 * {@link Identifier#toString()} and {@link Identifier#parse(String)}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class IdentifierFudgeType extends SecondaryFieldType<Identifier, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final IdentifierFudgeType INSTANCE = new IdentifierFudgeType();

  /**
   * Creates an instance.
   */
  private IdentifierFudgeType() {
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
