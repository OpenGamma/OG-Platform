/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

/**
 * Enumerates the modes in which function results may be converted into a JSON-friendly form.
 */
public enum ConversionMode {

  /**
   * Only summary information should be included, suitable for rendering into a small area (e.g. a sparkline in a grid
   * cell).
   */
  SUMMARY,
  
  /**
   * In addition to summary information, detailed information should also be included, suitable for providing a
   * complete, extended view.
   */
  FULL
  
}
