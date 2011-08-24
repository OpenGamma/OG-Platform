/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

/**
 * A filter for converting categories from the ones present here to
 * a form appropriate to the bound language.
 */
public interface CategoryFilter {

  /**
   * Converts the category to a form used by the bound language.
   * If a null category is returned, the published definition will have no category field.
   * 
   * @param category  the category
   * @return the validated category, null if none is applicable
   */
  String convertCategory(String category);

}
