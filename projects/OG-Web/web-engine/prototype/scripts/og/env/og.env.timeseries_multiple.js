/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.env.timeseries_multiple',
    dependencies: [],
    obj: function () {
        return function (selector) {
            new og.common.gadgets.Timeseries({
                rest_options: {id: 'DbHts~1007'},
                selector: selector,
                datapoints: true
            });
        };
    }
});
