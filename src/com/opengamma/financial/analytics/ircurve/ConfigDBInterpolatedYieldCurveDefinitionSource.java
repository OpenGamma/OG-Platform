/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.Instant;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigDocumentRepository;
import com.opengamma.financial.Currency;

/**
 * 
 */
public class ConfigDBInterpolatedYieldCurveDefinitionSource implements InterpolatedYieldCurveDefinitionSource {
  
  private ConfigDocumentRepository<YieldCurveDefinition> _curveRepo;

  public ConfigDBInterpolatedYieldCurveDefinitionSource(ConfigDocumentRepository<YieldCurveDefinition> curveRepo) {
    _curveRepo = curveRepo;
  }
  
  protected ConfigDocumentRepository<YieldCurveDefinition> getRepo() {
    return _curveRepo;
  }

  public YieldCurveDefinition getDefinition(Currency ccy, String name) {
    ConfigDocument<YieldCurveDefinition> configDocument = _curveRepo.getByName(name + "_" + ccy.getISOCode());
    YieldCurveDefinition curveDefinition = configDocument.getValue();
    return curveDefinition;
  }
  
  public YieldCurveDefinition getDefinition(Currency ccy, String name, Instant version) {
    ConfigDocument<YieldCurveDefinition> configDocument = _curveRepo.getByName(name + "_" + ccy.getISOCode(), version);
    YieldCurveDefinition curveDefinition = configDocument.getValue();
    return curveDefinition;
  }

}
