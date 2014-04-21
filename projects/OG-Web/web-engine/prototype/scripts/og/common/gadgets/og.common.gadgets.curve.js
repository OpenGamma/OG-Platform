/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Curve',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var module = this;
        return function (config) {
            var gadget = this, curve;
            gadget.dataman = new og.analytics
                .Cells({source: config.source, single: {row: config.row, col: config.col}, format: 'EXPANDED'}, 'curve')
                .on('data', function (data) {
                    data = data.v || data;
                    if (!$.isArray(data)) {
                        return og.dev.warn(module.name + ': data should be an Array', data);
                    }
                    gadget.data = [{curve: data}];
                    if (!curve && gadget.data) {
                        curve = new og.common.gadgets.CurvePlot({selector: config.selector, data: gadget.data});
                    } else {
                        curve.update(gadget.data);
                    }
                });
            gadget.alive = function () {
                var live = curve && curve.alive;
                if (!live) {
                    gadget.dataman.kill();
                }
                return live;
            };
            gadget.resize = function () {
                try {
                    curve.resize();
                } catch (error) {/* do nothing */}
            };
            if (!config.child) {
                og.common.gadgets.manager.register(gadget);
            }
        };
    }
});