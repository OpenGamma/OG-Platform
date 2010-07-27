/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigDocumentRepository;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.financial.security.FutureSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class ConfigDBInterpolatedYieldCurveSpecificationBuilder implements InterpolatedYieldCurveSpecificationBuilder {
  private ConfigDocumentRepository<CurveSpecificationBuilderConfiguration> _configRepo;
  private SecurityMaster _secMaster;
  // REVIEW: maybe we shouldn't cache these and rely on the repo doing that, but this prevents changes flowing through while we're running.
  private Map<String, CurveSpecificationBuilderConfiguration> _specBuilderCache = new HashMap<String, CurveSpecificationBuilderConfiguration>();
  private Clock _clock;
  
  public ConfigDBInterpolatedYieldCurveSpecificationBuilder(Clock clock, ConfigDocumentRepository<CurveSpecificationBuilderConfiguration> configRepo, SecurityMaster secMaster) {
    _clock = clock;
    _configRepo = configRepo;
    _secMaster = secMaster;
  }
  
  private void clearConfigCache() {
    _specBuilderCache.clear(); // specifically so if the config is changed, we don't cache stale values outside a single curve build.
  }
  
  // this is factored out into a method so it's easier to remove if we want to disable caching.
  private CurveSpecificationBuilderConfiguration getBuilderConfig(String conventionName) {
    if (_specBuilderCache.containsKey(conventionName)) {
      return _specBuilderCache.get(conventionName);
    } else {
      ConfigDocument<CurveSpecificationBuilderConfiguration> builderSpecDoc = _configRepo.getByName(conventionName);
      _specBuilderCache.put(conventionName, builderSpecDoc.getValue());
      return builderSpecDoc.getValue();
    }
  }
  
  @Override
  public InterpolatedYieldCurveSpecification buildCurve(LocalDate curveDate, YieldCurveDefinition curveDefinition) {
    clearConfigCache();
    Collection<ResolvedFixedIncomeStrip> securities = new ArrayList<ResolvedFixedIncomeStrip>();
    for (FixedIncomeStrip strip : curveDefinition.getStrips()) {
      CurveSpecificationBuilderConfiguration builderConfig = getBuilderConfig(strip.getConventionName() + "_" + curveDefinition.getCurrency().getISOCode());
      Security security;
      TimeZone timeZone = _clock.getZone();
      long epochSeconds;
      switch (strip.getInstrumentType()) {
        case CASH:
          Identifier cashIdentifier = builderConfig.getCashSecurity(curveDate, strip.getCurveNodePointTime());
          CashSecurity cashSecurity = (CashSecurity) _secMaster.getSecurity(IdentifierBundle.of(cashIdentifier));
          if (cashSecurity == null) { throw new OpenGammaRuntimeException("Could not resolve cash curve instrument " + cashIdentifier + " from strip " + strip + " in " + curveDefinition); }
          epochSeconds = curveDate.plus(strip.getCurveNodePointTime().getPeriod()).atMidnight().atZone(timeZone).toEpochSeconds();
          security = cashSecurity;
          break;
        case FRA:
          Identifier fraIdentifier = builderConfig.getFRASecurity(curveDate, strip.getCurveNodePointTime());
          FRASecurity fraSecurity = (FRASecurity) _secMaster.getSecurity(IdentifierBundle.of(fraIdentifier));
          if (fraSecurity == null) { throw new OpenGammaRuntimeException("Could not resolve FRA curve instrument " + fraIdentifier + " from strip " + strip + " in " + curveDefinition); }
          epochSeconds = fraSecurity.getEndDate().toEpochSeconds();
          security = fraSecurity;
          break;
        case FUTURE:
          Identifier futureIdentifier = builderConfig.getFutureSecurity(curveDate, strip.getCurveNodePointTime(), strip.getNumberOfFuturesAfterTenor());
          FutureSecurity futureSecurity = (FutureSecurity) _secMaster.getSecurity(IdentifierBundle.of(futureIdentifier));
          if (futureSecurity == null) { throw new OpenGammaRuntimeException("Could not resolve future curve instrument " + futureIdentifier + " from strip " + strip + " in " + curveDefinition); }
          epochSeconds = futureSecurity.getExpiry().getExpiry().toEpochSeconds();
          security = futureSecurity;
          break;
        case LIBOR:
          Identifier rateIdentifier = builderConfig.getRateSecurity(curveDate, strip.getCurveNodePointTime());
          CashSecurity rateSecurity = (CashSecurity) _secMaster.getSecurity(IdentifierBundle.of(rateIdentifier));
          if (rateSecurity == null) { throw new OpenGammaRuntimeException("Could not resolve future curve instrument " + rateIdentifier + " from strip " + strip + " in " + curveDefinition); }
          epochSeconds = curveDate.plus(strip.getCurveNodePointTime().getPeriod()).atMidnight().atZone(timeZone).toEpochSeconds();
          security = rateSecurity;
          break;
        case SWAP:
          Identifier swapIdentifier = builderConfig.getSwapSecurity(curveDate, strip.getCurveNodePointTime());
          SwapSecurity swapSecurity = (SwapSecurity) _secMaster.getSecurity(IdentifierBundle.of(swapIdentifier));
          if (swapSecurity == null) { throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + swapIdentifier + " from strip " + strip + " in " + curveDefinition); }
          epochSeconds = swapSecurity.getMaturityDate().toEpochSeconds();
          security = swapSecurity;
          break;
        default:
          throw new OpenGammaRuntimeException("Unhandled type of instrument in curve definition " + strip.getInstrumentType());
      }
      // TODO: stop using LocalDate for curves.
      double years = ((double) (curveDate.atMidnight().atZone(timeZone).toEpochSeconds() - epochSeconds)) / DateUtil.SECONDS_PER_YEAR; 
      securities.add(new ResolvedFixedIncomeStrip(strip.getInstrumentType(), years, security));
    }
    Interpolator1D<?, ?> interpolator = Interpolator1DFactory.getInterpolator(curveDefinition.getInterpolatorName());  
    return new InterpolatedYieldCurveSpecification(curveDate, curveDefinition.getName(), curveDefinition.getCurrency(), interpolator, securities);
  }

}
