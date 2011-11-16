/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.calendar.Period;

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
  private static final String BASIS_SWAP = "basisSwapInstrumentProviders";
  private static final String TENOR_SWAP = "tenorSwapInstrumentProviders";
  private static final String OIS_SWAP = "oisSwapInstrumentProviders";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveSpecificationBuilderConfiguration object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, CurveSpecificationBuilderConfiguration.class);

    if (object.getCashInstrumentProviders() != null) {
      final MutableFudgeMsg cashInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getCashInstrumentProviders().entrySet()) {
        serializer.addToMessage(cashInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(CASH, cashInstrumentProvidersMessage);
    }

    if (object.getFra3MInstrumentProviders() != null) {
      final MutableFudgeMsg fra3MInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getFra3MInstrumentProviders().entrySet()) {
        if (entry.getKey().getPeriod().toString() == null) {
          throw new OpenGammaRuntimeException("null");
        }
        serializer.addToMessage(fra3MInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(FRA_3M, fra3MInstrumentProvidersMessage);
    }

    if (object.getFra6MInstrumentProviders() != null) {
      final MutableFudgeMsg fra6MInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getFra6MInstrumentProviders().entrySet()) {
        if (entry.getKey().getPeriod().toString() == null) {
          throw new OpenGammaRuntimeException("null");
        }
        serializer.addToMessage(fra6MInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(FRA_6M, fra6MInstrumentProvidersMessage);
    }

    if (object.getFutureInstrumentProviders() != null) {
      final MutableFudgeMsg futureInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getFutureInstrumentProviders().entrySet()) {
        serializer.addToMessage(futureInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(FUTURE, futureInstrumentProvidersMessage);
    }

    if (object.getLiborInstrumentProviders() != null) {
      final MutableFudgeMsg liborInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getLiborInstrumentProviders().entrySet()) {
        serializer.addToMessage(liborInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(LIBOR, liborInstrumentProvidersMessage);
    }

    if (object.getEuriborInstrumentProviders() != null) {
      final MutableFudgeMsg euriborInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getEuriborInstrumentProviders().entrySet()) {
        serializer.addToMessage(euriborInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(EURIBOR, euriborInstrumentProvidersMessage);
    }

    if (object.getCDORInstrumentProviders() != null) {
      final MutableFudgeMsg cdorInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getCDORInstrumentProviders().entrySet()) {
        serializer.addToMessage(cdorInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(CDOR, cdorInstrumentProvidersMessage);
    }

    if (object.getCiborInstrumentProviders() != null) {
      final MutableFudgeMsg ciborInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getCiborInstrumentProviders().entrySet()) {
        serializer.addToMessage(ciborInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(CIBOR, ciborInstrumentProvidersMessage);
    }

    if (object.getStiborInstrumentProviders() != null) {
      final MutableFudgeMsg stiborInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getStiborInstrumentProviders().entrySet()) {
        serializer.addToMessage(stiborInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(STIBOR, stiborInstrumentProvidersMessage);
    }

    if (object.getSwap3MInstrumentProviders() != null) {
      final MutableFudgeMsg swap3MInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getSwap3MInstrumentProviders().entrySet()) {
        serializer.addToMessage(swap3MInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(SWAP_3M, swap3MInstrumentProvidersMessage);
    }

    if (object.getSwap6MInstrumentProviders() != null) {
      final MutableFudgeMsg swap6MInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getSwap6MInstrumentProviders().entrySet()) {
        serializer.addToMessage(swap6MInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(SWAP_6M, swap6MInstrumentProvidersMessage);
    }

    if (object.getBasisSwapInstrumentProviders() != null) {
      final MutableFudgeMsg basisSwapInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getBasisSwapInstrumentProviders().entrySet()) {
        serializer.addToMessage(basisSwapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(BASIS_SWAP, basisSwapInstrumentProvidersMessage);
    }

    if (object.getTenorSwapInstrumentProviders() != null) {
      final MutableFudgeMsg tenorSwapInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getTenorSwapInstrumentProviders().entrySet()) {
        serializer.addToMessage(tenorSwapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(TENOR_SWAP, tenorSwapInstrumentProvidersMessage);
    }

    if (object.getOISSwapInstrumentProviders() != null) {
      final MutableFudgeMsg oisSwapInstrumentProvidersMessage = serializer.newMessage();
      for (final Entry<Tenor, CurveInstrumentProvider> entry : object.getOISSwapInstrumentProviders().entrySet()) {
        serializer.addToMessage(oisSwapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add(OIS_SWAP, oisSwapInstrumentProvidersMessage);
    }

    return message;
  }

  @Override
  public CurveSpecificationBuilderConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = null;
    if (message.hasField(CASH)) {
      cashInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg cashInstrumentProvidersMessage = message.getMessage(CASH);
      for (final FudgeField field : cashInstrumentProvidersMessage.getAllFields()) {
        cashInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    if (message.hasField(FRA_3M) && message.hasField(FRA)) {
      throw new OpenGammaRuntimeException("Have message with the old FRA field and the new FRA_3M field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> fra3MInstrumentProviders = null;
    if (message.hasField(FRA)) {
      // Treat all old definitions as if they were 3m FRA rates
      final FudgeMsg fraInstrumentProvidersMessage = message.getMessage(FRA);
      fra3MInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      for (final FudgeField field : fraInstrumentProvidersMessage.getAllFields()) {
        fra3MInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    } else if (message.hasField(FRA_3M)) {
      fra3MInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg fra3MInstrumentProvidersMessage = message.getMessage(FRA_3M);
      for (final FudgeField field : fra3MInstrumentProvidersMessage.getAllFields()) {
        fra3MInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    if (message.hasField(FRA_6M) && message.hasField(FRA)) {
      throw new OpenGammaRuntimeException("Have message with the old FRA field and the new FRA_6M field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> fra6MInstrumentProviders = null;
    if (message.hasField(FRA_6M)) {
      fra6MInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg fra6MInstrumentProvidersMessage = message.getMessage(FRA_6M);
      for (final FudgeField field : fra6MInstrumentProvidersMessage.getAllFields()) {
        fra6MInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = null;
    if (message.hasField(FUTURE)) {
      futureInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg futureInstrumentProvidersMessage = message.getMessage(FUTURE);
      for (final FudgeField field : futureInstrumentProvidersMessage.getAllFields()) {
        futureInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }
    if (message.hasField(RATE) && message.hasField(LIBOR)) {
      throw new OpenGammaRuntimeException("Have message with old RATE field and new LIBOR field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> liborInstrumentProviders = null;
    if (message.hasField(RATE)) {
      // Treat all old definitions as if they were Libor rates
      liborInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg rateInstrumentProvidersMessage = message.getMessage(RATE);
      for (final FudgeField field : rateInstrumentProvidersMessage.getAllFields()) {
        liborInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    } else if (message.hasField(LIBOR)) {
      liborInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg liborInstrumentProvidersMessage = message.getMessage(LIBOR);
      for (final FudgeField field : liborInstrumentProvidersMessage.getAllFields()) {
        liborInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    if (message.hasField(RATE) && message.hasField(EURIBOR)) {
      throw new OpenGammaRuntimeException("Have message with old RATE field and new EURIBOR field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> euriborInstrumentProviders = null;
    if (message.hasField(EURIBOR)) {
      euriborInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg euriborInstrumentProvidersMessage = message.getMessage(EURIBOR);
      for (final FudgeField field : euriborInstrumentProvidersMessage.getAllFields()) {
        euriborInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> cdorInstrumentProviders = null;
    if (message.hasField(CDOR)) {
      cdorInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg cdorInstrumentProvidersMessage = message.getMessage(CDOR);
      for (final FudgeField field : cdorInstrumentProvidersMessage.getAllFields()) {
        cdorInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> ciborInstrumentProviders = null;
    if (message.hasField(CIBOR)) {
      ciborInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg ciborInstrumentProvidersMessage = message.getMessage(CIBOR);
      for (final FudgeField field : ciborInstrumentProvidersMessage.getAllFields()) {
        ciborInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> stiborInstrumentProviders = null;
    if (message.hasField(STIBOR)) {
      stiborInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg stiborInstrumentProvidersMessage = message.getMessage(STIBOR);
      for (final FudgeField field : stiborInstrumentProvidersMessage.getAllFields()) {
        stiborInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    if (message.hasField(SWAP) && message.hasField(SWAP_3M)) {
      throw new OpenGammaRuntimeException("Have message with old SWAP field and new SWAP_3M field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> swap3MInstrumentProviders = null;
    if (message.hasField(SWAP)) {
      // Treat all old definitions as if they were swaps with 3m floating legs
      swap3MInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg swapInstrumentProvidersMessage = message.getMessage(SWAP);
      for (final FudgeField field : swapInstrumentProvidersMessage.getAllFields()) {
        swap3MInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    } else if (message.hasField(SWAP_3M)) {
      swap3MInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg swap3MInstrumentProvidersMessage = message.getMessage(SWAP_3M);
      for (final FudgeField field : swap3MInstrumentProvidersMessage.getAllFields()) {
        swap3MInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    if (message.hasField(SWAP) && message.hasField(SWAP_6M)) {
      throw new OpenGammaRuntimeException("Have message with old SWAp field and new SWAP_6M field: should not happen");
    }

    Map<Tenor, CurveInstrumentProvider> swap6MInstrumentProviders = null;
    if (message.hasField(SWAP_6M)) {
      swap6MInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg swap6MInstrumentProvidersMessage = message.getMessage(SWAP_6M);
      for (final FudgeField field : swap6MInstrumentProvidersMessage.getAllFields()) {
        swap6MInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = null;
    if (message.hasField(BASIS_SWAP)) {
      basisSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg basisSwapInstrumentProvidersMessage = message.getMessage(BASIS_SWAP);
      for (final FudgeField field : basisSwapInstrumentProvidersMessage.getAllFields()) {
        basisSwapInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    if (message.hasField(TENOR_SWAP)) {
      tenorSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      final FudgeMsg tenorSwapInstrumentProvidersMessage = message.getMessage(TENOR_SWAP);
      for (final FudgeField field : tenorSwapInstrumentProvidersMessage.getAllFields()) {
        tenorSwapInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders = null;
    if (message.hasField(OIS_SWAP)) {
      final FudgeMsg oisSwapInstrumentProvidersMessage = message.getMessage(OIS_SWAP);
      oisSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      for (final FudgeField field : oisSwapInstrumentProvidersMessage.getAllFields()) {
        oisSwapInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }

    return new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, fra3MInstrumentProviders, fra6MInstrumentProviders, liborInstrumentProviders, euriborInstrumentProviders,
        cdorInstrumentProviders, ciborInstrumentProviders, stiborInstrumentProviders, futureInstrumentProviders, swap6MInstrumentProviders, swap3MInstrumentProviders, basisSwapInstrumentProviders,
        tenorSwapInstrumentProviders, oisSwapInstrumentProviders);
  }
}
