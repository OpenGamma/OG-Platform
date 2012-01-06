/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 *
 * Renders a Canvas plot using Flot
 * @see http://code.google.com/p/flot/
 */
$.register_module({
    name: 'og.common.gadgets.timeseries',
    dependencies: ['og.api.rest'],
    obj: function () {
        var api = og.api, config, handler;
        handler = function (result) {
            if (result.error) return;
            var identifier = result.data.identifiers[0].value,
                data = result.data,
                selector = config.selector,
                init_data_field = data.template_data.data_field,
                init_ob_time = data.template_data.observation_time,
                data_arr = [{
                    data: data.timeseries.data,
                    label: '0.00',
                    data_provider: data.template_data.data_provider,
                    data_source: data.template_data.data_source,
                    object_id: data.template_data.object_id
                }],
                meta, // object that stores the structure and data of the different plots
                state = {}, // keeps a record of the current active data sets in the plot along with zoom and pan data
                colors_arr = ['#42669a', '#ff9c00', '#00e13a', '#313b44'], // line colours for plot 1 data sets
                colors_arr_p2 = ['#ccc', '#b1b1b1', '#969696', '#858585'], // line colours for plot 2 data sets
                $p1, p1_options, p1_selector = selector + ' .og-js-p1',
                $p2, p2_options, p2_selector = selector + ' .og-js-p2',
                tenor = selector + ' .og-tenor',
                plot_selector = selector + ' .og-plot-header',
                $legend, panning, hover_pos = null,
                x_max, initial_preset, reset_options,
                load_plots, empty_plots, update_legend, rescale_yaxis, calculate_y_values, data_points, get_legend;
            $(selector).html('\
              <div class="og-flot-top"></div>\
              <div class="og-flot-xaxis"></div>\
              <div class="og-flot-right-mask"></div>\
              <div class="og-flot-left-mask"></div>\
              <div class="og-timeseries-plot og-js-timeseries-plot">\
                <div class="og-js-p1-crop"><div class="og-js-p1"></div></div>\
                <div class="og-js-p2-crop"><div class="og-js-p2"></div></div>\
                <div class="og-tenor"></div>\
              </div>\
              <div class="og-plot-header"></div>'
            ).css({position: 'relative'});
            $(plot_selector).html('<span class="og-checking-related">checking for related timeseries data...</span>');
            get_legend = function () {return $(selector + ' .legend');}; // the legend is often regenerated
            reset_options = function () {
                p1_options = {
                    colors: colors_arr,
                    series: {shadowSize: 1, threshold: {below: 0, color: '#960505'}},
                    legend: {
                        show: true, labelBoxBorderColor: 'transparent', position: 'nw', margin: 1, backgroundColor: null
                    },
                    crosshair: {mode: 'x', color: '#e5e5e5', lineWidth: '1'},
                    lines: {lineWidth: 1, fill: 1, fillColor: '#f8fbfd'},
                    xaxis: {
                        ticks: 6, mode: 'time', tickLength: 0, labelHeight: 26, color: '#fff', tickColor: null,
                        min: initial_preset, max: x_max
                    },
                    yaxis: {
                        ticks: 5, position: 'right', panRange: false, tickLength: 'full', tickColor: '#f3f3f3',
                        labelWidth: 53, reserveSpace: true
                    },
                    grid: {
                        borderWidth: 1, color: '#999', borderColor: '#e9eaeb', labelMargin: 3,
                        minBorderMargin: 29, hoverable: true
                    },
                    selection: {mode: null},
                    pan: {interactive: true, cursor: "move", frameRate: 30}
                };
                p2_options = {
                    colors: colors_arr_p2,
                    series: {shadowSize: 1, threshold: {below: 0, color: '#960505'}},
                    legend: {show: false},
                    lines: {lineWidth: 1, fill: 1, fillColor: '#fafafa'},
                    xaxis: {ticks: 6, mode: 'time', tickLength: '0', labelHeight: 55, tickColor: '#fff'},
                    yaxis: {show: false, ticks: 1, position: 'right', tickLength: 0, labelWidth: 53, reserveSpace: true},
                    grid: {
                        borderWidth: 1, color: '#999', borderColor: '#e9eaeb',
                        aboveData: true, labelMargin: -14, minBorderMargin: 43
                    },
                    selection: {mode: 'x', color: '#ddd'}
                };
            };
            empty_plots = function () {
                var d = ['1', '2'], disabled_options,
                    msg = $('<div>No data points available</div>').css({
                        position: 'absolute', color: '#999', left: '35px', top: '30px'
                    });
                reset_options();
                data_points();
                disabled_options = {
                    xaxis: {show: false, panRange: false},
                    yaxis: {show: false, panRange: false},
                    selection: {mode: null},
                    pan: {interactive: false}
                };
                $(tenor).css({visibility: 'hidden'});
                $p1 = $.plot($(p1_selector), d, $.extend(true, {}, p1_options, disabled_options));
                $p2 = $.plot($(p2_selector), d, $.extend(true, {}, p2_options, disabled_options));
                $(p1_selector).append(msg);
                $('.og-js-timeseries-plot').animate({opacity: '0.5'});
            };
            data_points = function () {
                var $template, render_grid, $data_points = $('.OG-timeseries .og-data-points'),
                    slick_tmpl = '\
                        <div>\
                          <div class="og-data-series">\
                            <header>\
                              <h3 style="border-left: 2px solid ${color}">${time}</h3>\
                              <span class="OG-link OG-icon og-icon-download og-js-timeseries-csv">download csv</span>\
                            </header>\
                            <div class="og-slick og-slick-${index}"><span class="og-loading">Loading...</span></div>\
                            <footer>Data Source: ${source}<br />Data provider: ${provider}</footer>\
                          </div>\
                        </div>';
                if (!$data_points.length) return;
                $('.OG-timeseries .og-data-points').html('<div class="og-container"></div>');
                $template = $('.OG-timeseries .og-data-points .og-container');
                if (!data_arr) {
                    $template.html('<span class="og-no-datapoint">No data points available</span>')
                        .animate({opacity: '0.5'});
                    return;
                }
                render_grid = function (index) {
                    var SLICK_SELECTOR = '.OG-timeseries .og-data-points .og-slick-' + index, slick, data,
                    columns = [
                        {id: 'time', name: 'Time', field: 'time', width: 200,
                            formatter: function (row, cell, value) {
                                return og.common.util.date(value);
                            }
                        },
                        {id: 'value', name: 'Value', field: 'value', width: 160}
                    ],
                    options = {
                        editable: false,
                        enableAddRow: false,
                        enableCellNavigation: false,
                        headerHeight: 11,
                        showHeaderRow: false,
                        headerRowHeight: 0
                    };
                    data = data_arr[index].data.reduce(function (acc, val) {
                        return acc.push({time: val[0], value: val[1]}) && acc;
                    }, []);
                    $(SLICK_SELECTOR).css({opacity: '0.1'});
                    columns = og.common.slickgrid.calibrate_columns({
                        container: SLICK_SELECTOR,
                        columns: columns,
                        buffer: 17
                    });
                    try {slick = new Slick.Grid(SLICK_SELECTOR, data, columns, options);}
                    catch(e) {$(SLICK_SELECTOR + ' .og-loading').html('' + e);}
                    finally {$(SLICK_SELECTOR).animate({opacity: '1'});}
                };
                data_arr.forEach(function (val, i) {
                    $(slick_tmpl).tmpl({
                        time: (!meta ? init_ob_time : val.label).toLowerCase().replace(/_/g, ' '),
                        index: i,
                        color: colors_arr[i],
                        source: data_arr[i].data_source,
                        provider: data_arr[i].data_provider
                    }).find('.og-js-timeseries-csv')
                      .bind('click', function () {
                          window.location.href = '/jax/timeseries/' + data_arr[i].object_id + '.csv';
                      })
                      .end()
                      .appendTo($template);
                    setTimeout(render_grid.partial(i), 0);
                });
                if (data_arr.length === 1) $data_points.find('.og-data-series').css('width', '799px');
            };
            load_plots = function () {
                if (data_arr[0] === void 0 || data_arr[0].data.length < 2) {empty_plots(); return}
                var d = data_arr, data = data_arr[0].data;
                (function () { // set up presets
                    var new_max_date_obj, _1m, _3m, _6m, _1y, _2y, _3y, counter = 0, presets = {};
                    x_max = data[data.length-1][0];
                    new_max_date_obj = function () {return new Date(x_max)};
                    presets._1d = x_max - 86400 * 1000; // in milliseconds
                    presets._1w = x_max - 7 * 86400 * 1000;
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
                        state.from = from, state.to = x_max;
                        $p2.setSelection({xaxis: {from: from , to: x_max}}, true);
                        p1_options.xaxis.min = from, p1_options.xaxis.max = x_max;
                        rescale_yaxis();
                        $legend = get_legend();
                        $legend.css({visibility: 'hidden'});
                    });
                    $(tenor).css({visibility: 'visible'});
                }());
                reset_options();
                p1_options.xaxis.panRange = [data[0][0], data[data.length-1][0]];
                if (!(state.from && state.to)) {state.from = initial_preset, state.to = x_max;}
                // in xaxis, min/max sets the pan, from/to sets the selection
                p1_options.xaxis.min = state.from, p1_options.xaxis.max = state.to;
                p2_options.xaxis.from = state.from, p1_options.xaxis.to = state.to;
                calculate_y_values();
                if (data_arr.length > 1) p1_options.lines.fill = 0, p2_options.lines.fill = 0;
                $p1 = $.plot($(p1_selector), d, p1_options);
                $p2 = $.plot($(p2_selector), d, p2_options);
                $p2.setSelection({xaxis: {from: state.from, to: state.to}}, true);
                // connect the two plots
                $(p1_selector).unbind('plotselected').bind('plotselected', function (e, r) { // events, ranges
                    state.from = r.xaxis.from, state.to = r.xaxis.to;
                    p1_options.xaxis.min = state.from, p1_options.xaxis.max = state.to;
                    $p1 = $.plot($(p1_selector), d, p1_options);
                    $legend = get_legend(), $legend.css({visibility: 'hidden'});
                    $p2.setSelection(r, true);
                });
                $(p1_selector).unbind('plotpan').bind('plotpan', function (e, obj) { // panning
                    var mouseup = function () {
                        setTimeout(function () {panning = false;}, 0);
                        $(document).unbind('mouseup', mouseup);
                        rescale_yaxis();
                    },
                    xaxes = obj.getXAxes()[0];
                    panning = true;
                    $(document).bind('mouseup', mouseup);
                    $legend = get_legend(), $legend.css({visibility: 'hidden'});
                    $p2.setSelection({xaxis: {from: state.from = xaxes.min, to: state.to = xaxes.max}}, true);
                });
                $(p2_selector).unbind('plotselected').bind('plotselected', function (e, r) {
                    $p1.setSelection(r);
                    $(tenor + ' .OG-link').removeClass('OG-link-active');
                    rescale_yaxis();
                });
                $(p1_selector).unbind('plothover').bind('plothover', function (e, pos) {
                    if (!panning) hover_pos = pos, setTimeout(update_legend, 50);
                });
                $legend = get_legend(), $legend.css({visibility: 'hidden'});
                data_points();
                rescale_yaxis();
                $('.og-js-timeseries-plot').animate({opacity: '1'});
            };
            calculate_y_values = function () {
                var cur, // the current data set
                    idx_from, idx_to,// indexes used to slice [cur] to the visible range
                    sliced, // temporary sliced array
                    get_values, // function that takes an array and return the min an max values with a buffer
                    p1_vals, p2_vals,
                    i = data_arr.length,
                    arr_full = [], // the full data set (for p2)
                    arr_sel = []; // the selection data set (for p1)
                // create 2 arrays, 1 of the VIEWABLE data points of all visible data sets, for p1,
                // and the other of the FULL data points of all visible data sets for p2
                while(i--) {
                    if (!data_arr[i].data.length) continue; // account for threshold data
                    cur = data_arr[i].data;
                    arr_full = arr_full.concat(cur);
                    // Find closest min / max index in x range, lazy
                    // This only needs to be done for one series
                    if (!idx_from) data_arr[0].data.map(function (v, i) {
                        if (!idx_from && v[0] >= state.from) i < 2 ? idx_from = 0 : idx_from = i - 1;
                        if (!idx_to && v[0] >= state.to) i > cur.length - 3 ? idx_to = cur.length - 1 : idx_to = i;
                    });
                    // if the number of data points between the indices is less than 10, add more
                    // this doesn't change the viewable data, just the yaxis range
                    while (idx_to - idx_from < Math.min(10, cur.length - 1)) {
                        if (idx_to !== cur.length - 1) ++idx_to;
                        if (idx_from !== 0) --idx_from;
                    }
                    sliced = data_arr[i].data.slice(idx_from, idx_to);
                    arr_sel = arr_sel.concat(sliced);
                }
                get_values = function (arr) {
                    var min, max, buffer;
                    max = (function (arr) {return Math.max.apply(null, arr.map(function (v) {return v[1];}));})(arr);
                    min = (function (arr) {return Math.min.apply(null, arr.map(function (v) {return v[1];}));})(arr);
                    buffer = (max - min) / 10, max += buffer, min -= buffer;
                    return {min: min, max: max}
                };
                p1_vals = get_values(arr_sel), p2_vals = get_values(arr_full);
                p1_options.yaxis.min = p1_vals.min, p1_options.yaxis.max = p1_vals.max;
                p2_options.yaxis.min = p2_vals.min, p2_options.yaxis.max = p2_vals.max;
            };
            rescale_yaxis = function () {
                calculate_y_values();
                p1_options.xaxis.min = state.from, p1_options.xaxis.max = state.to;
                $p1.setSelection({
                    xaxis: {from: state.from, to: state.to},
                    yaxis: {from: p1_options.yaxis.min, to: p1_options.yaxis.max}
                });
            };
            update_legend = function () {
                var $legends = $(selector + ' .legendLabel'),
                    axes = $p1.getAxes(), j, dataset = $p1.getData(), i = dataset.length, series, y, p1, p2,
                    $date_elm = $legend.find('.og-date'), date, format_date, legend_height,
                    rel_cursor_pos = hover_pos.pageX - ($(selector).offset().left) + 25;
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
                legend_height = $legend.find('table').prev().height() + 30;
                $legend.css({
                    left: rel_cursor_pos > $(selector).width() - 310 ? rel_cursor_pos - 141 : rel_cursor_pos,
                    visibility: 'visible', position: 'absolute', top: '13px', width: '140px',
                    'background-color': '#f9f9f9', 'height': legend_height + 'px', 'border': '1px solid #e5e5e5'
                }).fadeTo(0, 0.9);
                $legend.find('div, table').css({'left': '5px', 'background': 'none'});
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
                            position: 'absolute', top: '15px', 'white-space': 'nowrap',
                            padding: '0 0 0 2px', 'font-size': '10px', 'color': '#999'
                        }).prependTo($legend);
                    }
                    $legends.eq(i).text(y.toFixed(2));
                }
            };
            load_plots();
            // find related timeseries and create the menu
            api.rest.timeseries.get({
                handler: function (r) {
                    // build meta data object and populate it with the initial plot data
                    meta = r.data.data.reduce(function (acc, val) {
                        var arr = val.split('|'), field = arr[4], time = arr[5], id = arr[0];
                        if (!acc[field]) acc[field] = {};
                        acc[field][time] = id;
                        return acc;
                    }, {});
                    meta[init_data_field][init_ob_time] = result;
                    state.field = init_data_field, state.time = [init_ob_time];
                    (function () {
                        // Helper function, returns a timeseries data object, or an id which can be used to get it
                        // Takes a sub object of meta
                        var get_data_or_id = function (obj) {return obj[Object.keys(obj)[0]];},
                        // build select
                        build_select = function () {
                            var field, select = '';
                            for (field in meta) select += '<option>'+ field +'</option>';
                            return select = '<div class="og-field"><span>Timeseries:</span><select>' + select
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
                                    if (time === $(node).text())
                                        $(this).parent().prev().prop('checked', 'checked').parent()
                                            .css({'color': '#fff', 'background-color': colors_arr[ctr]}), ctr += 1;
                                });
                            });
                            // attach handlers
                            $form.find('select, input').change(function (e) {
                                var meta_sub_obj, data = [], is_select = $(e.target).is('select'),
                                    handler = function (r) {
                                        var td = r.data.template_data, field = td.data_field,
                                            time = td.observation_time, t, cached_object;
                                        state.field = field, meta[field][time] = r;
                                        if (is_select) state.time = [time],
                                                data.push({
                                                    data: r.data.timeseries.data,
                                                    label: time,
                                                    data_provider: r.data.template_data.data_provider,
                                                    data_source: r.data.template_data.data_source,
                                                    object_id: r.data.template_data.object_id
                                                });
                                        else for (t in state.time) {
                                            if (!meta[state.field][state.time[t]]) continue;
                                            cached_object = meta[state.field][state.time[t]].data;
                                            data.push({
                                                data: cached_object.timeseries.data,
                                                label: state.time[t],
                                                data_provider: cached_object.template_data.data_provider,
                                                data_source: cached_object.template_data.data_source,
                                                object_id: cached_object.template_data.object_id
                                            });
                                        }
                                        data_arr = data;
                                        load_plots();
                                        $(plot_selector).html(build_form());
                                };
                                if (is_select) {
                                    state.from = state.to = void 0;
                                    meta_sub_obj = get_data_or_id(meta[$(e.target).val()]);
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
                        $(plot_selector).html(build_form());
                    }());
                },
                loading: function () {},
                identifier: identifier
            });
        };
        return function (conf) {
            config = conf;
            api.rest.timeseries.get({
                dependencies: ['id'],
                handler: handler,
                id: config.id,
                cache_for: 10000,
                loading: function () {}
            });
        }
    }
});