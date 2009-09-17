/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.engine.analytics.AbstractAnalyticValue;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.HardCodedUSDDiscountCurveAnalyticFunction;
import com.opengamma.engine.analytics.InMemoryAnalyticFunctionRepository;
import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.position.csv.CSVPositionMaster;
import com.opengamma.engine.security.InMemorySecurityMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityIdentificationDomain;
import com.opengamma.engine.security.SecurityIdentifier;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;

/**
 * 
 *
 * @author kirk
 */
public class ViewImplTest {
  
  @Test
  public void trivialExampleSingleCycle() throws Exception {
    ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("Kirk", "KirkPortfolio");
    viewDefinition.addValueDefinition("KIRK", HardCodedUSDDiscountCurveAnalyticFunction.getDiscountCurveValueDefinition());
    final Portfolio portfolio = CSVPositionMaster.loadPortfolio("KirkPortfolio", getClass().getResourceAsStream("KirkPortfolio.txt"));
    PositionMaster positionMaster = new PositionMaster() {
      @Override
      public Portfolio getRootPortfolio(String portfolioName) {
        return portfolio;
      }

      @Override
      public Collection<String> getRootPortfolioNames() {
        return Collections.singleton(portfolio.getName());
      }
    };
    Security security = new Security() {
      @Override
      public Collection<SecurityIdentifier> getIdentifiers() {
        return Collections.singleton(new SecurityIdentifier(new SecurityIdentificationDomain("KIRK"), "ID1"));
      }
      @Override
      public String getSecurityType() {
        return "KIRK";
      }
    };
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    secMaster.add(security);
    
    HardCodedUSDDiscountCurveAnalyticFunction function = new HardCodedUSDDiscountCurveAnalyticFunction();
    InMemoryAnalyticFunctionRepository functionRepo = new InMemoryAnalyticFunctionRepository(Collections.singleton(function));
    
    FixedLiveDataAvailabilityProvider ldap = new FixedLiveDataAvailabilityProvider();
    for(AnalyticValueDefinition definition : function.getInputs(security)) {
      ldap.addDefinition(definition);
    }
    
    ViewComputationCacheFactory cacheFactory = new ViewComputationCacheFactory() {
      @Override
      public ViewComputationCache generateCache() {
        return new MapViewComputationCache();
      }
    };
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    populateInitialSnapshot(snapshotProvider);
    
    ViewImpl view = new ViewImpl(viewDefinition);
    view.setPositionMaster(positionMaster);
    view.setAnalyticFunctionRepository(functionRepo);
    view.setLiveDataAvailabilityProvider(ldap);
    view.setSecurityMaster(secMaster);
    view.setComputationCacheFactory(cacheFactory);
    view.setLiveDataSnapshotProvider(snapshotProvider);
    
    view.init();
    assertEquals(ViewCalculationState.NOT_STARTED, view.getCalculationState());
    view.runOneCycle();
    ViewComputationResultModel result = view.getMostRecentResult();
    assertNotNull(result);
    Collection<Position> positions = result.getPositions();
    assertNotNull(positions);
    assertEquals(1, positions.size());
    Position resultPosition = positions.iterator().next();
    assertNotNull(resultPosition);
    assertEquals(new BigDecimal(9873), resultPosition.getQuantity());
    
    Map<AnalyticValueDefinition, AnalyticValue> resultValues = result.getValues(resultPosition);
    assertNotNull(resultValues);
    assertEquals(1, resultValues.size());
    AnalyticValue discountCurveValue = resultValues.get(HardCodedUSDDiscountCurveAnalyticFunction.getDiscountCurveValueDefinition());
    assertNotNull(discountCurveValue);
    assertEquals(HardCodedUSDDiscountCurveAnalyticFunction.getDiscountCurveValueDefinition(), discountCurveValue.getDefinition());
    assertNotNull(discountCurveValue.getValue());
    assertTrue(discountCurveValue.getValue() instanceof DiscountCurve);
    DiscountCurve theCurveItself = (DiscountCurve) discountCurveValue.getValue();
    System.out.println("Discount Curve is " + theCurveItself.getData());
    System.out.println("Discount Curve is " + theCurveItself.getInterestRate(3.5));
  }

  /**
   * @param function 
   * @param snapshotProvider 
   * 
   */
  private void populateInitialSnapshot(
      InMemoryLKVSnapshotProvider snapshotProvider) {
    // Inflection point is 10.
    double currValue = 0.005;
    for(int i = 0; i < HardCodedUSDDiscountCurveAnalyticFunction.STRIPS.length; i++) {
      String strip = HardCodedUSDDiscountCurveAnalyticFunction.STRIPS[i];
      final Map<String, Double> dataFields = new HashMap<String, Double>();
      dataFields.put(HardCodedUSDDiscountCurveAnalyticFunction.PRICE_FIELD_NAME, currValue);
      final AnalyticValueDefinition definition = HardCodedUSDDiscountCurveAnalyticFunction.constructDefinition(strip);
      AnalyticValue value = new AbstractAnalyticValue(definition, dataFields) {
        @Override
        public AnalyticValue scaleForPosition(BigDecimal quantity) {
          return this;
        }
      };
      snapshotProvider.addValue(value);
      
      if(i < 10) {
        currValue += 0.005;
      } else {
        currValue -= 0.001;
      }
    }
  }

}
