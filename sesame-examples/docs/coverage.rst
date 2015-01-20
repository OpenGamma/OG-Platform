================
Examples covered
================

The best place to start exploring the functionality covered in this project is in the example app tests.

RemoteBondTest
==============

This integration test validates Present Value against a remote server. The view definition and bond inputs are defined in ``BondViewUtils``.

RemoteFraTest
=============

This integration test validates Present Value against a remote server. The view definition and FRA inputs are defined in ``FraViewUtils``

RemoteSwapTest
==============

This integration test validates Present Value, Bucketed PV01 and cash flows against a remote server. The view definition and swap inputs are defined in ``SwapViewUtils``

Examples include swaps with fixed, float, spread, fixing, compounding, stubs, fees, single leg, zero coupon, cross currency and notional exchange.

RemoteCreditTest and LibraryCreditTest
======================================

These integration tests validates Present Value and CS01 against a remote server and as a library. The view definition and credit inputs are defined in ``CreditViewUtils``

Examples include legacy and standard credit default swaps.

RemoteComponentSwapTest
=======================

This integration test runs a single cycle of the engine locally, pulling the configuration components from the remote server.

RemoteViewConfigTest
====================

This integration test creates, persists and accesses a view config

LibraryCurveBundleProviderTest
==============================

This integration test tests the creation of a MulticurveBundle for a given valuation time, curve configuration, currency matrix and snapshot.