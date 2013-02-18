package com.opengamma.integration.tool.portfolio.xml.v1_0;

import com.opengamma.integration.tool.portfolio.xml.PortfolioConversion;
import com.opengamma.integration.tool.portfolio.xml.SchemaVersion;

public class PortfolioConversionV1_0 extends PortfolioConversion {

  public PortfolioConversionV1_0() {
    super(new SchemaVersion("1.0"), PortfolioDocumentV1_0.class, new PortfolioDocumentConverterV1_0(), new IdRefResolverFactoryV1_0());
  }
}
