/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.BondTradeWithEntityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility methods that are used in functions that calculate bond analytics.
 */
public class BondFunctionUtils {

  /**
   * Converts a bond trade into a form that can be used in the analytics library.
   * @param context The execution context, not null
   * @param target The computation target, not null
   * @param date The valuation date / time, not null
   * @return The analytics form of a bond security
   */
  public static InstrumentDerivative getDerivative(final FunctionExecutionContext context, final ComputationTarget target, final ZonedDateTime date) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(target.getType() == ComputationTargetType.TRADE, "Computation target must be a trade");
    final Trade trade = target.getTrade();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
    final BondTradeWithEntityConverter converter = new BondTradeWithEntityConverter(holidaySource, conventionSource, regionSource, securitySource);
    final InstrumentDefinition<?> definition = converter.convert(trade);
    if (definition instanceof BondFutureDefinition) {
      return ((BondFutureDefinition) definition).toDerivative(date, 0.0);
    }
    return definition.toDerivative(date);
  }

}
