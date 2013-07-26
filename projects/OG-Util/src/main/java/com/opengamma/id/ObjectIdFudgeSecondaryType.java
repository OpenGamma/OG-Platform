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
 * Defines a {@code ObjectId} as a Fudge type using a String.
 * <p>
 * An {@code ObjectId} is typically encoded as a sub-message in Fudge with two separate strings.
 * This class allows the objects to be sent as a single formatted string using
 * {@link ObjectId#toString()} and {@link ObjectId#parse(String)}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class ObjectIdFudgeSecondaryType extends SecondaryFieldType<ObjectId, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final ObjectIdFudgeSecondaryType INSTANCE = new ObjectIdFudgeSecondaryType();

  /**
   * Creates an instance.
   */
  private ObjectIdFudgeSecondaryType() {
    super(FudgeWireType.STRING, ObjectId.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(final ObjectId objectId) {
    return objectId.toString();
  }

  @Override
  public ObjectId primaryToSecondary(final String string) {
    return ObjectId.parse(string);
  }

}
