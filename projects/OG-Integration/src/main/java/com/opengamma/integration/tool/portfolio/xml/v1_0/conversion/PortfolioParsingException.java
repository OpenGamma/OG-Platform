/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

/**
 * Exception thrown when an error interrupts the parsing of a
 * portfolio. It should be assumed that other portfolios can
 * be successfully parsed.
 */
public class PortfolioParsingException extends RuntimeException {

  public PortfolioParsingException(String message) {
    super(message);
  }

  public PortfolioParsingException(String message, Throwable cause) {
    super(message, cause);
  }
}