/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.curve',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var prefix = 'og_curve_gadget_', counter = 1, module = this;
        return function (config) {
            var source = {
                type: 'portfolio',
                depgraph: true,
                viewdefinition: 'DbCfg~1475588',
                live: true,
                providers: [{'marketDataType': 'live', 'source': 'Bloomberg'}],
                view_id: 'DbCfg~1475588',
                row: 5,
                col: 5
            };
            var gadget = this, curve, alive = prefix + counter++, d;
            gadget.alive = function () {return !!$('.' + alive).length;};
            gadget.load = function () {
                (new og.analytics.Cell({source: source, col: 3, row: 985})).on('data', function (data) {
                    if (data.t !== 'CURVE') return og.dev.warn(module.name + ': data.v should be CURVE');
                    d = $.isArray(data.v) && [{curve: data.v}];
                    curve ? curve.update(d) : gadget.resize();
                });
            };
            gadget.resize = function () {
                curve = $(config.selector)
                    .addClass(alive)
                    .css({position: 'absolute', top: 0, left: 0, right: 0, bottom: 0})
                    .ogcurve(d);
            };
            gadget.load();
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});