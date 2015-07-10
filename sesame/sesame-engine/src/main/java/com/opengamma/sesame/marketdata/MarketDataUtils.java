/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.springframework.jms.core.JmsTemplate;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.client.JmsLiveDataClient;
import com.opengamma.provider.livedata.LiveDataMetaData;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.LiveDataServerTypes;
import com.opengamma.sesame.marketdata.scenarios.FilteredPerturbation;
import com.opengamma.sesame.marketdata.scenarios.MatchDetails;
import com.opengamma.sesame.marketdata.scenarios.Perturbation;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.transport.jms.JmsByteArrayRequestSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import com.opengamma.util.metric.OpenGammaMetricRegistry;
import com.opengamma.util.money.Currency;

/**
 * Market data util methods.
 */
public class MarketDataUtils {

  /** The default field name used for looking up data in a market data record. */
  static final FieldName MARKET_VALUE = FieldName.of(MarketDataRequirementNames.MARKET_VALUE);

  private MarketDataUtils() {
  }

  static LocalDateDoubleTimeSeries asLocalDateDoubleTimeSeries(DateTimeSeries<LocalDate, Double> timeSeries) {
    ArgumentChecker.notNull(timeSeries, "timeSeries");

    if (timeSeries instanceof LocalDateDoubleTimeSeries) {
      return (LocalDateDoubleTimeSeries) timeSeries;
    }
    throw new IllegalArgumentException("Time series of type " + timeSeries.getClass().getName() + " cannot be " +
                                           "converted to a LocalDateDoubleTimeSeries");
  }

  /**
   * Creates a live data client based on the information in the remote metadata.
   * <p>
   * This was copy-pasted from {@code LiveMarketDataProviderFactoryComponentFactory} because having application code
   * depending on configuration code seems particularly nasty.
   *
   * @param provider the metadata provider
   * @param jmsConnector the JMS connector
   * @return the client
   */
  @SuppressWarnings("deprecation")
  static LiveDataClient createLiveDataClient(LiveDataMetaDataProvider provider, JmsConnector jmsConnector) {
    ArgumentChecker.notNull(jmsConnector, "jmsConnector");
    ArgumentChecker.notNull(provider, "provider");

    LiveDataMetaData metaData = provider.metaData();
    URI jmsUri = metaData.getJmsBrokerUri();

    if (metaData.getServerType() != LiveDataServerTypes.STANDARD || jmsUri == null) {
      throw new IllegalArgumentException("Unsupported live data server type " + metaData.getServerType() +
                                             " for " + metaData.getDescription() + " live data provider.");
    }
    if (!jmsConnector.getClientBrokerUri().equals(jmsUri)) {
      JmsConnectorFactoryBean jmsFactory = new JmsConnectorFactoryBean(jmsConnector);
      jmsFactory.setClientBrokerUri(jmsUri);
      jmsConnector = jmsFactory.getObjectCreating();
    }
    JmsTemplate jmsTemplate = jmsConnector.getJmsTemplateTopic();
    JmsByteArrayRequestSender jmsSubscriptionRequestSender;

    if (metaData.getJmsSubscriptionQueue() != null) {
      JmsTemplate subscriptionRequestTemplate = jmsConnector.getJmsTemplateQueue();
      jmsSubscriptionRequestSender = new JmsByteArrayRequestSender(metaData.getJmsSubscriptionQueue(),
                                                                   subscriptionRequestTemplate);
    } else {
      jmsSubscriptionRequestSender = new JmsByteArrayRequestSender(metaData.getJmsSubscriptionTopic(), jmsTemplate);
    }
    ByteArrayFudgeRequestSender fudgeSubscriptionRequestSender =
        new ByteArrayFudgeRequestSender(jmsSubscriptionRequestSender);

    JmsByteArrayRequestSender jmsEntitlementRequestSender =
        new JmsByteArrayRequestSender(metaData.getJmsEntitlementTopic(), jmsTemplate);
    ByteArrayFudgeRequestSender fudgeEntitlementRequestSender =
        new ByteArrayFudgeRequestSender(jmsEntitlementRequestSender);

    JmsLiveDataClient liveDataClient = new JmsLiveDataClient(fudgeSubscriptionRequestSender,
                                                             fudgeEntitlementRequestSender,
                                                             jmsConnector,
                                                             OpenGammaFudgeContext.getInstance(),
                                                             JmsLiveDataClient.DEFAULT_NUM_SESSIONS);
    liveDataClient.setFudgeContext(OpenGammaFudgeContext.getInstance());

    if (metaData.getJmsHeartbeatTopic() != null) {
      JmsByteArrayMessageSender jmsHeartbeatSender =
          new JmsByteArrayMessageSender(metaData.getJmsHeartbeatTopic(), jmsTemplate);
      liveDataClient.setHeartbeatMessageSender(jmsHeartbeatSender);
    }
    liveDataClient.start();
    liveDataClient.registerMetrics(OpenGammaMetricRegistry.getSummaryInstance(),
                                   OpenGammaMetricRegistry.getDetailedInstance(),
                                   "LiveDataClient - " + provider.metaData().getDescription());
    return liveDataClient;
  }

  // TODO Java 8 - replace with stream().filter()
  /**
   * Returns the perturbations whose {@link Perturbation#getMarketDataType() data type} equals {@code dataType}
   * and {@link Perturbation#getMatchDetailsType()} equals {@code matchDetailsType}.
   *
   * @param filteredPerturbations market data perturbations
   * @param dataType a type of market data or data used to build market data that the returned perturbations must handle
   * @param matchDetailsType a type of {@link MatchDetails} that the returned perturbations must handle
   * @return the perturbations that can act on the specified data type
   */
  static <T> Collection<FilteredPerturbation> filterPerturbations(
      Collection<FilteredPerturbation> filteredPerturbations,
      Class<T> dataType,
      Class<? extends MatchDetails> matchDetailsType) {

    ArgumentChecker.notNull(filteredPerturbations, "perturbationMatches");
    ArgumentChecker.notNull(dataType, "dataType");

    ImmutableList.Builder<FilteredPerturbation> builder = ImmutableList.builder();

    for (FilteredPerturbation filteredPerturbation : filteredPerturbations) {
      Perturbation perturbation = filteredPerturbation.getPerturbation();
      if (perturbation.getMarketDataType().equals(dataType) &&
          perturbation.getMatchDetailsType().equals(matchDetailsType)) {

        builder.add(filteredPerturbation);
      }
    }
    return builder.build();
  }

  /**
   * Returns a new multicurve with one of its curves replaced. The new curve replaces any curves with the same name.
   * If the curve appears multiple times in the multicurve it is replaced everywhere. If there is no curve
   * in the multicurve with a matching name the returned multicurve is equal to the input.
   *
   * @param multicurve a multicurve
   * @param curve a curve
   * @return a new multicurve with a curve replaced
   */
  public static MulticurveProviderDiscount replaceCurve(MulticurveProviderDiscount multicurve,
                                                        YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(curve, "curve");

    MulticurveProviderDiscount updatedProvider = multicurve;

    List<Currency> currencies = multicurve.getCurrencyForName(curve.getName());
    for (Currency currency : currencies) {
      updatedProvider = updatedProvider.withDiscountFactor(currency, curve);
    }

    List<IborIndex> iborIndices = multicurve.getIborIndexForName(curve.getName());

    for (IborIndex index : iborIndices) {
      updatedProvider = updatedProvider.withForward(index, curve);
    }

    List<IndexON> onIndices = multicurve.getOvernightIndexForName(curve.getName());

    for (IndexON index : onIndices) {
      updatedProvider = updatedProvider.withForward(index, curve);
    }

    return updatedProvider;
  }

  /**
   * Returns a list of lists that each contain one element from each of the input lists. There is one list in
   * the output for every combination of items in the inputs. The items in the output lists have the same order as
   * the input lists.
   * <p>
   * If the input consists of {@code n} lists with sizes {@code s1, s2, ..., sn}, the output list will have size
   * {@code (s1 x s2 x ... x sn)} and each of its lists will have size {@code n}.
   * <p>
   * For example:
   * <pre>
   *   cartesianProduct([1, 2, 3], [a, b]) = [[1, a], [1, b], [2, a], [2, b], [3, a], [3, b]]
   * </pre>
   * <p>
   * If any of the lists are empty, the result is an empty list.
   * <p>
   * If there are no input lists, the result is [[]] which might be surprising but is mathematically correct.
   * <p>
   * Strictly speaking this isn't a cartesian product as that is a concept defined for sets, not lists. But
   * it's a close enough concept to justify using the name.
   * <p>
   * Guava has a package-private method {@code Lists.cartesianProduct()}. If that is ever made public this
   * can be deleted.
   *
   * @param inputs some lists
   * @return a list of lists containing the cartesian product of the input lists
   */
  public static <T> List<List<T>> cartesianProduct(List<List<T>> inputs) {
    return cartesianProduct(ImmutableList.<T>of(), inputs);
  }

  /**
   * Returns a list of lists that each contain one element from each of the input lists. There is one list in
   * the output for every combination of items in the inputs. The items in the output lists have the same order as
   * the input lists.
   * <p>
   * If the input consists of {@code n} lists with sizes {@code s1, s2, ..., sn}, the output list will have size
   * {@code (s1 x s2 x ... x sn)} and each of its lists will have size {@code n}.
   * <p>
   * For example:
   * <pre>
   *   cartesianProduct([1, 2, 3], [a, b]) = [[1, a], [1, b], [2, a], [2, b], [3, a], [3, b]]
   * </pre>
   * <p>
   * If any of the lists are empty, the result is an empty list.
   * <p>
   * If there are no input lists, the result is [[]] which might be surprising but is mathematically correct.
   * <p>
   * Strictly speaking this isn't a cartesian product as that is a concept defined for sets, not lists. But
   * it's a close enough concept to justify using the name.
   * <p>
   * Guava has a package-private method {@code Lists.cartesianProduct()}. If that is ever made public this
   * can be deleted.
   *
   * @param inputs some lists
   * @return a list of lists containing the cartesian product of the input lists
   */
  @SafeVarargs
  public static <T> List<List<T>> cartesianProduct(List<T>... inputs) {
    return cartesianProduct(ImmutableList.<T>of(), ImmutableList.copyOf(inputs));
  }

  /**
   * Recursively builds up the cartesian product. Each invocation of this method inserts the elements from
   * one input list. It then recurses, passing in the output so far and the remaining input lists.
   *
   * @param output the output list, possibly incomplete
   * @param inputs the remaining input lists
   * @return the complete output lists
   */
  private static <T> List<List<T>> cartesianProduct(List<T> output, List<List<T>> inputs) {
    if (inputs.isEmpty()) {
      return ImmutableList.of(output);
    }
    List<T> firstList = inputs.get(0);
    ImmutableList.Builder<List<T>> builder = ImmutableList.builder();
    List<List<T>> remainingInputs = inputs.subList(1, inputs.size());

    for (T item : firstList) {
      builder.addAll(cartesianProduct(ImmutableList.<T>builder().addAll(output).add(item).build(), remainingInputs));
    }
    return builder.build();
  }
}
