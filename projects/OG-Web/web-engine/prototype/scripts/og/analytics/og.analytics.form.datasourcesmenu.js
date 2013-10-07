/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.DatasourcesMenu',
    dependencies: [],
    obj: function () {
        var Block = og.common.util.ui.Block;
        var DatasourcesMenu = function (config) {
            if (!config) {
                return og.dev.warn('og.analytics.DatasourcesMenu: Missing param [config] to constructor.');
            }
            if (!(config.hasOwnProperty('form')) || !(config.form instanceof og.common.util.ui.Form)) {
                return og.dev.warn('og.analytics.DatasourcesMenu: Missing param key [config.form] to constructor.');
            }
            // Private
            var block = this, initialized = false, form = config.form, menu, query = [], $query,
                default_type_txt = 'select type...', default_sel_txt = 'select data source...',
                del_s = '.og-icon-delete', parent_s = '.OG-dropmenu-options', type_s = '.type', source_s = '.source',
                extra_opts_s = '.extra-opts', latest_s = '.latest', custom_s = '.custom', custom_val = 'Custom',
                date_selected_s = 'date-selected', active_s = 'active', versions_s = '.versions',
                corrections_s = '.corrections', types = [], datasources, default_source, events = og.common.events,
                sources = {
                    live: {
                        type: 'Live',
                        source: '',
                        datasource: 'livedatasources',
                        api_opts: { cache_for: 5000 }
                    },
                    snapshot: {
                        type: 'Snapshot',
                        source: '',
                        datasource: 'marketdatasnapshots',
                        api_opts: { cache_for: 5000 }
                    },
                    randomizedsnapshot: {
                        type: 'RandomizedSnapshot',
                        source: '',
                        datasource: 'marketdatasnapshots',
                        updateProbability: '0.2',
                        maxPercentageChange: '5',
                        averageCycleInterval: '1000',
                        api_opts: { cache_for: 5000 }
                    },
                    historical: {
                        type: 'Historical',
                        source: '',
                        date: '',
                        datasource: 'timeseriesresolverkeys',
                        api_opts: { cache_for: 5000 }
                    }
                };

            var add_handler = function (obj) {
                add_row_handler(obj).html(function (html) {
                    menu.add_handler($(html));
                    menu.opts[menu.opts.length - 1].data('type', obj.type.toLowerCase());
                    source_handler(menu.opts.length - 1);
                });
            };

            var add_row_handler = function (obj) {
                return new form.Block({
                    module: 'og.analytics.form_datasources_row_tash',
                    extras: {
                        type: obj.type.toLowerCase(),
                        type_list: types.map(function (entry) {
                            return {text: entry.type, selected: obj.type === entry.type};
                        })
                    },
                    children: [add_source_dropdown(obj)]
                });
            };

            // create the source dropdown block
            var add_source_dropdown = function (obj) {
                var datasource = obj.datasource.split('.').reduce(function (api, key) {return api[key]; }, og.api.rest);
                return new form.Block({
                    module: 'og.analytics.form_datasources_source_tash',
                    generator: function (handler, tmpl, data) {
                        datasource.get(obj.api_opts).pipe(function (resp) {
                            if (resp.error) {
                                return og.dev.warn('og.analytics.DatasourcesMenu: ' + resp.message);
                            }
                            data.source = obj.type === 'Live' ? resp.data.map(function (entry) {
                                return { text: entry, value: entry, selected: obj.source === entry };
                            }) : obj.type === 'Historical' ? resp.data.map(function (entry) {
                                return { text: entry, value: entry, selected: obj.source === entry};
                            }) : obj.type === 'Snapshot' ? resp.data[0].snapshots.map(function (entry) {
                                return { text: entry.name, value: entry.id, selected: obj.source === entry.id };
                            }) : obj.type === 'RandomizedSnapshot' ? resp.data[0].snapshots.map(function (entry) {
                                return { text: entry.name, value: entry.id, selected: obj.source === entry.id };
                            }) : {};
                            if (obj.type === 'Historical') {
                                data.historical = {
                                    fixed: obj.date ? true : false,
                                    latest: !!(obj.date === '')
                                };
                            }
                            if (data.historical && data.historical.fixed) {
                                data.historical.date = obj.date;
                            }
                            if (obj.type === 'RandomizedSnapshot') {
                                data.randomizedsnapshot = {
                                    updateProbability: obj.updateProbability,
                                    maxPercentageChange: obj.maxPercentageChange,
                                    averageCycleInterval: obj.averageCycleInterval
                                };
                            }
                            handler(tmpl(data));
                        });
                    }
                });
            };

            var date_handler = function (entry) { //TODO AG: refocus custom, hide datepicker
                if (!menu.opts[entry]) {
                    return;
                }
                var custom = $(custom_s, menu.opts[entry]), latest = $(latest_s, menu.opts[entry]),
                    idx = query.pluck('pos').indexOf(menu.opts[entry].data('pos'));
                if (custom) {
                    custom.addClass(active_s + ' ' + date_selected_s);
                }
                if (latest) {
                    latest.removeClass(active_s);
                }
                if (custom.parent().is(versions_s)) {
                    query[idx].version_date = custom.datepicker('getDate');
                } else if (custom.parent().is(corrections_s)) {
                    query[idx].correction_date = custom.val();
                } else {
                    query[idx].date = custom.val();
                }
            };

            var delete_handler = function (entry) {
                if (!menu.opts[entry]) return;
                if (menu.opts.length === 1 && query.length) {
                    return remove_ext_opts(entry), reset_query(entry);
                }
                var idx = query.pluck('pos').indexOf(menu.opts[entry].data('pos')),
                    sel_pos = menu.opts[entry].data('pos');
                menu.delete_handler(menu.opts[entry]);
                if (menu.opts.length) {
                    for (var i = ~idx ? idx : sel_pos, len = query.length; i < len; query[i++].pos -= 1);
                    if (~idx) return remove_entry(idx), display_query(); // fire; optsrepositioned
                }
            };

            var deserialize = function (data) {
                return data.map(function (entry) { // TODO AG: refactor.
                    var obj;
                    switch (entry.marketDataType) {
                        case 'live':
                            obj = $.extend({}, sources['live'], { source: entry.source });
                            break;
                        case 'snapshot':
                            obj = $.extend({}, sources['snapshot'], { source: entry.snapshotId });
                            break;
                        case 'randomizedsnapshot':
                            obj = $.extend({}, sources['randomizedsnapshot'], {
                                source: entry.snapshotId,
                                updateProbability: entry.updateProbability,
                                maxPercentageChange: entry.maxPercentageChange,
                                averageCycleInterval: entry.averageCycleInterval
                            });
                            break;
                        case 'latestHistorical':
                            obj = $.extend({}, sources['historical'], { source: entry.resolverKey });
                            break;
                        case 'fixedHistorical':
                            obj = $.extend({}, sources['historical'], { source: entry.resolverKey, date: entry.date });
                            break;
                        //no default
                    }
                    return obj;
                });
            };

            var display_datepicker = function (entry) {
                if (!menu.opts[entry]) return;
                var custom_dp = $(custom_s, menu.opts[entry]), widget;
                custom_dp.datepicker({
                    dateFormat:'yy-mm-dd',
                    onSelect: function () {
                        date_handler(entry);
                        widget.off('mouseup.prevent_blurkill');
                    }
                });
                custom_dp.datepicker('show');
                widget = custom_dp.datepicker('widget').on('mouseup.prevent_blurkill', function() {
                    menu.fire('dropmenu:open');
                });
            };

            var display_query = function () {
                if(query.length) {
                    var i = 0, arr = [];
                    query.sort(menu.sort_opts).forEach(function (entry) { // revisit the need for sorting this..
                        if (i > 0) arr[i++] = menu.$dom.toggle_infix.html() + ' ';
                        arr[i++] = entry;
                    });
                    $query.html(arr.reduce(function (a, v) {
                        return a += v.type ? menu.capitalize(v.type) + ':' + v.txt : menu.capitalize(v);
                    }, ''));
                } else $query.text(default_sel_txt);
            };

            // enable/disable extra option fields
            var enable_extra_options = function (entry, val, preload) {
            if (!menu.opts[entry]) return;
                var inputs = $(extra_opts_s, menu.opts[entry]).find('input');
                if (!inputs || preload) {
                    return;
                }
                if (val) {
                    inputs.removeAttr('disabled').filter(latest_s).addClass(active_s);
                } else {
                    inputs.attr('disabled', true).filter('.'+active_s).removeClass(active_s);
                }
                inputs.filter(custom_s).removeClass(active_s+ ' ' +date_selected_s).val(custom_val);
            };

            var init = function () {
                menu = new og.analytics.form.DropMenu({cntr: $('.OG-analytics-form .og-datasources')});
                if (menu.$dom) {
                    $query = $('.datasources-query', menu.$dom.toggle);
                    if (menu.$dom.menu)
                        menu.$dom.menu
                            .on('click', 'input, button, div.og-icon-delete, a.OG-link-add', menu_handler)
                            .on('change', 'select', menu_handler)
                            .on('change', '.og-randomized-opts input', menu_handler)
                            .on('keypress', 'select.source', function (event) {
                                if (event.keyCode === 13) return form.submit();
                            });
                    menu.opts.forEach(function (entry, idx) { source_handler(idx, true); });
                    og.common.events.on('datasources:dropmenu:open', function() {menu.fire('dropmenu:open', this);});
                    og.common.events.on('datasources:dropmenu:close', function() {menu.fire('dropmenu:close', this);});
                    og.common.events.on('datasources:dropmenu:focus', function() {menu.fire('dropmenu:focus', this);});
                    menu.fire('initialized', [initialized = true]);
                    events.fire.call(form, 'datasources:initialized');
                }
            };

            // central event handler
            var menu_handler = function (event) {
                var entry, elem = $(event.srcElement || event.target), parent = elem.parents(parent_s);
                if (!parent) return;
                entry = parent.data('pos');
                if (elem.is(menu.$dom.add)) return menu.stop(event), add_handler(default_source);
                if (elem.is(del_s)) return menu.stop(event), delete_handler(entry);
                if (elem.is(type_s)) return type_handler(entry);
                if (elem.is(source_s)) return source_handler(entry);
                if (elem.is(custom_s)) return display_datepicker(entry);
                if (elem.is(latest_s)) return remove_date(entry);
                if (elem.is('.updateProbability')) return set_update_probability(entry, elem);
                if (elem.is('.maxPercentageChange')) return set_max_percentage_change(entry, elem);
                if (elem.is('.averageCycleInterval')) return set_average_cycle_interval(entry, elem);
                if (elem.is('button')) return menu.button_handler(elem.text()), menu.stop(event), false;
            };

            var set_update_probability = function (entry, elem) {
                var idx = query.pluck('pos').indexOf(menu.opts[entry].data('pos'));
                query[idx].updateProbability = elem.val();
            };

            var set_max_percentage_change = function (entry, elem) {
                var idx = query.pluck('pos').indexOf(menu.opts[entry].data('pos'));
                query[idx].maxPercentageChange = elem.val();
            };

            var set_average_cycle_interval = function (entry, elem) {
                var idx = query.pluck('pos').indexOf(menu.opts[entry].data('pos'));
                query[idx].averageCycleInterval = elem.val();
            };

            var init_randomized_snapshots = function (entry, act) {
                if (!act) return;
                set_average_cycle_interval(entry, $('.averageCycleInterval', menu.opts[entry]));
                set_max_percentage_change(entry, $('.maxPercentageChange', menu.opts[entry]));
                set_update_probability(entry, $('.updateProbability', menu.opts[entry]));
            };

            var remove_date = function (entry) {
                if (!menu.opts[entry]) return;
                var custom = $(custom_s, menu.opts[entry]).removeClass(active_s+ ' ' +date_selected_s).val(custom_val);
                $(latest_s, menu.opts[entry]).addClass(active_s);
                if (custom.parent().is(versions_s)) {
                    delete query[entry].version_date;
                } else if (custom.parent().is(corrections_s)) {
                    delete query[entry].correction_date;
                } else {
                    delete query[entry].date;
                }
            };

            var remove_entry = function (entry) {
                if (menu.opts.length === 1 && query.length === 1) {
                    return query.length = 0;
                } // fire; resetquery
                if (~entry) {
                    query.splice(entry, 1);
                }
            };

            var remove_ext_opts = function (entry) {
                if (!menu.opts[entry]) return;
                var parent = menu.opts[entry];
                return reset_source_select(entry), parent.removeClass(parent.data('type')).find(extra_opts_s).remove();
            };

            var reset_query = function (entry) {
                if (!menu.opts[entry]) return;
                var type_select = $(type_s, menu.opts[entry]);
                return $query.text(default_sel_txt), type_select.val(default_sel_txt).focus(), remove_entry();
            };

            var reset_source_select = function (entry) {
                if (!menu.opts[entry]) return;
                var source_select = $(source_s, menu.opts[entry]).hide(), options = $('option', source_select), i;
                if (!source_select || !options.length) return;
                options.each(function(idx){ // IE
                    if (idx > 0) $(this).remove();
                });
                source_select.show();
            };

            var serialize = function () {
                if (!query.length) return;
                var arr = [];
                query.forEach(function (entry) {
                    var obj = {}, val = entry.type.toLowerCase();
                    switch (val) {
                        case 'live':
                            obj['marketDataType'] = val;
                            obj['source'] = entry.src;
                            break;
                        case 'snapshot':
                            obj['marketDataType'] = val;
                            obj['snapshotId'] = entry.src;
                            break;
                        case 'randomizedsnapshot':
                            obj['marketDataType'] = val;
                            obj['snapshotId'] = entry.src;
                            obj['updateProbability'] = entry.updateProbability;
                            obj['maxPercentageChange'] = entry.maxPercentageChange;
                            obj['averageCycleInterval'] = entry.averageCycleInterval;
                            break;
                        case 'historical':
                            if (entry.date) {
                                obj['marketDataType'] = 'fixedHistorical';
                                obj['date'] = entry.date;
                            } else obj['marketDataType'] = 'latestHistorical';
                            obj['resolverKey'] = entry.src; break;
                        //no default
                    }
                    arr.push(obj);
                });
                return arr;
            };

            // dropdown handler for data source for specific data type
            var source_handler = function (entry, preload) {
                if (!menu.opts[entry]) return;
                var option, sel_pos = menu.opts[entry].data('pos'),
                    type_val = $(type_s, menu.opts[entry]).val().toLowerCase(),
                    source_select = $(source_s, menu.opts[entry]),
                    source_val = source_select.val(),
                    source_txt = source_select.find('option:selected').text(),
                    idx = query.pluck('pos').indexOf(sel_pos),
                    date = $('.extra-opts .custom', menu.opts[entry]).val();
                    date = date !== 'Custom' ? date : null;
                if (source_val === default_sel_txt) {
                    return remove_entry(idx), display_query(), enable_extra_options(entry, false);
                } else if (~idx) {
                    query[idx] = {pos:sel_pos, type:type_val, src:source_val, txt: source_txt, date: date};
                } else {
                    query.splice(sel_pos, 0, {pos:sel_pos, type:type_val, src:source_val, txt: source_txt, date: date});
                }
                init_randomized_snapshots(entry, type_val === 'randomizedsnapshot');
                enable_extra_options(entry, true, preload);
                display_query();
            };

            // dropdown handler for live, snapshot, randomized snapshot or historical
            var type_handler = function (entry) {
                if (!menu.opts[entry]) return;
                var parent = menu.opts[entry], type_select = $(type_s, parent), src_parent = $('.datasource', parent),
                    type_val = type_select.val().toLowerCase(), idx = query.pluck('pos').indexOf(parent.data('pos'));
                if (type_val === default_type_txt.toLowerCase()){
                    if (menu.opts.length === 1 && query.length === 1) return remove_ext_opts(entry), reset_query(entry);
                    return remove_entry(idx), remove_ext_opts(entry), display_query(),
                        enable_extra_options(entry, false);
                }
                if (parent.hasClass(parent.data('type'))) {
                    remove_entry(idx);
                    remove_ext_opts(entry);
                    display_query();
                }
                add_source_dropdown(sources[type_val]).html(function (html) {
                    src_parent.html($(html));
                    menu.opts[entry].data('type', type_val);
                    parent.addClass(parent.data('type'));
                    source_handler(parent.data('pos'));
                });
            };

            form.Block.call(block, {
                data: { providers: [] },
                module: 'og.analytics.form_datasources_loading_tash',
                processor: function (data) {
                    data.providers = serialize();
                }
            });

            $.when( //TODO AG: Automate this process when an endpoint is available for datasource types
                og.api.rest.livedatasources.get({page: '*'}),
                og.api.rest.timeseriesresolverkeys.get({page: '*'}),
                og.api.rest.marketdatasnapshots.get({page: '*'})
            ).pipe(function (live, historical, snapshot) {
                if (live.data.length) {
                    types.push({type: 'Live', source: live.data[0]});
                }
                if (historical.data.length) {
                    types.push({type: 'Historical', source: historical[0]});
                }
                if (snapshot.data.length && snapshot.data[0].snapshots.length) {
                    types.push({type: 'Snapshot', source: snapshot.data[0].snapshots[0].id});
                    types.push({type: 'RandomizedSnapshot', source: snapshot.data[0].snapshots[0].id});
                }
                default_source = $.extend({}, sources[types[0].type.toLowerCase()]);
                default_source.source = types[0].source;
                datasources = config.source ? deserialize(config.source) : [default_source];
                new form.Block({
                    module: 'og.analytics.form_datasources_tash',
                    children: datasources.map(add_row_handler)
                }).html(function (html) {
                    $('.datasource-load').replaceWith(html);
                    init();
                });
            });

        };

        DatasourcesMenu.prototype = new Block;
        return DatasourcesMenu;
    }
});