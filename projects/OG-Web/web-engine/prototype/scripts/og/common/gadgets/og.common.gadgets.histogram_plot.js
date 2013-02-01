/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.HistogramPlot',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var module = this, loading_template;
        return function (config) {
            var gadget = this, selector = config.selector, $plot, options, disabled_options, data = [];
            gadget.resize = function () {

            };
            for (var i = 0; i < 10; i += 0.5){ 
                data.push([i, Math.tan(i)]); 
            }
            options = { 
                series: { 
                    lines: { show: true }, 
                    points: { show: true } 
                    } 
                };
            $plot = $.plot($(selector), data, options);
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});