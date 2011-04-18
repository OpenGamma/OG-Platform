package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.SyntheticIdentifierCurveInstrumentProvider;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.util.money.Currency;

/**
 * Fudge builder for SyntheticIdentifierCurveInstrumentProvider
 */
@FudgeBuilderFor(SyntheticIdentifierCurveInstrumentProvider.class)
public class SyntheticIdentifierCurveInstrumentProviderBuilder implements FudgeBuilder<SyntheticIdentifierCurveInstrumentProvider> {
  /**
   * type used as a human readable subclass discriminator for mongo (which strips out type information).
   * REVIEW: jim 13-Apr-2011 -- above comment obsolete as we're no using Mongo for config storage any more, this should be refactored at some point.
   */
  public static final String TYPE = "Synthetic";
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, SyntheticIdentifierCurveInstrumentProvider object) {
    MutableFudgeMsg message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, SyntheticIdentifierCurveInstrumentProvider.class);
    message.add("type", TYPE); // so we can tell what type it is when mongo throws away the class header.
    message.add("ccy", object.getCurrency().getCode());
    message.add("stripType", object.getType().name());
    message.add("scheme", object.getScheme().getName());
    return message; 
  }

  @Override
  public SyntheticIdentifierCurveInstrumentProvider buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    Currency ccy = Currency.of(message.getString("ccy"));
    StripInstrumentType stripType = StripInstrumentType.valueOf(message.getString("stripType"));
    IdentificationScheme scheme = IdentificationScheme.of(message.getString("scheme"));
    return new SyntheticIdentifierCurveInstrumentProvider(ccy, stripType, scheme);
  }

}
