/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.volatility.cube.fitting.FittedSmileDataPoints;
import com.opengamma.financial.analytics.volatility.fittedresults.HestonFittedSurfaces;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.analytics.volatility.surface.fitting.SurfaceFittedSmileDataPoints;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.id.ExternalId;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */

/* package */final class SmileFittedSurfacesBuilder {

  private SmileFittedSurfacesBuilder() {
  }

  @FudgeBuilderFor(SABRFittedSurfaces.class)
  public static final class SABRFittedSurfacesFudgeBuilder extends AbstractFudgeBuilder<SABRFittedSurfaces> {
    /** Field name. */
    public static final String ALPHA_SURFACE_FIELD_NAME = "AlphaSurface";
    /** Field name. */
    public static final String BETA_SURFACE_FIELD_NAME = "BetaSurface";
    /** Field name. */
    public static final String NU_SURFACE_FIELD_NAME = "NuSurface";
    /** Field name. */
    public static final String RHO_SURFACE_FIELD_NAME = "RhoSurface";
    /** Field name. */
    public static final String INVERSE_JACOBIANS_PAIRS_FIELD_NAME = "pairs";
    /** Field name. */
    public static final String INVERSE_JACOBIANS_MATRICES_FIELD_NAME = "matrices";
    /** Field name. */
    public static final String DAYCOUNT_FIELD_NAME = "DayCountName";
    /** Field name. */
    public static final String CURRENCY_FIELD_NAME = "Currency";

    @Override
    public SABRFittedSurfaces buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final VolatilitySurface alphaSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(ALPHA_SURFACE_FIELD_NAME));
      final VolatilitySurface betaSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(BETA_SURFACE_FIELD_NAME));
      final VolatilitySurface nuSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(NU_SURFACE_FIELD_NAME));
      final VolatilitySurface rhoSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(RHO_SURFACE_FIELD_NAME));
      final Currency currency = deserializer.fieldValueToObject(Currency.class, message.getByName(CURRENCY_FIELD_NAME));
      final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(deserializer.fieldValueToObject(String.class, message.getByName(DAYCOUNT_FIELD_NAME)));
      final List<FudgeField> pairFields = message.getAllByName(INVERSE_JACOBIANS_PAIRS_FIELD_NAME);
      final List<FudgeField> matricesFields = message.getAllByName(INVERSE_JACOBIANS_MATRICES_FIELD_NAME);
      if (pairFields.size() != matricesFields.size()) {
        throw new OpenGammaRuntimeException("Should never happen");
      }
      final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<DoublesPair, DoubleMatrix2D>();
      for (int i = 0; i < pairFields.size(); i++) {
        final DoubleMatrix2D matrix = deserializer.fieldValueToObject(DoubleMatrix2D.class, matricesFields.get(i));
        inverseJacobians.put((DoublesPair) deserializer.fieldValueToObject(Pair.class, pairFields.get(i)), matrix);
      }
      return new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, inverseJacobians, currency, dayCount);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final SABRFittedSurfaces object) {
      serializer.addToMessage(message, ALPHA_SURFACE_FIELD_NAME, null, object.getAlphaSurface());
      serializer.addToMessage(message, BETA_SURFACE_FIELD_NAME, null, object.getBetaSurface());
      serializer.addToMessage(message, NU_SURFACE_FIELD_NAME, null, object.getNuSurface());
      serializer.addToMessage(message, RHO_SURFACE_FIELD_NAME, null, object.getRhoSurface());
      serializer.addToMessage(message, CURRENCY_FIELD_NAME, null, object.getCurrency());
      serializer.addToMessage(message, DAYCOUNT_FIELD_NAME, null, object.getDayCount().getConventionName());
      final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = object.getInverseJacobians();
      for (final Map.Entry<DoublesPair, DoubleMatrix2D> entry : inverseJacobians.entrySet()) {
        message.add(INVERSE_JACOBIANS_PAIRS_FIELD_NAME, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(entry.getKey()), entry.getKey().getClass()));
        message.add(INVERSE_JACOBIANS_MATRICES_FIELD_NAME, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(entry.getValue()), entry.getValue().getClass()));
      }
    }
  }
  
  @FudgeBuilderFor(HestonFittedSurfaces.class)
  public static final class HestonFittedSurfacesFudgeBuilder extends AbstractFudgeBuilder<HestonFittedSurfaces> {
    /** Field name. */
    public static final String KAPPA_SURFACE_FIELD_NAME = "KappaSurface";
    /** Field name. */
    public static final String THETA_SURFACE_FIELD_NAME = "ThetaSurface";
    /** Field name. */
    public static final String VOL0_SURFACE_FIELD_NAME = "Vol0Surface";
    /** Field name. */
    public static final String OMEGA_SURFACE_FIELD_NAME = "OmegaSurface";
    /** Field name. */
    public static final String RHO_SURFACE_FIELD_NAME = "RhoSurface";
    /** Field name. */
    public static final String INVERSE_JACOBIANS_PAIRS_FIELD_NAME = "pairs";
    /** Field name. */
    public static final String INVERSE_JACOBIANS_MATRICES_FIELD_NAME = "matrices";
    /** Field name. */
    public static final String CURRENCY_FIELD_NAME = "Currency";

    @Override
    public HestonFittedSurfaces buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final VolatilitySurface kappaSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(KAPPA_SURFACE_FIELD_NAME));
      final VolatilitySurface thetaSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(THETA_SURFACE_FIELD_NAME));
      final VolatilitySurface vol0Surface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(VOL0_SURFACE_FIELD_NAME));
      final VolatilitySurface omegaSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(OMEGA_SURFACE_FIELD_NAME));
      final VolatilitySurface rhoSurface = deserializer.fieldValueToObject(VolatilitySurface.class, message.getByName(RHO_SURFACE_FIELD_NAME));
      final Currency currency = deserializer.fieldValueToObject(Currency.class, message.getByName(CURRENCY_FIELD_NAME));
      final List<FudgeField> pairFields = message.getAllByName(INVERSE_JACOBIANS_PAIRS_FIELD_NAME);
      final List<FudgeField> matricesFields = message.getAllByName(INVERSE_JACOBIANS_MATRICES_FIELD_NAME);
      if (pairFields.size() != matricesFields.size()) {
        throw new OpenGammaRuntimeException("Should never happen");
      }
      final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<DoublesPair, DoubleMatrix2D>();
      for (int i = 0; i < pairFields.size(); i++) {
        final DoubleMatrix2D matrix = deserializer.fieldValueToObject(DoubleMatrix2D.class, matricesFields.get(i));
        inverseJacobians.put((DoublesPair) deserializer.fieldValueToObject(Pair.class, pairFields.get(i)), matrix);
      }
      return new HestonFittedSurfaces(kappaSurface, thetaSurface, vol0Surface, omegaSurface, rhoSurface, inverseJacobians, currency);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final HestonFittedSurfaces object) {
      serializer.addToMessage(message, KAPPA_SURFACE_FIELD_NAME, null, object.getKappaSurface());
      serializer.addToMessage(message, THETA_SURFACE_FIELD_NAME, null, object.getThetaSurface());
      serializer.addToMessage(message, VOL0_SURFACE_FIELD_NAME, null, object.getVol0Surface());
      serializer.addToMessage(message, OMEGA_SURFACE_FIELD_NAME, null, object.getOmegaSurface());
      serializer.addToMessage(message, RHO_SURFACE_FIELD_NAME, null, object.getRhoSurface());
      serializer.addToMessage(message, CURRENCY_FIELD_NAME, null, object.getCurrency());
      final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = object.getInverseJacobians();
      for (final Map.Entry<DoublesPair, DoubleMatrix2D> entry : inverseJacobians.entrySet()) {
        message.add(INVERSE_JACOBIANS_PAIRS_FIELD_NAME, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(entry.getKey()), entry.getKey().getClass()));
        message.add(INVERSE_JACOBIANS_MATRICES_FIELD_NAME, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(entry.getValue()), entry.getValue().getClass()));
      }
    }
  }
  
  @FudgeBuilderFor(FittedSmileDataPoints.class)
  public static final class FittedSmileDataPointsBuilder extends AbstractFudgeBuilder<FittedSmileDataPoints> {
    /** Field name */
    public static final String TENOR_PAIR_FIELD_NAME = "Tenor pairs";
    /** Field name */
    public static final String EXTERNAL_IDS_ARRAY_FIELD_NAME = "External ids";
    /** Field name */
    public static final String RELATIVE_STRIKES_ARRAY_FIELD_NAME = "Relative strikes";
    @SuppressWarnings("unchecked")
    @Override
    public FittedSmileDataPoints buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      final List<FudgeField> tenorPairFields = message.getAllByName(TENOR_PAIR_FIELD_NAME);
      final List<FudgeField> externalIdsFields = message.getAllByName(EXTERNAL_IDS_ARRAY_FIELD_NAME);
      final List<FudgeField> relativeStrikesFields = message.getAllByName(RELATIVE_STRIKES_ARRAY_FIELD_NAME);
      final Map<Pair<Tenor, Tenor>, ExternalId[]> externalIds = new HashMap<Pair<Tenor, Tenor>, ExternalId[]>();
      final Map<Pair<Tenor, Tenor>, Double[]> relativeStrikes = new HashMap<Pair<Tenor, Tenor>, Double[]>();
      if (tenorPairFields.size() != externalIdsFields.size() || tenorPairFields.size() != relativeStrikesFields.size()) {
        throw new OpenGammaRuntimeException("Should never happen");
      }
      for (int i = 0; i < tenorPairFields.size(); i++) {
        final Pair<Tenor, Tenor> tenors = deserializer.fieldValueToObject(Pair.class, tenorPairFields.get(i));
        final List<ExternalId> externalIdList = deserializer.fieldValueToObject(List.class, externalIdsFields.get(i));
        final List<Double> relativeStrikesList = deserializer.fieldValueToObject(List.class, relativeStrikesFields.get(i));
        externalIds.put(tenors, externalIdList.toArray(new ExternalId[externalIdList.size()]));
        relativeStrikes.put(tenors, relativeStrikesList.toArray(new Double[relativeStrikesList.size()]));
      }
      return new FittedSmileDataPoints(externalIds, relativeStrikes);
    }

    @Override
    protected void buildMessage(FudgeSerializer serializer, MutableFudgeMsg message, FittedSmileDataPoints object) {
      final Map<Pair<Tenor, Tenor>, ExternalId[]> externalIds = object.getExternalIds();
      final Map<Pair<Tenor, Tenor>, Double[]> relativeStrikes = object.getRelativeStrikes();
      for (final Map.Entry<Pair<Tenor, Tenor>, ExternalId[]> entry : externalIds.entrySet()) {
        message.add(TENOR_PAIR_FIELD_NAME, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(entry.getKey()), Pair.class));
        serializer.addToMessageObject(message, EXTERNAL_IDS_ARRAY_FIELD_NAME, null, Arrays.asList(entry.getValue()), List.class);
        serializer.addToMessageObject(message, RELATIVE_STRIKES_ARRAY_FIELD_NAME, null, Arrays.asList(relativeStrikes.get(entry.getKey())), List.class);
      }
    }  
  }
  
  @FudgeBuilderFor(SurfaceFittedSmileDataPoints.class)
  public static final class SurfaceFittedSmileDataPointsBuilder extends AbstractFudgeBuilder<SurfaceFittedSmileDataPoints> {
    public static final String T_FIELD_NAME = "t field";
    public static final String K_FIELD_NAME = "k field";
    
    @Override
    public SurfaceFittedSmileDataPoints buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      final List<FudgeField> tFields = message.getAllByName(T_FIELD_NAME);
      final List<FudgeField> kFields = message.getAllByName(K_FIELD_NAME);
      final Map<Double, List<Double>> map = new HashMap<Double, List<Double>>();
      for (int i = 0; i < tFields.size(); i++) {
        Double t = deserializer.fieldValueToObject(Double.class, tFields.get(i));
        @SuppressWarnings("unchecked")
        List<Double> ks = deserializer.fieldValueToObject(List.class, kFields.get(i));
        map.put(t, ks);
      }
      return new SurfaceFittedSmileDataPoints(map);
    }

    @Override
    protected void buildMessage(FudgeSerializer serializer, MutableFudgeMsg message, SurfaceFittedSmileDataPoints object) {
      final Map<Double, List<Double>> map = object.getData();
      for (final Map.Entry<Double, List<Double>> entry : map.entrySet()) {
        message.add(T_FIELD_NAME, null, entry.getKey());
        serializer.addToMessageObject(message, K_FIELD_NAME, null, entry.getValue(), List.class);
      }
    }
    
  }

}
