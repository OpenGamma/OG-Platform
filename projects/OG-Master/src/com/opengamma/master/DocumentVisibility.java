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
 * Each visibility level corresponds to a number (considered to be from 0 to 100)
 * where a lower number indicates a more visible document.
 * Searches may be expanded to include less visible documents.
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
  /**
   * Map for looking up values.
   */
  private static final Int2ObjectMap<DocumentVisibility> s_reverseMap = new Int2ObjectArrayMap<DocumentVisibility>();
  static {
    for (DocumentVisibility value : DocumentVisibility.values()) {
      s_reverseMap.put(value.getVisibilityLevel(), value);
    }
  }

  /**
   * The visibility level.
   */
  private final short _visibilityLevel;

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param visibilityLevel  the level, typically from 0 (highest) to 100 (lowest)
   */
  private DocumentVisibility(short visibilityLevel) {
    _visibilityLevel = visibilityLevel;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the visibility level.
   * 
   * @return the level, typically from 0 (highest) to 100 (lowest)
   */
  public short getVisibilityLevel() {
    return _visibilityLevel;
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a {@code DocumentVisibility} by level.
   * 
   * @param level  the visibility level, typically from 0 (highest) to 100 (lowest)
   * @return the visibility object, not null
   * @throws IllegalArgumentException if the visibility is not found
   */
  public static DocumentVisibility ofLevel(short level) {
    DocumentVisibility visibility = s_reverseMap.get(level);
    if (visibility == null) {
      throw new IllegalArgumentException("No visibility exists with ordinal " + level);
    }
    return visibility;
  }

}
