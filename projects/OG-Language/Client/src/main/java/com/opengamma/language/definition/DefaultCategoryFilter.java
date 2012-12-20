/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

/**
 * A {@link CategoryFilter} implementation that makes no changes.
 */
public class DefaultCategoryFilter implements CategoryFilter {

  // CategoryFilter

  @Override
  public String convertCategory(final String category) {
    return category;
  }

}
