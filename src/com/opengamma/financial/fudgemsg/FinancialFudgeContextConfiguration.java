/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeContextConfiguration;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.mapping.FudgeObjectDictionary;

import com.opengamma.financial.Region;
import com.opengamma.financial.RegionRepository;

/**
 * Configuration for Fudge of the OG-Financial library.
 * <p>
 * This configures Fudge builders.
 */
public class FinancialFudgeContextConfiguration extends FudgeContextConfiguration {

  /**
   * The singleton configuration.
   */
  public static final FudgeContextConfiguration INSTANCE = new FinancialFudgeContextConfiguration();

  private RegionRepository _regionRepository;

  public RegionRepository getRegionRepository() {
    return _regionRepository;
  }

  public void setRegionRepository(final RegionRepository regionRepository) {
    _regionRepository = regionRepository;
  }

  //-------------------------------------------------------------------------
  
  // REVIEW kirk 2010-08-26 -- If you think of changing these things, take a look at the
  // fudgeContext setup in the common.xml spring file.
  
  @Override
  public void configureFudgeObjectDictionary(final FudgeObjectDictionary dictionary) {
    dictionary.addAllClasspathBuilders();
    // REVIEW kirk 2010-08-26 -- This is the one thing that needs changed to eliminate this class.
    dictionary.getDefaultBuilderFactory().addGenericBuilder(Region.class, new RegionBuilder(this));
  }

  @Override
  public void configureFudgeTypeDictionary(FudgeTypeDictionary dictionary) {
    dictionary.addAllAnnotatedSecondaryTypes();
  }

}
