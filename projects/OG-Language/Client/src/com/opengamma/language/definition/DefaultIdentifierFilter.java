/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

/**
 * An {@link IdentifierFilter} implementation that makes no changes.
 */
public class DefaultIdentifierFilter implements IdentifierFilter {

  // IdentifierFilter

  @Override
  public String convertName(final String identifier) {
    return identifier;
  }

  @Override
  public String convertAlias(final String identifier, final String alias) {
    return convertName(alias);
  }

}
