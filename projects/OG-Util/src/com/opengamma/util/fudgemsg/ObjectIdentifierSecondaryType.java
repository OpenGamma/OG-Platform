/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.id.ObjectIdentifier;

/**
 * Defines a {@code ObjectIdentifier} as a Fudge type using a String.
 * <p>
 * A {@code ObjectIdentifier} is typically encoded as a sub-message in Fudge with two separate strings.
 * This class allows the objects to be sent as a single formatted string using
 * {@link ObjectIdentifier#toString()} and {@link ObjectIdentifier#parse(String)}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class ObjectIdentifierSecondaryType extends SecondaryFieldType<ObjectIdentifier, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final ObjectIdentifierSecondaryType INSTANCE = new ObjectIdentifierSecondaryType();

  /**
   * Creates an instance.
   */
  private ObjectIdentifierSecondaryType() {
    super(FudgeWireType.STRING, ObjectIdentifier.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(final ObjectIdentifier objectId) {
    return objectId.toString();
  }

  @Override
  public ObjectIdentifier primaryToSecondary(final String string) {
    return ObjectIdentifier.parse(string);
  }

}
