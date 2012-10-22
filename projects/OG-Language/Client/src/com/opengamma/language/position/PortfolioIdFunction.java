/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.position;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.id.UniqueId;
import com.opengamma.language.client.CombinedPortfolioMaster;
import com.opengamma.language.client.CombiningMaster;
import com.opengamma.language.client.MasterID;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchSortOrder;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Returns the identifier of a portfolio with a given name.
 */
public class PortfolioIdFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final PortfolioIdFunction INSTANCE = new PortfolioIdFunction();
  
  private final MetaFunction _meta;
  
  private static List<MetaParameter> parameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).get());
    return ImmutableList.of(name);
  }
  
  protected PortfolioIdFunction() {
    this(new DefinitionAnnotater(PortfolioIdFunction.class));
  }
  
  private PortfolioIdFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.POSITION, "PortfolioId", getParameters(), this));
  }
  
  public UniqueId invoke(final CombinedPortfolioMaster combinedPortfolioMaster, final String portfolioName) {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(portfolioName);
    request.setDepth(0);
    request.setSortOrder(PortfolioSearchSortOrder.VERSION_FROM_INSTANT_DESC);
    request.setVisibility(DocumentVisibility.HIDDEN);
    final List<UniqueId> resultIds = new ArrayList<UniqueId>();
    combinedPortfolioMaster.search(request, new CombinedPortfolioMaster.SearchCallback() {

      @Override
      public boolean include(PortfolioDocument document) {
        final String name = document.getPortfolio().getName();
        return name.charAt(0) != '.';
      }

      @Override
      public void accept(PortfolioDocument document, MasterID master, boolean masterUnique, boolean clientUnique) {
        final ManageablePortfolio portfolio = document.getPortfolio();
        resultIds.add(portfolio.getUniqueId());
      }

      @Override
      public int compare(PortfolioDocument o1, PortfolioDocument o2) {
        return o1.getPortfolio().getName().compareToIgnoreCase(o2.getPortfolio().getName());
      }

    });
    if (resultIds.size() > 0) {
      return resultIds.get(0);
    } else {
      throw new InvokeInvalidArgumentException("No matching portfolio found");
    }    
  }

  @Override
  protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) throws AsynchronousExecution {
    final String portfolioName = (String) parameters[0];
    return invoke(CombiningMaster.PORTFOLIO.get(sessionContext), portfolioName);
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }  
  
}
