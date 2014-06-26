
================
Yield curve data
================

Interest rate data for building ISDA yield curves is imported into OpenGamma via snapshots.
A simple domain model is used to store this data, consisting of two classes:

* ``com.opengamma.financial.analytics.isda.credit.YieldCurveData``
* ``com.opengamma.financial.analytics.isda.credit.YieldCurveDataSnapshot``

``YieldCurveData`` holds the values required to calibrate the yield curve, namely market data
and a set of conventions. ISDA yield curves support two instrument types - cash deposits
and swaps. Two maps, both indexed by tenor, are defined on the ``YieldCurveData`` class for
holding market data values. The curve's term structure is implied from the tenors defined across
the two maps.

Other fields on ``YieldCurveData`` define conventions required for bootstrapping.

``YieldCurveDataSnapshot`` is the snapshot object used to store the full set of ``YieldCurveData``
instances. When pricing credit instruments, yield curves are typically resolved at the currency
level. Hence they are keyed by currency in the snapshot.

For further information on yield curve bootstrapping in the ISDA model, see the
`OpenGamma quantitative research document`_ on the subject.

.. _OpenGamma quantitative research document: http://developers.opengamma.com/quantitative-research/Pricing-and-Risk-Management-of-Credit-Default-Swaps-OpenGamma.pdf
