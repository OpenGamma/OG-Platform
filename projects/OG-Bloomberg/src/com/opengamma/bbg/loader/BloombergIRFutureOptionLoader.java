/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Sets;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.PlatformConfigUtils.MarketDataSource;

/**
 * Little util for loading options for Interest rate future
 */
public class BloombergIRFutureOptionLoader {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergIRFutureOptionLoader.class);
  
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
    
    PlatformConfigUtils.configureSystemProperties(MarketDataSource.DIRECT);
    ConfigurableApplicationContext appcontext = getApplicationContext();
    
    ReferenceDataProvider bbgRefDataProvider = appcontext.getBean("sharedReferenceDataProvider", ReferenceDataProvider.class);
    String bloombergKey = BloombergDomainIdentifierResolver.toBloombergKey(underlyingId);
    BloombergBulkSecurityLoader bulkSecurityLoader = appcontext.getBean("bbgBulkSecLoader", BloombergBulkSecurityLoader.class);
    DbSecurityMaster secMaster = appcontext.getBean("dbSecurityMaster", DbSecurityMaster.class);
    BloombergSecurityLoader loader = new BloombergSecurityLoader(secMaster, bulkSecurityLoader);
    
    Set<ExternalId> optionChain = BloombergDataUtils.getOptionChain(bbgRefDataProvider, bloombergKey);
    if (optionChain != null && !optionChain.isEmpty()) {
      loader.loadSecurity(toBundles(optionChain));
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
