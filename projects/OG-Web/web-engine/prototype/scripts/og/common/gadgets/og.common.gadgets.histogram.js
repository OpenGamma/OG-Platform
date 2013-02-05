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
            prepare_data = function (data) {
                var input = data.timeseries.data, stripped, max, min, range, buckets = 50, bar, count = [],
                maxcount = 0, output = {};
                stripped = input.reduce(function(a,b){return a.concat(b[1]);},[]);
                max = Math.max.apply(Math, stripped);
                min = Math.min.apply(Math, stripped);
                range = max - min;
                bar = range/buckets;
                for (var i = 0; i < buckets; i++) { 
                    var label = min + (bar*i);
                    count[i] = [label , 0];}
                $.each(stripped, function(index, value){
                    if (value == max) maxcount++; 
                    else{                        
                        var p = Math.floor((value-min)/bar);
                        count[p][1] = count[p][1]  + 1;
                    }
                });
                count[buckets-1][1] = count[buckets-1][1] + maxcount;
                output.data = count;
                output.bar = bar;
                return output;
            };
            gadget.dataman = new og.analytics
                .Cell({source: config.source, row: config.row, col: config.col, format: 'EXPANDED'}, 'histogram')
                .on('data', function (value) {
                    var data = typeof value.v !== 'undefined' ? value.v : value;
                    if (!histogram && data && (typeof data === 'object')) histogram = new og.common.gadgets
                        .HistogramPlot($.extend(true, {}, config,  prepare_data(data)));
                })
                .on('fatal', function (message) {$selector.html(message);});
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});