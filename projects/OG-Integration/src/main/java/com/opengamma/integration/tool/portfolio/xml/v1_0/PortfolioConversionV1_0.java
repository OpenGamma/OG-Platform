package com.opengamma.integration.tool.portfolio.xml.v1_0;

import java.lang.Class;

import com.opengamma.integration.tool.portfolio.xml.IdRefResolverFactory;
import com.opengamma.integration.tool.portfolio.xml.PfConverter;
import com.opengamma.integration.tool.portfolio.xml.PortfolioConversion;

public class PortfolioConversionV1_0 extends PortfolioConversion {

  public PortfolioConversionV1_0() {

    super(PortfolioDocumentV1_0.class, new PfConverterV1_0(), new IdRefResolverFactoryV1_0());
  }
}
