/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

/**
 * Formatter for columns whose type is unknown.
 */
/* package */ class UnknownTypeFormatter extends DefaultFormatter {

  @Override
  public FormatType getFormatType() {
    return FormatType.UNKNOWN;
  }
}
