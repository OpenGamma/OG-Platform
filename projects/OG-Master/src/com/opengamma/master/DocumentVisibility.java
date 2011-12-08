/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

/**
 * Enumerates the visibility levels that apply to documents.
 * <p>
 * Each visibility level corresponds to an ordinal (considered to be from 0 to 100) where a lower number indicates a
 * more visible document. Searches may be expanded to include less visible documents.
 */
public enum DocumentVisibility {
  
  /**
   * Documents which are the most visible.
   * <p>
   * This might be used for user-generated content which should be displayed by default.
   */
  VISIBLE((short) 25),
  /**
   * Documents which are the least visible.
   * <p>
   * This might be used for automatically-generated content which should be hidden by default.
   */
  HIDDEN((short) 75);

  //-------------------------------------------------------------------------
  private static final Int2ObjectMap<DocumentVisibility> s_reverseMap = new Int2ObjectArrayMap<DocumentVisibility>();
  
  private final short _visibilityOrdinal;
  
  static {
    for (DocumentVisibility value : DocumentVisibility.values()) {
      s_reverseMap.put(value.getVisibilityOrdinal(), value);
    }
  }
  
  public static DocumentVisibility fromOrdinal(short ordinal) {
    DocumentVisibility visibility = s_reverseMap.get(ordinal);
    if (visibility == null) {
      throw new IllegalArgumentException("No visibility exists with ordinal " + ordinal);
    }
    return visibility;
  }
  
  private DocumentVisibility(short visibilityOrdinal) {
    _visibilityOrdinal = visibilityOrdinal;
  }
  
  public short getVisibilityOrdinal() {
    return _visibilityOrdinal;
  }
  
}
