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
import com.opengamma.util.time.ExpiryFieldType;

/**
 * Registers custom builders for the OG-Financial library.
 */
public class FinancialFudgeContextConfiguration extends FudgeContextConfiguration {

  /**
   * A pre-constructed instance.
   */
  public static final FudgeContextConfiguration INSTANCE = new FinancialFudgeContextConfiguration();

  private RegionRepository _regionRepository;

  public RegionRepository getRegionRepository() {
    return _regionRepository;
  }

  public void setRegionRepository(final RegionRepository regionRepository) {
    _regionRepository = regionRepository;
  }

  @Override
  public void configureFudgeTypeDictionary(final FudgeTypeDictionary dictionary) {
    // Secondary types from this package
    dictionary.addType(BusinessDayConventionSecondaryType.INSTANCE);
    dictionary.addType(CurrencySecondaryType.INSTANCE);
    dictionary.addType(DayCountSecondaryType.INSTANCE);
    dictionary.addType(FrequencySecondaryType.INSTANCE);
    dictionary.addType(GICSCodeSecondaryType.INSTANCE);
    // Plus standard ones from OG-Util
    dictionary.addType(ExpiryFieldType.INSTANCE);
  }

  @Override
  public void configureFudgeObjectDictionary(final FudgeObjectDictionary dictionary) {
    dictionary.getDefaultBuilderFactory().addGenericBuilder(Region.class, new RegionBuilder(this));
  }

}
