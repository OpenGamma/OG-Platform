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
            var gadget = this, selector = config.selector, $plot, options;
            gadget.resize = function () {
                load_plots();
            };
            load_plots = function () {
                options = { bars: { show: true, barWidth: config.bar}};
                $plot = $.plot($(selector), [config.data], options);
            };
            if (!config.child) og.common.gadgets.manager.register(gadget);
            load_plots();
            
        };
    }
});