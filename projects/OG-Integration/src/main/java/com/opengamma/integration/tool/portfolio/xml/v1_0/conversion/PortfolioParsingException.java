/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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

  /**
   * Serialization version.
   */
  private static final long serialVersionUID = 2772294476733988641L;

  /**
   * Creates an instance.
   * 
   * @param message  the message
   */
  public PortfolioParsingException(final String message) {
    super(message);
  }

  /**
   * Creates an instance.
   * 
   * @param message  the message
   * @param cause  the cause
   */
  public PortfolioParsingException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
