/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.env.data',
    dependencies: [],
    obj: function () {
        return function (selector) {
            new og.common.gadgets.Data({
                resource: 'timeseries', rest_options: {id: 'DbHts~1020'},
                type: 'TIME_SERIES', selector: selector, menu: false, child: false
            });
        };
    }
});
