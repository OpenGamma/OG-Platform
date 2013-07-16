/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.net.URI;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Defines an {@code URI} as a Fudge type using a String.
 * <p>
 * An {@code URI} is typically encoded as a sub-message in Fudge with two separate strings.
 * This class allows the objects to be sent as a single formatted string using
 * {@link URI#toString()} and {@link URI#create(String)}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class URIFudgeSecondaryType extends SecondaryFieldType<URI, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final URIFudgeSecondaryType INSTANCE = new URIFudgeSecondaryType();

  /**
   * Creates an instance.
   */
  private URIFudgeSecondaryType() {
    super(FudgeWireType.STRING, URI.class);
  }

  // -------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(final URI externalId) {
    return externalId.toString();
  }

  @Override
  public URI primaryToSecondary(final String string) {
    return URI.create(string);
  }

}
