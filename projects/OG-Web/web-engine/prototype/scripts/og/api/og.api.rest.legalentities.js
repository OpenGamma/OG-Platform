/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.legalentities',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest;
        return { // all requests that begin with /organizations
            root: 'organizations',
            get: api.default_get.partial(['node', 'name', 'obligor_red_code', 'obligor_ticker'],
                ['organizationId', 'shortName', 'obligorREDCode', 'obligorTicker']),
            put: common.not_implemented_put,
            del: common.not_implemented_del
        };
    }
});