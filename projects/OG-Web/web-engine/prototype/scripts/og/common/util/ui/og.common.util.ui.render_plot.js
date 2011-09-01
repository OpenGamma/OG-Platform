/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * Renders a Canvas plot using Flot
 * @see http://code.google.com/p/flot/
 * @param {String} selector
 * @param {Array} data
 */
$.register_module({
    name: 'og.common.util.ui.render_plot',
    dependencies: [],
    obj: function () {
        return function (selector, data) { // Current default plot settings
            var colors_arr = ['#487287', '#ff9c00'],
                d = [{shadowSize: '1', data: data}],
                $p1, p1_options, p1_selector = selector + ' .og-js-p1',
                $p2, p2_options, p2_selector = selector + ' .og-js-p2',
                tenor = selector + ' .og-tenor',
                date_max, presets = {}, initial_preset;
            $(selector).html(
                '<div class="og-js-p1" style="height: 250px; width: 800px; margin: 0 0 0 -20px"></div>\
                 <div class="og-js-p2" style="height: 100px; width: 800px; margin: -43px 0 0 -20px"></div>\
                 <div class="og-tenor" style="margin: 30px 0 0 15px; position: absolute; top: 0; left: 0"></div>'
            ).css({position: 'relative'});
            (function () { // set up presets
                var max_obj, new_max_date_obj, _1m, _3m, _6m, _1y, _2y, _3y, counter = 0;
                date_max = data[data.length-1][0];
                new_max_date_obj = function () {return new Date(date_max)};
                presets._1d = date_max - 86400 * 1000; // in milliseconds
                presets._1w = date_max - 7 * 86400 * 1000;
                _1m = new_max_date_obj(), _1m.setMonth(_1m.getMonth() - 1), presets._1m = +_1m;
                _3m = new_max_date_obj(), _3m.setMonth(_3m.getMonth() - 3), presets._3m = +_3m;
                _6m = new_max_date_obj(), _6m.setMonth(_6m.getMonth() - 6), presets._6m = +_6m;
                _1y = new_max_date_obj(), _1y.setYear(_1y.getFullYear() - 1), presets._1y = +_1y;
                _2y = new_max_date_obj(), _2y.setYear(_2y.getFullYear() - 2), presets._2y = +_2y;
                _3y = new_max_date_obj(), _3y.setYear(_3y.getFullYear() - 3), presets._3y = +_3y;
                presets._all = data[0][0];
                $(tenor).html(['all', '3y', '2y', '1y', '6m', '3m', '1m', '1w', '1d'].reduce(function (pre, cur) {
                    var is_valid = presets['_' + cur] >= presets._all,
                        classes = is_valid ? 'OG-link og-js-' + cur : 'OG-link-disabled';
                    if (is_valid) ++counter;
                    if (counter === 3) classes += ' OG-link-active', initial_preset = presets['_' + cur];
                    pre.unshift('<span class="'+ classes +'" style="margin-right: 5px; font-size: 10px;">'
                        + cur +'</span>');
                    return pre;
                }, []).join('')).click(function (e) {
                    e.stopPropagation();
                    var from = presets['_' + e.target.textContent], $elm = $(e.target);
                    if ($elm.hasClass('OG-link-disabled') || !$elm.is('span')) return;
                    $elm.siblings().removeClass('OG-link-active'), $elm.addClass('OG-link-active');
                    $p2.setSelection({xaxis: {from: from , to: date_max}}, true);
                    $p1 = $.plot($(p1_selector), d, $.extend(true, {}, p1_options, {xaxis: {min: from}}));
                });
            }());
            p1_options = {
                colors: colors_arr,
                lines: {lineWidth: 1},
                xaxis: {
                    ticks: 6, mode: 'time', panRange: [data[0][0], data[data.length-1][0]],
                    min: initial_preset, max: date_max,
                    tickLength: 0, labelHeight: 26
                },
                yaxis: {ticks: 5, position: 'right', panRange: false, tickLength: 10, labelWidth: 53},
                grid: {borderWidth: 1, color: '#ccc', borderColor: '#e9eaeb', labelMargin: 3, minBorderMargin: 30},
                selection: {mode: null, color: '#d7e7f2'},
                pan: {interactive: true, cursor: "move", frameRate: 20}
            },
            p2_options = {
                colors: colors_arr,
                lines: {lineWidth: 1, fill: 1, fillColor: '#f8fbfd'},
                xaxis: {ticks: 6, mode: 'time', tickLength: 10, labelHeight: 17},
                yaxis: {show: false, ticks: 1, position: 'right', tickLength: 10, labelWidth: 53, reserveSpace: true},
                grid: {borderWidth: 1, color: '#ccc', borderColor: '#e9eaeb', labelMargin: 3, minBorderMargin: 30},
                selection: {mode: 'x', color: '#d7e7f2'}
            },
            $p1 = $.plot($(p1_selector), d, p1_options),
            $p2 = $.plot($(p2_selector), d, p2_options);
            $p2.setSelection({xaxis: {from: initial_preset, to: date_max}}, true); // initial selection setup
            $(p1_selector).bind('plotselected', function (event, ranges) { // connect the two plots
                var options = $.extend(true, {}, p1_options, {xaxis: {min: ranges.xaxis.from, max: ranges.xaxis.to}});
                $p1 = $.plot($(p1_selector), d, options);
                $p2.setSelection(ranges, true);
            });
            $(p1_selector).bind('plotpan', function (e, obj) { // panning
                var xaxes = obj.getXAxes()[0];
                $p2.setSelection({xaxis: {from: xaxes.min, to: xaxes.max}}, true);
            });
            $(p2_selector).bind('plotselected', function (event, ranges) {$p1.setSelection(ranges);});
            $(p1_selector).bind('plothover', function (event, pos, item) {
                //console.log(event, pos, item);
            });
        }

    }
});