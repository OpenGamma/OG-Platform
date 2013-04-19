/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Lists;
import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * A Fudge builder for {@link CreditDefaultSwapIndexDefinitionSecurity}.
 */
@FudgeBuilderFor(CreditDefaultSwapIndexDefinitionSecurity.class)
public class CDSIndexDefinitionSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<CreditDefaultSwapIndexDefinitionSecurity> {
  
  /** Field name. */
  public static final String VERSION_FLD = "version";
  /** Field name. */
  public static final String SERIES_FLD = "series";
  /** Field name. */
  public static final String FAMILY_FLD = "family";
  /** Field name. */
  public static final String CURRENCY_FLD = "currency";
  /** Field name. */
  public static final String RECOVERY_RATE_FLD = "recoveryRate";
  /** Field name. */
  public static final String TERMS_FLD = "terms";
  /** Field name. */
  public static final String COMPONENTS_FLD = "components";
 
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CreditDefaultSwapIndexDefinitionSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    CDSIndexDefinitionSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, CreditDefaultSwapIndexDefinitionSecurity security, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, security, msg);
    addToMessage(msg, VERSION_FLD, security.getVersion());
    addToMessage(msg, SERIES_FLD, security.getSeries());
    addToMessage(msg, FAMILY_FLD, security.getFamily());
    addToMessage(msg, CURRENCY_FLD, security.getCurrency());
    addToMessage(msg, RECOVERY_RATE_FLD, security.getRecoveryRate());
    for (Tenor tenor : security.getTerms()) {
      addToMessage(serializer, msg, TERMS_FLD, tenor, Tenor.class);
    }
    for (CreditDefaultSwapIndexComponent component : security.getComponents()) {
      addToMessage(serializer, msg, COMPONENTS_FLD, component, CreditDefaultSwapIndexComponent.class);
    }
  }

  @Override
  public CreditDefaultSwapIndexDefinitionSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    CreditDefaultSwapIndexDefinitionSecurity security = new CreditDefaultSwapIndexDefinitionSecurity();
    CDSIndexDefinitionSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, security);
    return security;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, CreditDefaultSwapIndexDefinitionSecurity security) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, security);
    
    List<FudgeField> termsFields = msg.getAllByName(TERMS_FLD);
    List<Tenor> tenors = Lists.newArrayList();
    for (FudgeField field : termsFields) {
      tenors.add(deserializer.fieldValueToObject(Tenor.class, field));
    }
    security.setTerms(CDSIndexTerms.of(tenors));
    
    List<FudgeField> componentsFields = msg.getAllByName(COMPONENTS_FLD);
    List<CreditDefaultSwapIndexComponent> components = Lists.newArrayList();
    for (FudgeField field : componentsFields) {
      components.add(deserializer.fieldValueToObject(CreditDefaultSwapIndexComponent.class, field));
    }
    security.setComponents(CDSIndexComponentBundle.of(components));
    security.setVersion(msg.getString(VERSION_FLD));
    security.setSeries(msg.getString(SERIES_FLD));
    security.setFamily(msg.getString(FAMILY_FLD));
    security.setCurrency(msg.getValue(Currency.class, CURRENCY_FLD));
    security.setRecoveryRate(msg.getDouble(RECOVERY_RATE_FLD));
  }

}
