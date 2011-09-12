/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * Renders a Canvas plot using Flot
 * @see http://code.google.com/p/flot/
 */
$.register_module({
    name: 'og.common.util.ui.render_plot',
    dependencies: ['og.api.rest'],
    obj: function () {
        var api = og.api;
        return function (config) {
            var selector = config.selector, data = config.data, identifier = config.identifier,
                meta = {}, // object that stores the structure and data of the different plots
                state = {}, // keeps a record of the current active data sets in the plot along with zoom and pan data
                colors_arr = ['#42669a', '#ff9c00', '#00e13a', '#313b44'], // line colours for the different data sets
                $p1, p1_options, p1_selector = selector + ' .og-js-p1',
                $p2, p2_options, p2_selector = selector + ' .og-js-p2',
                tenor = selector + ' .og-tenor',
                $legend, hover_pos = null,
                date_max, initial_preset,
                $plot_header = $('.OG-timeseries .og-plotHeader'),
                load_plots, empty_plots, update_legend, get_legend, panning;
            $(selector).html(
                '<div class="og-js-p1" style="height: 250px; width: 800px; margin: 0 0 0 -20px"></div>\
                 <div class="og-js-p2" style="height: 100px; width: 800px; margin: -43px 0 0 -20px"></div>\
                 <div class="og-tenor" style="margin: 30px 0 0 15px; position: absolute; top: -27px; right: -166px;\
                     background: #fff"></div>'
            ).css({position: 'relative'});
            get_legend = function () {return $(selector + ' .legend');}; // the legend is often regenerated
            empty_plots = function () {
                var d = ['1', '2'], $p1, $p2, disabled_options;
                disabled_options = {
                    xaxis: {show: false, panRange: false},
                    yaxis: {show: false, panRange: false},
                    selection: {mode: null},
                    pan: {interactive: false}
                };
                $(tenor).css({visibility: 'hidden'});
                $p1 = $.plot($(p1_selector), d, $.extend(true, {}, p1_options, disabled_options));
                $p2 = $.plot($(p2_selector), d, $.extend(true, {}, p2_options, disabled_options));
            };
            load_plots = function (data_arr) {
                if (data_arr[0] === void 0) {empty_plots(); return}
                var d = data_arr, data = data_arr[0].data;
                (function () { // set up presets
                    var max_obj, new_max_date_obj, _1m, _3m, _6m, _1y, _2y, _3y, counter = 0, presets = {};
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
                        if (!state.zoom && counter === 3) { // we are picking the third largest valid preset by default
                            classes += ' OG-link-active', initial_preset = presets['_' + cur];
                            state.zoom = '_' + cur;
                        } else if (state.zoom === '_' + cur) { // or the one last set by the user
                            classes += ' OG-link-active', initial_preset = presets[state.zoom];
                        }
                        pre.unshift('<span class="'+ classes +'" style="margin-right: 5px; font-size: 10px;">'
                            + cur +'</span>');
                        return pre;
                    }, []).join('')).unbind().bind('click', function (e) {
                        e.stopPropagation();
                        var target = e.target, from = presets['_' + target.textContent], $elm = $(target);
                        if ($elm.hasClass('OG-link-disabled') || !$elm.is('span')) return;
                        state.zoom = '_' + target.textContent;
                        $elm.siblings().removeClass('OG-link-active'), $elm.addClass('OG-link-active');
                        state.from = from, state.to = date_max;
                        $p2.setSelection({xaxis: {from: from , to: date_max}}, true);
                        $p1 = $.plot($(p1_selector), d,
                                $.extend(true, {}, p1_options, {xaxis: {min: from, max: date_max}}));
                        $legend = get_legend();
                        $legend.css({visibility: 'hidden'});
                    });
                    $(tenor).css({visibility: 'visible'});
                }());
                p1_options = {
                    colors: colors_arr,
                    series: {shadowSize: 1, threshold: {below: 0, color: '#960505'}},
                    legend: {
                        show: true, labelBoxBorderColor: 'transparent', position: 'nw', margin: 1, backgroundColor: null                    },
                    crosshair: {mode: 'x', color: '#e5e5e5', lineWidth: '1'},
                    lines: {lineWidth: 1},
                    xaxis: {
                        ticks: 6, mode: 'time', panRange: [data[0][0], data[data.length-1][0]],
                        min: initial_preset, max: date_max,
                        tickLength: 0, labelHeight: 26,

                        color: '#fff', // base color, labels, ticks
                        tickColor: null // possibly different color of ticks, e.g. "rgba(0,0,0,0.15)"


                    },
                    yaxis: {
                        ticks: 5, position: 'right', panRange: false, tickLength: 10, labelWidth: 53, reserveSpace: true
                    },
                    grid: {borderWidth: 1, color: '#999', borderColor: '#e9eaeb', labelMargin: 3,
                        minBorderMargin: 30, hoverable: true},
                    selection: {mode: null, color: '#d7e7f2'},
                    pan: {interactive: true, cursor: "move", frameRate: 30}
                };
                p2_options = {
                    colors: colors_arr,
                    series: {shadowSize: 1, threshold: {below: 0, color: '#960505'}},
                    legend: {show: false},
                    lines: {lineWidth: 1, fill: 1, fillColor: '#f8fbfd'},
                    xaxis: {ticks: 6, mode: 'time', tickLength: 10, labelHeight: 17},
                    yaxis: {
                        show: false, ticks: 1, position: 'right', tickLength: 10, labelWidth: 53, reserveSpace: true
                    },
                    grid: {borderWidth: 1, color: '#999', borderColor: '#e9eaeb', labelMargin: 3, minBorderMargin: 30},
                    selection: {mode: 'x', color: '#d7e7f2'}
                };
                if (!(state.from && state.to)) {state.from = initial_preset, state.to = date_max;}
                // in xaxis, min/max sets the pan, from/to sets the selection
                p1_options = $.extend(true, {}, p1_options, {xaxis: {min: state.from, max: state.to}});
                p2_options = $.extend(true, {}, p2_options, {xaxis: {from: state.from, to: state.to}});
                $p1 = $.plot($(p1_selector), d, p1_options);
                $p2 = $.plot($(p2_selector), d, p2_options);
                $p2.setSelection({xaxis: {from: state.from, to: state.to}}, true);
                // connect the two plots
                $(p1_selector).unbind('plotselected').bind('plotselected', function (e, r) { // events, ranges
                    state.from = r.xaxis.from, state.to = r.xaxis.to;
                    var options = $.extend(true, {}, p1_options, {xaxis: {min: state.from, max: state.to}});
                    $p1 = $.plot($(p1_selector), d, options);
                    $p2.setSelection(r, true);
                    $(tenor + ' .OG-link').removeClass('OG-link-active');
                });
                $(p1_selector).unbind('plotpan').bind('plotpan', function (e, obj) { // panning
                    var mouseup = function () {
                        setTimeout(function () {panning = false;}, 0);
                        $(document).unbind('mouseup', mouseup);
                    },
                    xaxes = obj.getXAxes()[0];
                    panning = true;
                    $(document).bind('mouseup', mouseup);
                    $legend = get_legend(), $legend.css({visibility: 'hidden'});
                    state.from = xaxes.min, state.to = xaxes.max;
                    $p2.setSelection({xaxis: {from: state.from, to: state.to}}, true);
                });
                $(p2_selector).unbind('plotselected').bind('plotselected', function (e, r) {$p1.setSelection(r);});
                $(p1_selector).unbind('plothover').bind('plothover', function (e, pos) {
                    if (!panning) hover_pos = pos, setTimeout(update_legend, 50);
                });
                $legend = get_legend(), $legend.css({visibility: 'hidden'});
            };
            update_legend = function () {
                var $legends = $(selector + ' .legendLabel'),
                    axes = $p1.getAxes(), j, dataset = $p1.getData(), i = dataset.length, series, y, p1, p2,
                    $date_elm = $legend.find('.og-date'), date, format_date;
                format_date = function (date) {
                    return ('' + new Date(date)).replace(/(^.*:[0-9]{2}\s).*$/, '$1');
                };
                $legend = get_legend();
                if (hover_pos.x < axes.xaxis.min || hover_pos.x > axes.xaxis.max ||
                    hover_pos.y < axes.yaxis.min || hover_pos.y > axes.yaxis.max) {
                    $legend.css({visibility: 'hidden'});
                    return;
                }
                $legends.each(function () {$(this).css('line-height', 0)});
                $legend.css({
                    left: hover_pos.pageX - ($(selector).offset().left + 9),
                    visibility: 'visible', position: 'absolute', top: '13px', width: '250px'
                });
                while(i--) {
                    if (!dataset[i].data.length) continue;
                    series = dataset[i];
                    // find the closest x points
                    for (j = 0; j < series.data.length; ++j) if (series.data[j][0] > hover_pos.x) break;
                    // now interpolate
                    p1 = series.data[j - 1], p2 = series.data[j];
                    if (p1 == null) y = p2[1], date = format_date(p2[0]); // first
                    else if (p2 == null) y = p1[1], date = format_date(p1[0]); // last
                    else y = p1[1], date = format_date(p1[0]); // all others
                    if ($date_elm.length) $date_elm.html(date);
                    else {
                        $('<div class="og-date"></div>').css({
                            position: 'absolute', top: '15px', 'left': '31px', 'white-space': 'nowrap',
                            padding: '0 0 0 2px', 'font-size': '10px', 'color': '#999'
                        }).prependTo($legend);
                    }
                    $legends.eq(i).text(y.toFixed(2));
                }
            };
            load_plots([{data: data.data.timeseries.data, label: '0.00'}]);  // yes, I know, lots of data
            // find all timeseries by the specified identifier
            api.rest.timeseries.get({
                handler: function (r) {
                    // build meta data object and populate it with the initial plot data
                    meta = r.data.data.reduce(function (acc, val) {
                        var arr = val.split('|'), field = arr[4], time = arr[5], id = arr[0];
                        if (!acc[field]) acc[field] = {};
                        acc[field][time] = id;
                        return acc;
                    }, {});
                    meta[config.init_data_field][config.init_ob_time] = config.data;
                    state.field = config.init_data_field, state.time = [config.init_ob_time];
                    (function () {
                        // Helper function, returns a timeseries data object, or an id which can be used to get it
                        // Takes a sub object of meta
                        var get_data_or_id = function (obj) {for (var time in obj) {return obj[time]}},
                        // build select
                        build_select = function () {
                            var field, select = '';
                            for (field in meta) select += '<option>'+ field +'</option>';
                            return select = '<div class="og-field"><span>Data field:</span><select>' + select
                                + '</select></div>';
                        },
                        // build checkboxes
                        build_checkbox = function  () {
                            var time, checkbox = '';
                            for (time in meta[state.field]) {
                                checkbox += '<label><input type="checkbox" /><span>' + time + '</span></label>';
                            }
                            return checkbox = '<div class="og-observation">' + checkbox + '</div>';
                        },
                        // build form
                        build_form = function () {
                            var $form = $(build_select() + build_checkbox()), ctr = 0;
                            // Set selected options
                            $form.find('select').val(state.field);
                            $.each(state.time, function (i, time) {
                                $form.find('label span').contents().each(function (i, node) {
                                    if (time === node.textContent) $(this).parent().prev().prop('checked', 'checked')
                                        .parent().css({'color': '#fff', 'background-color': colors_arr[ctr]}), ctr += 1;
                                });
                            });
                            // attach handlers
                            $form.find('select, input').change(function (e) {
                                var meta_sub_obj, data = [], is_select = $(e.currentTarget).is('select'),
                                    handler = function (r) {
                                        var td = r.data.template_data, field = td.data_field,
                                            time = td.observation_time, t;
                                        state.field = field, meta[field][time] = r;
                                        if (is_select) state.time = [time],
                                                data.push({data: r.data.timeseries.data, label: time});
                                        else for (t in state.time)
                                            data.push({
                                                data: meta[state.field][state.time[t]].data.timeseries.data,
                                                label: state.time[t]
                                            });
                                        load_plots(data);
                                        $plot_header.html(build_form());
                                };
                                if (is_select) {
                                    state.from = state.to = void 0;
                                    meta_sub_obj = get_data_or_id(meta[e.currentTarget.value]);
                                    if (typeof meta_sub_obj === 'object') handler(meta_sub_obj);
                                    else api.rest.timeseries.get({handler: function (r) {handler(r)}, id: meta_sub_obj});
                                }
                                if (!is_select) {
                                    var new_time = $(this).next().text(), index_of = state.time.indexOf(new_time);
                                    meta_sub_obj = meta[state.field][new_time];
                                    ~index_of ? state.time.splice(index_of, 1) : state.time.push(new_time);
                                    if (typeof meta_sub_obj === 'object') handler(meta_sub_obj);
                                    else api.rest.timeseries.get({handler: function (r) {handler(r)}, id: meta_sub_obj});
                                }
                            });
                            return $form;
                        };
                        $plot_header.html(build_form());
                    }());
                },
                loading: function () {console.log('loading...');},
                identifier: identifier
            });
        }

    }
});