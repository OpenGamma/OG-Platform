/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

/**
 * Defines a UniqueIdentifier as a Fudge type, based on String. The UniqueIdentifier is typically encoded as a 
 * submessage using its toFudgeMsg and fromFudgeMsg methods, but there may be cases where a string is required.
 */
public final class UniqueIdentifierFudgeType extends SecondaryFieldType<UniqueIdentifier, String> {

  /**
   * Singleton instance of the type.
   */
  public static final UniqueIdentifierFudgeType INSTANCE = new UniqueIdentifierFudgeType();

  private UniqueIdentifierFudgeType() {
    super(StringFieldType.INSTANCE, UniqueIdentifier.class);
  }

  @Override
  public String secondaryToPrimary(final UniqueIdentifier identifier) {
    return identifier.toString();
  }

  @Override
  public UniqueIdentifier primaryToSecondary(final String string) {
    return UniqueIdentifier.parse(string);
  }

}
