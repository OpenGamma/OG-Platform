/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Histogram',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        return function (config) {
            var gadget = this, $selector = $(config.selector), histogram, stripped, buckets, max, min, range;
                alive = og.common.id('gadget_histogram');
            $(config.selector).addClass(alive).css({
                position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: '#fff'
            });
            gadget.alive = function () {
                var live = !!$('.' + alive).length;
                if (!live && histogram) gadget.dataman.kill();
                return live;
            };
            gadget.resize = function () {try {histogram.resize();} catch (error) {}};
            var prepare_data = function (data) {
                stripped = data.timeseries.data.reduce(function (a, b){return a.concat(b[1]);}, []);
            };
            var histogram_data = function (data) {
                var length = stripped.length, max_buckets = 50, min_buckets = 10,
                    bucket_calc = Math.ceil(Math.sqrt(length));
                buckets = bucket_calc < max_buckets ? bucket_calc : max_buckets;
                buckets = buckets < min_buckets ? min_buckets : buckets;
                max = Math.max.apply(Math, stripped);
                min = Math.min.apply(Math, stripped);
                range = max - min;
                return bucket_data(buckets);
            };
            var bucket_data = function (buckets) {
                var interval = range / buckets, count = [], label, i = 0, maxcount = 0;
                // 2D count array, 1D is the label made up of the lower bound of the bucket
                for (; i < buckets; i++) {
                    label = (min + (interval * i));
                    count[i] = [label, 0];
                }
                // 2D count array, 2D is the number of occurances in [bound, next bound)
                $.each(stripped, function (index, value) {
                    if (value == max) return maxcount++;
                    var p = Math.floor((value - min) / interval);
                    count[p][1] = count[p][1] + 1;
                });
                // all values which match the max in the dataset are ignored above and added to the final bucket
                count[buckets - 1][1] = count[buckets - 1][1] + maxcount;
                /* test to ensure no data is lost - sum of all items in buckets must match dataset length */
                /*
                if (count.map(function(v) { return v[1]; }).reduce(function(a,b) { return a + b; }) != length)
                    og.dev.warn('bucket totals to not match dataset length');
                */
                return {histogram_data: count, interval: interval};
            };
            var bucket_range = function () {
                var step = buckets/2.5, double_step = 2 * step, round = Math.round, triple_step = 3 * step;
                return { buckets: { oleft: round(buckets - double_step),ileft: round(buckets - step), mid: buckets,
                            iright: round(buckets + step), oright: round(buckets + double_step),
                            ooright: round(buckets + triple_step)}};
            };
            var normpdf = function (x, mu, sigma, constant) {
                var diff = x-mu;
                return (Math.exp(-( (diff*diff) / (2*(sigma*sigma)) ))) / (sigma*constant);
            };
            var normpdf_data = function () {
                var norm = [], diff = 0, sigma, constant = Math.sqrt(2*Math.PI),
                    mu = stripped.reduce(function(a,b){return a+b;})/stripped.length;
                $.each(stripped, function(index, value){
                    diff +=  (value-mu)*(value-mu);
                });
                sigma = Math.sqrt(diff/(stripped.length-1));
                $.each(stripped, function(index, value){
                    norm.push([value, (normpdf(value, mu, sigma, constant))]);
                });
                return {norm_pdf_data:norm};
            };
            gadget.dataman = new og.analytics.Cell({
                source: config.source, row: config.row, col: config.col, format: 'EXPANDED'}, 'histogram')
                .on('data', function (value) {
                    var input, data = typeof value.v !== 'undefined' ? value.v : value;
                    if (!histogram && data && (typeof data === 'object')) {
                        prepare_data(data);
                        input = $.extend(true, {}, config, histogram_data(data), normpdf_data(),
                            {callback: bucket_data}, bucket_range());
                        histogram = new og.common.gadgets.HistogramPlot(input);
                    }
                })
                .on('fatal', function (message) {$selector.html(message);});
            if (!config.child) og.common.gadgets.manager.register(gadget);
        };
    }
});