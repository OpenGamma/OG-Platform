/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Simple fragment of textual SQL.
 * <p>
 * This would typically be straightforward SQL.
 */
public final class TextSqlFragment extends SqlFragment {

  /**
   * The text of the fragment.
   */
  private final String _text;

  /**
   * Creates an instance with text.
   * 
   * @param text  the text of the fragment, not null
   */
  public TextSqlFragment(String text) {
    if (text == null) {
      throw new IllegalArgumentException("Text must be specified");
    }
    text = text.trim();
    if (text.length() == 0) {
      _text = "";
    } else {
      _text = text + " ";
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the text.
   * 
   * @return the text, not null
   */
  public String getText() {
    return _text;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void toSQL(StringBuilder buf, ExtSqlBundle bundle, SqlParameterSource paramSource) {
    buf.append(_text);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + ":" + _text;
  }

}
