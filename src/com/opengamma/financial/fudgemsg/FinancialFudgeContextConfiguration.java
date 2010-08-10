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
  @Override
  public void configureFudgeTypeDictionary(final FudgeTypeDictionary dictionary) {
    dictionary.addType(BusinessDayConventionSecondaryType.INSTANCE);
    dictionary.addType(CurrencySecondaryType.INSTANCE);
    dictionary.addType(DayCountSecondaryType.INSTANCE);
    dictionary.addType(FrequencySecondaryType.INSTANCE);
    dictionary.addType(GICSCodeSecondaryType.INSTANCE);
    dictionary.addType(YieldSecondaryType.INSTANCE);
  }

  @Override
  public void configureFudgeObjectDictionary(final FudgeObjectDictionary dictionary) {
    dictionary.getDefaultBuilderFactory().addGenericBuilder(Region.class, new RegionBuilder(this));
  }

}
