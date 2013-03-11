/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

/**
 * Fatal error indicating that the Java stack could not be started, but with a user visible string explaining exactly why. This string should contain server names, IP addresses, nice explanations,
 * URLs to the forums. Use line feeds to break it up if space is limited. For example, assume that the first line (wrapped) will always be displayed, perhaps in a desktop notification, but the other
 * lines might only appear in a more thorough "error details" window or the client logs.
 */
public final class ConnectorStartupError extends Error {

  private static final long serialVersionUID = 1L;

  public ConnectorStartupError(final String errorSummary) {
    super(errorSummary);
  }

  private static String concatenate(final String a, final String... bs) {
    final StringBuilder sb = new StringBuilder(a);
    for (String b : bs) {
      if (b != null) {
        b = b.trim();
        sb.append('\n');
        sb.append(b);
      }
    }
    return sb.toString();
  }

  public ConnectorStartupError(final String errorSummary, final String... errorDetails) {
    super(concatenate(errorSummary, errorDetails));
  }

  public ConnectorStartupError(final Throwable cause, final String errorSummary) {
    super(errorSummary, cause);
  }

  public ConnectorStartupError(final Throwable cause, final String errorSummary, final String... errorDetails) {
    super(concatenate(errorSummary, errorDetails), cause);
  }

}
