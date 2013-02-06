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
            histogram_data = function (data) {
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
                output.histogram_data = count;
                output.pdf_data = pdf_data(stripped);
                output.bar = bar;
                return output;
            };
            normpdf = function (x, mu, sigma, constant) {
                var diff = x-mu;
                return (Math.exp(-( (diff*diff) / (2*(sigma*sigma)) ))) / (sigma*constant);
            };
            pdf_data = function (stripped) {
                var output = {}, 
                    count = [], 
                    diff = 0, 
                    sigma, 
                    constant = Math.sqrt(2*Math.PI), 
                    mu = stripped.reduce(function(a,b){return a+b;})/stripped.length;
                $.each(stripped, function(index, value){
                    diff +=  (value-mu)*(value-mu);
                });
                sigma = Math.sqrt(diff/(stripped.length-1));
                stripped.sort();
                $.each(stripped, function(index, value){
                    count.push([value, (normpdf(value,mu,sigma, constant))]);
                });
                output.data = count;
                return count;
            };
            gadget.dataman = new og.analytics
                .Cell({source: config.source, row: config.row, col: config.col, format: 'EXPANDED'}, 'histogram')
                .on('data', function (value) {
                    var data = typeof value.v !== 'undefined' ? value.v : value;
                    if (!histogram && data && (typeof data === 'object')) histogram = new og.common.gadgets
                        .HistogramPlot($.extend(true, {}, config,  histogram_data(data)));
                })
                .on('fatal', function (message) {$selector.html(message);});
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});