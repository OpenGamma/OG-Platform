/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

public interface PortfolioDocumentConverter {

  VersionedPortfolioHandler convert(Object content);
}
