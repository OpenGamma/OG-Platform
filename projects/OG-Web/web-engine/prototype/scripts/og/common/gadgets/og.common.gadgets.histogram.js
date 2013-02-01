/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Histogram',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        return function (config) {
            var gadget = this, histogram, alive = og.common.id('gadget_histogram'), $selector = $(config.selector);
            $(config.selector).addClass(alive).css({position: 'absolute', top: 0, left: 0, right: 0, bottom: 0});
            gadget.alive = function () {
                var live = !!$('.' + alive).length;
                if (!live && histogram) gadget.dataman.kill();
                return live;
            };
            gadget.resize = function () {try {histogram.resize();} catch (error) {}};
            gadget.dataman = new og.analytics
                .Cell({source: config.source, row: config.row, col: config.col, format: 'EXPANDED'}, 'histogram')
                .on('data', function (value) {
                    var data = typeof value.v !== 'undefined' ? value.v : value;
                    if (!histogram && data && (typeof data === 'object')) histogram = new og.common.gadgets
                        .HistogramPlot($.extend(true, {}, config, {data: [data]}));
                })
                .on('fatal', function (message) {$selector.html(message);});
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});