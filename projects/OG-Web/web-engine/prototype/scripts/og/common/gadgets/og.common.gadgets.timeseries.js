/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Timeseries',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        return function (config) {
            var gadget = this, timeseries, alive = og.common.id('gadget_timeseries'), $selector = $(config.selector);
            $(config.selector).addClass(alive).css({position: 'absolute', top: 0, left: 0, right: 0, bottom: 0});
            gadget.alive = function () {
                var live = !!$('.' + alive).length;
                if (!live && timeseries) gadget.dataman.kill();
                return live;
            };
            gadget.resize = function () {try {timeseries.resize();} catch (error) {}};
            gadget.dataman = new og.analytics
                .Cell({source: config.source, row: config.row, col: config.col, format: 'EXPANDED'}, 'timeseries')
                .on('data', function (value) {
                    var data = typeof value.v !== 'undefined' ? value.v : value;
                    if (!timeseries && data && (typeof data === 'object')) timeseries = new og.common.gadgets
                        .TimeseriesPlot($.extend(true, {}, config, {data: [data]}));
                })
                .on('fatal', function (message) {$selector.html(message)});
            if (!config.child) og.common.gadgets.manager.register(gadget);
        }
    }
});