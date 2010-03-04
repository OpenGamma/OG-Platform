/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * This {@link DistributionSpecificationResolver} generates a JMS topic
 * name by simply returning a (configurable) pre-computed ID, with / replaced
 * by . (LiveData/Equity/Nasdaq GS/AAPL -&gt; LiveData.Equity.Nasdaq GS.AAPL).    
 *
 * @author kirk
 */
public class JmsDistributionSpecificationResolver implements
    DistributionSpecificationResolver {
  
  private final IdentificationDomain _domain;
  
  public JmsDistributionSpecificationResolver(IdentificationDomain domain) {
    ArgumentChecker.checkNotNull(domain, "Identification domain to use");
    _domain = domain;    
  }
  
  @Override
  public String getDistributionSpecification(LiveDataSpecification fullyResolvedSpec) {
    String id = fullyResolvedSpec.getIdentifier(_domain);
    if (id == null) {
      throw new OpenGammaRuntimeException("Cannot generate JMS distribution spec because spec " + fullyResolvedSpec + " does not contain ID of type " + _domain);
    }
    
    String jmsSpec = id.replace('/', '.'); // use . as path separator in JMS topic names, not /
    return jmsSpec;
  }

}
