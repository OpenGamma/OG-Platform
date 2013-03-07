/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

/**
 * Converts version-specific portfolio data, to a generic form that can be used
 * to load data into the rest of the system. Using this interface means that the
 * code for all the version-specific parsing can be segregated from the rest of
 * the system. i.e. we can use a single loader for all versions of the schema.
 *
 * @param <T> the type of the content that has been parsed from the XML file. This
 * will depend on the schema version that has been used for the parsing.
 */
public interface PortfolioDocumentConverter<T> {

  /**
   * Convert the parsed xml content to a version-neutral form containing
   * portfolios, positions, trades and securities.
   *
   * @param content the content which has been parsed.
   * @return a collection of portfolios (with all their associated data)
   */
  Iterable<VersionedPortfolioHandler> convert(T content);
}
