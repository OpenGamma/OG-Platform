package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import com.opengamma.integration.tool.portfolio.xml.IdRefResolverFactory;
import com.sun.xml.internal.bind.IDResolver;

public class IdRefResolverFactoryV1_0 implements IdRefResolverFactory {

  @Override
  public IDResolver create() {
    return new IdRefResolver();
  }
}
