/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.security;

import java.util.Arrays;
import java.util.List;

import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.language.client.ContextRemoteClient;
import com.opengamma.language.client.MasterID;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.identifier.ExternalSchemeRank;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;

/**
 * Writes a security to the {@link SecurityMaster}.
 */
public class StoreSecurityProcedure extends AbstractProcedureInvoker.SingleResult implements PublishedProcedure {

  /**
   * Default instance.
   */
  public static final StoreSecurityProcedure INSTANCE = new StoreSecurityProcedure();

  private final MetaProcedure _meta;

  private static final int SECURITY = 0;
  private static final int IDENTIFIER = 1;
  private static final int MASTER = 2;

  private static List<MetaParameter> parameters() {
    final MetaParameter security = new MetaParameter("security", JavaTypeInfo.builder(ManageableSecurity.class).get());
    final MetaParameter identifier = new MetaParameter("identifier", JavaTypeInfo.builder(UniqueId.class).allowNull().get());
    final MetaParameter target = new MetaParameter("master", JavaTypeInfo.builder(MasterID.class).defaultValue(MasterID.SESSION).get());
    return Arrays.asList(security, identifier, target);
  }

  private StoreSecurityProcedure(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaProcedure(Categories.SECURITY, "StoreSecurity", getParameters(), this));
  }

  protected StoreSecurityProcedure() {
    this(new DefinitionAnnotater(StoreSecurityProcedure.class));
  }

  // TODO: Move SecurityPersister from OG-Excel into OG-Language and use that here

  protected static ExternalId invoke(final SecurityMaster master, final UniqueId identifier, ManageableSecurity security) {
    SecurityDocument document = new SecurityDocument(security);
    document.setUniqueId(identifier);
    if (identifier == null) {
      security.setExternalIdBundle(security.getExternalIdBundle().withoutScheme(ExternalScheme.of("UID")));
      document = master.add(document);
      security = document.getSecurity();
      security.setExternalIdBundle(security.getExternalIdBundle().withExternalId(document.getUniqueId().toExternalId()));
      document = master.update(document);
    } else {
      document = master.update(document);
    }
    return ExternalSchemeRank.DEFAULT.getPreferredIdentifier(document.getSecurity().getExternalIdBundle());
  }

  public static ExternalId invoke(final SessionContext sessionContext, final ManageableSecurity security, final UniqueId identifier, final MasterID master) {
    final RemoteClient client = ContextRemoteClient.get(sessionContext, master);
    final SecurityMaster secMaster;
    try {
      secMaster = client.getSecurityMaster();
    } catch (UnsupportedOperationException e) {
      throw new InvokeInvalidArgumentException(MASTER, e);
    }
    return invoke(secMaster, identifier, security);
  }

  // AbstractProcedureInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (ManageableSecurity) parameters[SECURITY], (UniqueId) parameters[IDENTIFIER], (MasterID) parameters[MASTER]);
  }

  // PublishedProcedure

  @Override
  public MetaProcedure getMetaProcedure() {
    return _meta;
  }

}
