=============================================
CurveNodeIdMappers & CurveInstrumentProviders
=============================================

A CurveInstrumentProvider provides the market quote corresponding to a curve node. It will be instrument
and data provider specific. The simplest provider is a mapping to a given market data ticker (e.g. Bloomberg ticker).

A CurveNodeIdMapper maps from an instrument type and tenor to a given instrument provider.

A CurveNode links to a CurveNodeIdMapper which will in turn link to a CurveInstrumentProvider. Thus the relevant
market quotes can be obtained.

CurveInstrumentProvider examples.
---------------------------------

StaticCurveInstrumentProvider
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A simple mapping to a fixed ticker. For example a given deposit, FRA, future or swap ticker.

CurveNodeIdMapper.
------------------

The mapper will contain a mapping between the instrument type and maturity and ``CurveInstrumentProvider``. For example
there may be mappings from deposit, FRA, future and swap types used in conjunctions with the maturity to an instrument
provider.

Fudge XML example
-----------------

The following is a snippet of 2 deposit nodes on 1 and 4 day points to be used within a USD curve.

.. code-block:: xml

    <fudgeEnvelope>
      <fudgeField0 ordinal="0" type="string">com.opengamma.financial.analytics.curve.CurveNodeIdMapper</fudgeField0>
      <name type="string">USD Deposit Bloomberg Mapper</name>
      <cashIds type="message">
        <P1D type="message">
          <fudgeField0 ordinal="0" type="string">com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider</fudgeField0>
          <instrument type="string">BLOOMBERG_TICKER~USDR1T Curncy</instrument>
          <dataField type="string">Market_Value</dataField>
          <typeField type="string">OUTRIGHT</typeField>
        </P1D>
        <P4D type="message">
          <fudgeField0 ordinal="0" type="string">com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider</fudgeField0>
          <instrument type="string">BLOOMBERG_TICKER~USDR2T Curncy</instrument>
          <dataField type="string">Market_Value</dataField>
          <typeField type="string">OUTRIGHT</typeField>
        </P4D>
      </cashIds>
    </fudgeEnvelope>
