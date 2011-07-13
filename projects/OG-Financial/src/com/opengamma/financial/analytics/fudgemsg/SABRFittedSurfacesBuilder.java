/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.volatility.sabr.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.money.Currency;

/**
 * 
 */
@FudgeBuilderFor(SABRFittedSurfaces.class)
public final class SABRFittedSurfacesBuilder extends AbstractFudgeBuilder<SABRFittedSurfaces> {
  private static final String ALPHA_SURFACE_NAME = "AlphaSurface";
  private static final String BETA_SURFACE_NAME = "BetaSurface";
  private static final String NU_SURFACE_NAME = "NuSurface";
  private static final String RHO_SURFACE_NAME = "RhoSurface";
  private static final String DAYCOUNT_NAME = "DayCountName";
  private static final String CURRENCY_NAME = "Currency";

  @Override
  public SABRFittedSurfaces buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    final VolatilitySurface alphaSurface = context.fieldValueToObject(VolatilitySurface.class, message.getByName(ALPHA_SURFACE_NAME));
    final VolatilitySurface betaSurface = context.fieldValueToObject(VolatilitySurface.class, message.getByName(BETA_SURFACE_NAME));
    final VolatilitySurface nuSurface = context.fieldValueToObject(VolatilitySurface.class, message.getByName(NU_SURFACE_NAME));
    final VolatilitySurface rhoSurface = context.fieldValueToObject(VolatilitySurface.class, message.getByName(RHO_SURFACE_NAME));
    final Currency currency = context.fieldValueToObject(Currency.class, message.getByName(CURRENCY_NAME));
    final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(context.fieldValueToObject(String.class, message.getByName(DAYCOUNT_NAME)));
    return new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, currency, dayCount);
  }

  @Override
  protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeMsg message, final SABRFittedSurfaces object) {
    context.addToMessage(message, ALPHA_SURFACE_NAME, null, object.getAlphaSurface());
    context.addToMessage(message, BETA_SURFACE_NAME, null, object.getBetaSurface());
    context.addToMessage(message, NU_SURFACE_NAME, null, object.getNuSurface());
    context.addToMessage(message, RHO_SURFACE_NAME, null, object.getRhoSurface());
    context.addToMessage(message, CURRENCY_NAME, null, object.getCurrency());
    context.addToMessage(message, DAYCOUNT_NAME, null, object.getDayCount().getConventionName());
  }

}
