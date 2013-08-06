/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Builder for converting CurveSpecificationBuilderConfiguration instances to/from Fudge messages.
 */
@FudgeBuilderFor(CurveSpecificationBuilderConfiguration.class)
public class CurveSpecificationBuilderConfigurationFudgeBuilder implements FudgeBuilder<CurveSpecificationBuilderConfiguration> {

  private static final String CASH = "cashInstrumentProviders";
  private static final String FRA = "fraInstrumentProviders";
  private static final String FRA_3M = "fra3MInstrumentProviders";
  private static final String FRA_6M = "fra6MInstrumentProviders";
  private static final String FUTURE = "futureInstrumentProviders";
  private static final String RATE = "rateInstrumentProviders";
  private static final String LIBOR = "liborInstrumentProviders";
  private static final String EURIBOR = "euriborInstrumentProviders";
  private static final String CDOR = "cdorInstrumentProviders";
  private static final String CIBOR = "ciborInstrumentProviders";
  private static final String STIBOR = "stiborInstrumentProviders";
  private static final String SWAP = "swapInstrumentProviders";
  private static final String SWAP_3M = "swap3MInstrumentProviders";
  private static final String SWAP_6M = "swap6MInstrumentProviders";
  private static final String SWAP_12M = "swap12MInstrumentProviders";
  private static final String BASIS_SWAP = "basisSwapInstrumentProviders";
  private static final String TENOR_SWAP = "tenorSwapInstrumentProviders";
  private static final String OIS_SWAP = "oisSwapInstrumentProviders";
  private static final String SIMPLE_ZERO_DEPOSIT = "simpleZeroDepositInstrumentProviders";
  private static final String PERIODIC_ZERO_DEPOSIT = "periodicZeroDepositInstrumentProviders";
  private static final String CONTINUOUS_ZERO_DEPOSIT = "continuousZeroDepositInstrumentProviders";
  private static final String SWAP_28D = "swap28DInstrumentProviders";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveSpecificationBuilderConfiguration object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, CurveSpecificationBuilderConfiguration.class);

    if (object.getCashInstrumentProviders() != null) {
      final MutableFudgeMsg cashInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getCashInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(cashInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(CASH, cashInstrumentProvidersMessage);
    }

    if (object.getFra3MInstrumentProviders() != null) {
      final MutableFudgeMsg fra3MInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getFra3MInstrumentProviders().entrySet()) {
        if (entry.getKey().getPeriod().toString() == null) {
          throw new OpenGammaRuntimeException("null");
        }
        serializer.addToMessageWithClassHeaders(fra3MInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(FRA_3M, fra3MInstrumentProvidersMessage);
    }

    if (object.getFra6MInstrumentProviders() != null) {
      final MutableFudgeMsg fra6MInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getFra6MInstrumentProviders().entrySet()) {
        if (entry.getKey().getPeriod().toString() == null) {
          throw new OpenGammaRuntimeException("null");
        }
        serializer.addToMessageWithClassHeaders(fra6MInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(FRA_6M, fra6MInstrumentProvidersMessage);
    }

    if (object.getFutureInstrumentProviders() != null) {
      final MutableFudgeMsg futureInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getFutureInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(futureInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(FUTURE, futureInstrumentProvidersMessage);
    }

    if (object.getLiborInstrumentProviders() != null) {
      final MutableFudgeMsg liborInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getLiborInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(liborInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(LIBOR, liborInstrumentProvidersMessage);
    }

    if (object.getEuriborInstrumentProviders() != null) {
      final MutableFudgeMsg euriborInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getEuriborInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(euriborInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(EURIBOR, euriborInstrumentProvidersMessage);
    }

    if (object.getCDORInstrumentProviders() != null) {
      final MutableFudgeMsg cdorInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getCDORInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(cdorInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(CDOR, cdorInstrumentProvidersMessage);
    }

    if (object.getCiborInstrumentProviders() != null) {
      final MutableFudgeMsg ciborInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getCiborInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(ciborInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(CIBOR, ciborInstrumentProvidersMessage);
    }

    if (object.getStiborInstrumentProviders() != null) {
      final MutableFudgeMsg stiborInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getStiborInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(stiborInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(STIBOR, stiborInstrumentProvidersMessage);
    }

    if (object.getSwap3MInstrumentProviders() != null) {
      final MutableFudgeMsg swap3MInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getSwap3MInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(swap3MInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(SWAP_3M, swap3MInstrumentProvidersMessage);
    }

    if (object.getSwap6MInstrumentProviders() != null) {
      final MutableFudgeMsg swap6MInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getSwap6MInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(swap6MInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(SWAP_6M, swap6MInstrumentProvidersMessage);
    }

    if (object.getSwap12MInstrumentProviders() != null) {
      final MutableFudgeMsg swap12MInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getSwap12MInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(swap12MInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(SWAP_12M, swap12MInstrumentProvidersMessage);
    }

    if (object.getSwap28DInstrumentProviders() != null) {
      final MutableFudgeMsg swap28DInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getSwap28DInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(swap28DInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(SWAP_28D, swap28DInstrumentProvidersMessage);
    }

    if (object.getBasisSwapInstrumentProviders() != null) {
      final MutableFudgeMsg basisSwapInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getBasisSwapInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(basisSwapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(BASIS_SWAP, basisSwapInstrumentProvidersMessage);
    }

    if (object.getTenorSwapInstrumentProviders() != null) {
      final MutableFudgeMsg tenorSwapInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getTenorSwapInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(tenorSwapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(TENOR_SWAP, tenorSwapInstrumentProvidersMessage);
    }

    if (object.getOISSwapInstrumentProviders() != null) {
      final MutableFudgeMsg oisSwapInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getOISSwapInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(oisSwapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(OIS_SWAP, oisSwapInstrumentProvidersMessage);
    }

    if (object.getSimpleZeroDepositInstrumentProviders() != null) {
      final MutableFudgeMsg simpleZeroDepositInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getSimpleZeroDepositInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(simpleZeroDepositInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(SIMPLE_ZERO_DEPOSIT, simpleZeroDepositInstrumentProvidersMessage);
    }

    if (object.getPeriodicZeroDepositInstrumentProviders() != null) {
      final MutableFudgeMsg periodicZeroDepositInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getPeriodicZeroDepositInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(periodicZeroDepositInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(PERIODIC_ZERO_DEPOSIT, periodicZeroDepositInstrumentProvidersMessage);
    }

    if (object.getContinuousZeroDepositInstrumentProviders() != null) {
      final MutableFudgeMsg continuousZeroDepositInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getContinuousZeroDepositInstrumentProviders().entrySet()) {
        serializer.addToMessageWithClassHeaders(continuousZeroDepositInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(CONTINUOUS_ZERO_DEPOSIT, continuousZeroDepositInstrumentProvidersMessage);
    }

    return message;
  }

  @Override
  public CurveSpecificationBuilderConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = null;
    if (message.hasField(CASH)) {
      cashInstrumentProviders = new HashMap<>();
      final FudgeMsg cashInstrumentProvidersMessage = message.getMessage(CASH);
      for (final FudgeField field : cashInstrumentProvidersMessage.getAllFields()) {
        cashInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    if (message.hasField(FRA_3M) && message.hasField(FRA)) {
      throw new OpenGammaRuntimeException("Have message with the old FRA field and the new FRA_3M field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> fra3MInstrumentProviders = null;
    if (message.hasField(FRA)) {
      // Treat all old definitions as if they were 3m FRA rates
      final FudgeMsg fraInstrumentProvidersMessage = message.getMessage(FRA);
      fra3MInstrumentProviders = new HashMap<>();
      for (final FudgeField field : fraInstrumentProvidersMessage.getAllFields()) {
        fra3MInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    } else if (message.hasField(FRA_3M)) {
      fra3MInstrumentProviders = new HashMap<>();
      final FudgeMsg fra3MInstrumentProvidersMessage = message.getMessage(FRA_3M);
      for (final FudgeField field : fra3MInstrumentProvidersMessage.getAllFields()) {
        fra3MInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    if (message.hasField(FRA_6M) && message.hasField(FRA)) {
      throw new OpenGammaRuntimeException("Have message with the old FRA field and the new FRA_6M field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> fra6MInstrumentProviders = null;
    if (message.hasField(FRA_6M)) {
      fra6MInstrumentProviders = new HashMap<>();
      final FudgeMsg fra6MInstrumentProvidersMessage = message.getMessage(FRA_6M);
      for (final FudgeField field : fra6MInstrumentProvidersMessage.getAllFields()) {
        fra6MInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = null;
    if (message.hasField(FUTURE)) {
      futureInstrumentProviders = new HashMap<>();
      final FudgeMsg futureInstrumentProvidersMessage = message.getMessage(FUTURE);
      for (final FudgeField field : futureInstrumentProvidersMessage.getAllFields()) {
        futureInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }
    if (message.hasField(RATE) && message.hasField(LIBOR)) {
      throw new OpenGammaRuntimeException("Have message with old RATE field and new LIBOR field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> liborInstrumentProviders = null;
    if (message.hasField(RATE)) {
      // Treat all old definitions as if they were Libor rates
      liborInstrumentProviders = new HashMap<>();
      final FudgeMsg rateInstrumentProvidersMessage = message.getMessage(RATE);
      for (final FudgeField field : rateInstrumentProvidersMessage.getAllFields()) {
        liborInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    } else if (message.hasField(LIBOR)) {
      liborInstrumentProviders = new HashMap<>();
      final FudgeMsg liborInstrumentProvidersMessage = message.getMessage(LIBOR);
      for (final FudgeField field : liborInstrumentProvidersMessage.getAllFields()) {
        liborInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    if (message.hasField(RATE) && message.hasField(EURIBOR)) {
      throw new OpenGammaRuntimeException("Have message with old RATE field and new EURIBOR field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> euriborInstrumentProviders = null;
    if (message.hasField(EURIBOR)) {
      euriborInstrumentProviders = new HashMap<>();
      final FudgeMsg euriborInstrumentProvidersMessage = message.getMessage(EURIBOR);
      for (final FudgeField field : euriborInstrumentProvidersMessage.getAllFields()) {
        euriborInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> cdorInstrumentProviders = null;
    if (message.hasField(CDOR)) {
      cdorInstrumentProviders = new HashMap<>();
      final FudgeMsg cdorInstrumentProvidersMessage = message.getMessage(CDOR);
      for (final FudgeField field : cdorInstrumentProvidersMessage.getAllFields()) {
        cdorInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> ciborInstrumentProviders = null;
    if (message.hasField(CIBOR)) {
      ciborInstrumentProviders = new HashMap<>();
      final FudgeMsg ciborInstrumentProvidersMessage = message.getMessage(CIBOR);
      for (final FudgeField field : ciborInstrumentProvidersMessage.getAllFields()) {
        ciborInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> stiborInstrumentProviders = null;
    if (message.hasField(STIBOR)) {
      stiborInstrumentProviders = new HashMap<>();
      final FudgeMsg stiborInstrumentProvidersMessage = message.getMessage(STIBOR);
      for (final FudgeField field : stiborInstrumentProvidersMessage.getAllFields()) {
        stiborInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    if (message.hasField(SWAP) && message.hasField(SWAP_3M)) {
      throw new OpenGammaRuntimeException("Have message with old SWAP field and new SWAP_3M field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> swap3MInstrumentProviders = null;
    if (message.hasField(SWAP)) {
      // Treat all old definitions as if they were swaps with 3m floating legs
      swap3MInstrumentProviders = new HashMap<>();
      final FudgeMsg swapInstrumentProvidersMessage = message.getMessage(SWAP);
      for (final FudgeField field : swapInstrumentProvidersMessage.getAllFields()) {
        swap3MInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    } else if (message.hasField(SWAP_3M)) {
      swap3MInstrumentProviders = new HashMap<>();
      final FudgeMsg swap3MInstrumentProvidersMessage = message.getMessage(SWAP_3M);
      for (final FudgeField field : swap3MInstrumentProvidersMessage.getAllFields()) {
        swap3MInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    if (message.hasField(SWAP) && message.hasField(SWAP_6M)) {
      throw new OpenGammaRuntimeException("Have message with old SWAP field and new SWAP_6M field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> swap6MInstrumentProviders = null;
    if (message.hasField(SWAP_6M)) {
      swap6MInstrumentProviders = new HashMap<>();
      final FudgeMsg swap6MInstrumentProvidersMessage = message.getMessage(SWAP_6M);
      for (final FudgeField field : swap6MInstrumentProvidersMessage.getAllFields()) {
        swap6MInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> swap12MInstrumentProviders = null;
    if (message.hasField(SWAP_12M)) {
      swap12MInstrumentProviders = new HashMap<>();
      final FudgeMsg swap12MInstrumentProvidersMessage = message.getMessage(SWAP_12M);
      for (final FudgeField field : swap12MInstrumentProvidersMessage.getAllFields()) {
        swap12MInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> swap28DInstrumentProviders = null;
    if (message.hasField(SWAP_28D)) {
      swap28DInstrumentProviders = new HashMap<>();
      final FudgeMsg swap28DInstrumentProvidersMessage = message.getMessage(SWAP_28D);
      for (final FudgeField field : swap28DInstrumentProvidersMessage.getAllFields()) {
        swap28DInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = null;
    if (message.hasField(BASIS_SWAP)) {
      basisSwapInstrumentProviders = new HashMap<>();
      final FudgeMsg basisSwapInstrumentProvidersMessage = message.getMessage(BASIS_SWAP);
      for (final FudgeField field : basisSwapInstrumentProvidersMessage.getAllFields()) {
        basisSwapInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = null;
    if (message.hasField(TENOR_SWAP)) {
      tenorSwapInstrumentProviders = new HashMap<>();
      final FudgeMsg tenorSwapInstrumentProvidersMessage = message.getMessage(TENOR_SWAP);
      for (final FudgeField field : tenorSwapInstrumentProvidersMessage.getAllFields()) {
        tenorSwapInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders = null;
    if (message.hasField(OIS_SWAP)) {
      final FudgeMsg oisSwapInstrumentProvidersMessage = message.getMessage(OIS_SWAP);
      oisSwapInstrumentProviders = new HashMap<>();
      for (final FudgeField field : oisSwapInstrumentProvidersMessage.getAllFields()) {
        oisSwapInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> simpleZeroDepositInstrumentProviders = null;
    if (message.hasField(SIMPLE_ZERO_DEPOSIT)) {
      final FudgeMsg simpleZeroDepositInstrumentProvidersMessage = message.getMessage(SIMPLE_ZERO_DEPOSIT);
      simpleZeroDepositInstrumentProviders = new HashMap<>();
      for (final FudgeField field : simpleZeroDepositInstrumentProvidersMessage.getAllFields()) {
        simpleZeroDepositInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> periodicZeroDepositInstrumentProviders = null;
    if (message.hasField(PERIODIC_ZERO_DEPOSIT)) {
      final FudgeMsg periodicZeroDepositInstrumentProvidersMessage = message.getMessage(PERIODIC_ZERO_DEPOSIT);
      periodicZeroDepositInstrumentProviders = new HashMap<>();
      for (final FudgeField field : periodicZeroDepositInstrumentProvidersMessage.getAllFields()) {
        periodicZeroDepositInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> continuousZeroDepositInstrumentProviders = null;
    if (message.hasField(CONTINUOUS_ZERO_DEPOSIT)) {
      final FudgeMsg continuousZeroDepositInstrumentProvidersMessage = message.getMessage(CONTINUOUS_ZERO_DEPOSIT);
      continuousZeroDepositInstrumentProviders = new HashMap<>();
      for (final FudgeField field : continuousZeroDepositInstrumentProvidersMessage.getAllFields()) {
        continuousZeroDepositInstrumentProviders.put(Tenor.of(DateUtils.toPeriod(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    return new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, fra3MInstrumentProviders, fra6MInstrumentProviders, liborInstrumentProviders, euriborInstrumentProviders,
        cdorInstrumentProviders, ciborInstrumentProviders, stiborInstrumentProviders, futureInstrumentProviders, swap6MInstrumentProviders, swap3MInstrumentProviders, basisSwapInstrumentProviders,
        tenorSwapInstrumentProviders, oisSwapInstrumentProviders, simpleZeroDepositInstrumentProviders, periodicZeroDepositInstrumentProviders, continuousZeroDepositInstrumentProviders,
        swap12MInstrumentProviders, swap28DInstrumentProviders);
  }
}
