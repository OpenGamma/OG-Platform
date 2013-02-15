package com.opengamma.integration.tool.portfolio.xml;

public interface PortfolioDocumentConverter {

  VersionedPortfolioHandler convert(Object content);
}
