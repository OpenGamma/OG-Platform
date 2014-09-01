How to use pre-built curve data
===============================

When integrating OpenGamma within an existing infrastructure it is possible that rather than configuring OpenGamma to
build and calibrate curves from raw market quotes you instead have access to pre-built curves and wish to use those.

This can be accomplished by using an appropriate OpenGamma curve configuration that consumes sampled curve points
together with an interpolation method that matches your desired methodology to reproduce the pre-built curve.

Supported curve pillar types
----------------------------

Pre calculated curve pillar rates are accepted for the following types:

 #. Continuously compounded rate (via ``ContinuouslyCompoundedRateNode``)
 #. Periodically compounded (includes annually compounded) rate (via ``PeriodicallyCompoundedRateNode``)
 #. Discount factor (via ``DiscountFactorNode``)

Note that types cannot be mixed within a curve, a curve must be made up of all the same curve node type.

Configuration example
---------------------

This describes how to configure a curve within OpenGamma to accept sampled curve points in order to replicate an
existing curve obtained from another source, e.g. an internal curve system. It does not describe how to configure the
OpenGamma system to use this curve for pricing of given instruments (i.e. currency/issuer based exposure functions,
OIS discounting etc), the
standard OpenGamma curve configuration will work (see the curve documentation for full details) as expected.

In this example we assume we have a pre-built and calibrated curve from another system, from which we can obtain a set of
sampled curve pillars. We will configure an OpenGamma curve with these pre-configured points.

.. code:: xml

  <!-- A curve that is assembled from a set of discount factors sampled from a pre built curve -->
  <bean type="com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition">
   <name>Curve from pre built discount factors</name>
   <nodes>
    <item type="com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode.DiscountFactorNode">
     <curveNodeIdMapperName>DiscountFactor Mapper</curveNodeIdMapperName>
     <tenor>P1M</startTenor>
    </item>
    <item type="com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode.DiscountFactorNode">
     <curveNodeIdMapperName>DiscountFactor Mapper</curveNodeIdMapperName>
     <tenor>P6M</startTenor>
    </item>
    <item type="com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode.DiscountFactorNode">
     <curveNodeIdMapperName>DiscountFactor Mapper</curveNodeIdMapperName>
     <tenor>P1Y</startTenor>
    </item>
    <item type="com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode.DiscountFactorNode">
     <curveNodeIdMapperName>DiscountFactor Mapper</curveNodeIdMapperName>
     <tenor>P2Y</startTenor>
    </item>
   </nodes>
   <interpolatorName>Linear</interpolatorName>
   <rightExtrapolatorName>FlatExtrapolator</rightExtrapolatorName>
   <leftExtrapolatorName>FlatExtrapolator</leftExtrapolatorName>
  </bean>

Combining this with a CurveNodeIdMapper:

.. code:: xml

  <!-- the mapping to obtain the market data ticker for curve pillars -->
  <fudgeEnvelope>
    <fudgeField0 ordinal="0" type="string">com.opengamma.financial.analytics.curve.CurveNodeIdMapper</fudgeField0>
    <name type="string">DiscountFactor Mapper</name>
    <discountFactorNodeIds type="message">
      <P1M type="message">
        <fudgeField0 ordinal="0" type="string">com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider</fudgeField0>
        <instrument type="string">CURVE_POINT~1M</instrument>
        <dataField type="string">Market_Value</dataField>
        <typeField type="string">OUTRIGHT</typeField>
      </P1M>
      <P6M type="message">
        <fudgeField0 ordinal="0" type="string">com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider</fudgeField0>
        <instrument type="string">CURVE_POINT~6M</instrument>
        <dataField type="string">Market_Value</dataField>
        <typeField type="string">OUTRIGHT</typeField>
      </P6M>
      <P1Y type="message">
        <fudgeField0 ordinal="0" type="string">com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider</fudgeField0>
        <instrument type="string">CURVE_POINT~1Y</instrument>
        <dataField type="string">Market_Value</dataField>
        <typeField type="string">OUTRIGHT</typeField>
      </P1Y>
      <P2Y type="message">
        <fudgeField0 ordinal="0" type="string">com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider</fudgeField0>
        <instrument type="string">CURVE_POINT~2Y</instrument>
        <dataField type="string">Market_Value</dataField>
        <typeField type="string">OUTRIGHT</typeField>
      </P2Y>
    </discountFactorNodeIds>
  </fudgeEnvelope>

The above configuration objects can be created programmatically via the Java API.

This will create a curve that will consume curve pillar points that have already been converted into discount factors
and will thus skip curve calibration. The points will be used directly and the provided interpolation method will be used when
needed to extract values between the provided pillars.

The curve node id mapper configuration allows the the curve pillar point values to be consumed like any other piece
of market data. The pillar market data may be provided via any of the normal market data mechanisms, e,g, snapshot,
time series or live market data. Any naming scheme can be adopted to match an existing convention.

Once configured this curve appears the same to the pricing logic as any other curve, and thus can be used to price
any supported instrument.

Model daycount
--------------

To match rates from other systems OpenGamma needs to be configured with the same model daycount for reading time intervals
from the curve. This can be set via the ``AnalyticsEnvironment`` object. It can be configured in the server config file:

.. code:: ini

  [analyticsEnvironment]
  factory = com.opengamma.component.analytics.AnalyticsEnvironmentComponentFactory
  modelDayCount = Actual/365




