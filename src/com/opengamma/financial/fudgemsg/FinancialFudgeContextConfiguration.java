/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeContextConfiguration;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.mapping.FudgeObjectDictionary;

import com.opengamma.engine.world.Region;
import com.opengamma.engine.world.RegionSource;
import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.financial.analytics.ircurve.ResolvedFixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;

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

  // REVIEW: jim 12-Aug-2010 -- we can probably remove the region source once we switch from direct Region objects to Identifiers
  private RegionSource _regionSource;

  public RegionSource getRegionSource() {
    return _regionSource;
  }

  public void setRegionSource(final RegionSource regionRepository) {
    _regionSource = regionRepository;
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
    dictionary.addType(PeriodSecondaryType.INSTANCE);
    dictionary.addType(TenorSecondaryType.INSTANCE);
    dictionary.addType(StripInstrumentTypeSecondaryType.INSTANCE);
  }

  @Override
  public void configureFudgeObjectDictionary(final FudgeObjectDictionary dictionary) {
    dictionary.getDefaultBuilderFactory().addGenericBuilder(Region.class, new RegionBuilder(this));
    dictionary.getDefaultBuilderFactory().addGenericBuilder(FixedIncomeStrip.class, new FixedIncomeStripBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(YieldCurveDefinition.class, new YieldCurveDefinitionBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(StaticCurveInstrumentProvider.class, new StaticCurveInstrumentProviderBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(BloombergFutureCurveInstrumentProvider.class, new BloombergFutureCurveInstrumentProviderBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(CurveInstrumentProvider.class, new CurveInstrumentProviderBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(CurveSpecificationBuilderConfiguration.class, new CurveSpecificationBuilderConfigurationBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(ResolvedFixedIncomeStrip.class, new ResolvedFixedIncomeStripBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(InterpolatedYieldCurveSpecification.class, new InterpolatedYieldCurveSpecificationFudgeBuilder());
  }

}
