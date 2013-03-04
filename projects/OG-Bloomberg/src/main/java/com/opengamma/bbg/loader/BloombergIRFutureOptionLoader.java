/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.Collection;
import java.util.Set;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.financial.security.DefaultSecurityLoader;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Little util for loading options for Interest rate future
 */
public class BloombergIRFutureOptionLoader {

  /* package */static final String CONTEXT_CONFIGURATION_PATH = "/com/opengamma/bbg/loader/bloomberg-security-loader-context.xml";

  private static ConfigurableApplicationContext getApplicationContext() {
    ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_CONFIGURATION_PATH);
    context.start();
    return context;
  }
  
  /**
   * Little util for loading options for Interest rate future.
   * @param args command line params
   */
  public static void main(String args[]) { //CSIGNORE
    ExternalId underlyingId = ExternalId.parse(args[0]);
    
    PlatformConfigUtils.configureSystemProperties();
    ConfigurableApplicationContext appcontext = getApplicationContext();
    
    ReferenceDataProvider bbgRefDataProvider = appcontext.getBean("sharedReferenceDataProvider", ReferenceDataProvider.class);
    String bloombergKey = BloombergDomainIdentifierResolver.toBloombergKey(underlyingId);
    SecurityProvider secProvider = appcontext.getBean("bloombergSecurityProvider", SecurityProvider.class);
    DbSecurityMaster secMaster = appcontext.getBean("dbSecurityMaster", DbSecurityMaster.class);
    DefaultSecurityLoader loader = new DefaultSecurityLoader(secMaster, secProvider);
    
    Set<ExternalId> optionChain = BloombergDataUtils.getOptionChain(bbgRefDataProvider, bloombergKey);
    if (optionChain != null && !optionChain.isEmpty()) {
      loader.loadSecurities(toBundles(optionChain));
    }
  }

  private static Collection<ExternalIdBundle> toBundles(Set<ExternalId> optionChain) {
    Set<ExternalIdBundle> results = Sets.newHashSet();
    for (ExternalId identifier : optionChain) {
      results.add(ExternalIdBundle.of(identifier));
    }
    return results;
  }

}
