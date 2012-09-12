/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.curve',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var prefix = 'og_curve_gadget_', counter = 1;
        return function (config) {
            var source = {
                type: 'portfolio',
                depgraph: true,
                viewdefinition: 'DbCfg~1475588',
                live: true,
                provider: 'Live market data (Bloomberg, Activ, TullettPrebon, ICAP)',
                view_id: 'DbCfg~1475588',
                row: 5,
                col: 5
            };
            var gadget = this, curve, alive = prefix + counter++;
            gadget.alive = function () {return !!$('.' + alive).length;};
            gadget.load = function () {
                (new og.analytics.Cell({source: source, col: 3, row: 1101})).on('data', function (data) {
                    var d = (data && data.v) && [{curve: data.v}];
                    if (!curve && d) curve = $(config.selector)
                        .addClass(alive)
                        .css({position: 'absolute', top: 0, left: 0, right: 0, bottom: 0})
                        .ogcurve(d);
                    else if (curve) curve.update(d);
                });
            };
            gadget.resize = gadget.load;
            gadget.load();
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});