/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Histogram',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        return function (config) {
            var gadget = this, histogram, alive = og.common.id('gadget_histogram'), $selector = $(config.selector),
            output = {}, stripped;
            $(config.selector).addClass(alive).css({position: 'absolute', top: 0, left: 0, right: 0, bottom: 0});
            gadget.alive = function () {
                var live = !!$('.' + alive).length;
                if (!live && histogram) gadget.dataman.kill();
                return live;
            };
            gadget.resize = function () {
                try {histogram.resize();} catch (error) {}
            };
            prepare_data = function (data) {
                console.log(data);
                stripped = data.timeseries.data.reduce(function(a,b){return a.concat(b[1]);},[]);
            };
            histogram_data = function () {
                var max, min, range, buckets = 50, interval, count = [], maxcount = 0, output = {};
                max = Math.max.apply(Math, stripped);
                min = Math.min.apply(Math, stripped);
                range = max - min;
                interval = range/buckets;
                for (var i = 0; i < buckets; i++) {
                    var label = min + (interval*i);
                    count[i] = [label , 0];}
                $.each(stripped, function(index, value){
                    if (value == max) maxcount++;
                    else{
                        var p = Math.floor((value-min)/interval);
                        count[p][1] = count[p][1]  + 1;
                    }
                });
                count[buckets-1][1] = count[buckets-1][1] + maxcount;
                return {histogram_data: count, interval: interval};
            };
            /*normpdf = function (x, mu, sigma, constant) {
                var diff = x-mu;
                return (Math.exp(-( (diff*diff) / (2*(sigma*sigma)) ))) / (sigma*constant);
            };
            normpdf_data = function () {
                var norm = [], diff = 0, sigma, constant = Math.sqrt(2*Math.PI),
                    mu = stripped.reduce(function(a,b){return a+b;})/stripped.length;
                $.each(stripped, function(index, value){
                    diff +=  (value-mu)*(value-mu);
                });
                sigma = Math.sqrt(diff/(stripped.length-1));
                //stripped.sort();
                $.each(stripped, function(index, value){
                    norm.push([value, (normpdf(value, mu, sigma, constant))]);
                });
                return {norm_pdf_data:norm};
            };*/
            gadget.dataman = new og.analytics.Cell({
                source: config.source, row: config.row, col: config.col, format: 'EXPANDED'}, 'histogram')
                .on('data', function (value) {
                    var input, data = typeof value.v !== 'undefined' ? value.v : value;
                    if (!histogram && data && (typeof data === 'object')){
                        prepare_data(data);
                        input = $.extend(true, {}, config,  histogram_data()/*, normpdf_data()*/);
                        histogram = new og.common.gadgets.HistogramPlot(input);
                    }
                })
                .on('fatal', function (message) {$selector.html(message);});
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});