/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.DatasourcesMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () {
        return function (config) {
            if (!config) return og.dev.warn('og.analytics.DatasourcesMenu: Missing param [config] to constructor.');

            if (!(config.hasOwnProperty('form')) || !(config.form instanceof og.common.util.ui.Form))
                return og.dev.warn('og.analytics.DatasourcesMenu: Missing param key [config.form] to constructor.');

            // Private
            var default_conf = {
                    form: config.form,
                    selector: '.og-datasources',
                    tmpl: 'og.analytics.form_datasources_tash',
                    children: [
                        new og.common.util.ui.Dropdown({
                            form: config.form, value: 'Live', placeholder: 'select type...', classes:'type',
                            data_generator: function (callback) { callback(['Live', 'Snapshot', 'Historical']);}
                        }),
                        new og.common.util.ui.Dropdown({
                            form: config.form, resource: 'livedatasources', classes:'source',
                            value: 'Bloomberg', placeholder: 'select data source...'
                        })
                    ]
                },
                events = {
                    open: 'dropmenu:open',
                    sourcespopulated: 'dropmenu:ds:sourcespopulated',
                    typereset: 'dropmenu:ds:typereset',
                    typeselected:'dropmenu:ds:typesselected',
                    dataselected: 'dropmenu:ds:dataselected',
                    optsrepositioned: 'dropmenu:ds:optsrespositioned',
                    resetquery:'dropmenu:resetquery',
                    queryresequested:'dropmenu:queryresequested',
                    preventblurkill: 'mouseup.prevent_blurkill',
                    reset:'reset',
                    replay:'replay'
                },
                query = [], resolver_keys = [], snapshots = {}, $dom, $query, $option, $snapshot_opts, $historical_opts,
                default_type_txt = 'select type...', default_sel_txt = 'select data source...',
                del_s = '.og-icon-delete', parent_s = '.OG-dropmenu-options', wrapper = '<wrapper>', type_s = '.type',
                source_s = '.source',  extra_opts_s = '.extra-opts', latest_s = '.latest', custom_s = '.custom',
                custom_val = 'Custom', date_selected_s = 'date-selected', active_s = 'active', versions_s = '.versions',
                corrections_s = '.corrections', initialized = false, form = config.form;

            var add_handler = function (opts) {
                var index = opts && opts.hasOwnProperty('idx') ? opts.idx : menu.opts.length;
                new og.common.util.ui.Dropdown({
                    form: config.form, resource: opts.source, rest_options: opts.rest_options || {},
                    value: (opts && opts.hasOwnProperty('val') ? opts.val : ''), placeholder: 'select data source...'
                }).html(function (html) {
                    var idx, $elem = menu.$dom.opt_cp.clone(true); $elem.find('td.ds-opts').html(html);
                    menu.add_handler($elem); idx = menu.opts.length-1;
                    populate_src_options(idx);
                    source_handler(idx);
                });
            };

            var date_handler = function (entry, preload) { // TODO AG: refocus custom, hide datepicker
                if (!menu.opts[entry]) return;
                var custom = $(custom_s, menu.opts[entry]), latest = $(latest_s, menu.opts[entry]),
                    idx = query.pluck('pos').indexOf(menu.opts[entry].data('pos'));
                if (custom) custom.addClass(active_s+ ' ' +date_selected_s);
                if (latest) latest.removeClass(active_s);
                if (preload && 'date' in preload) custom.val(preload.date);
                if (custom.parent().is(versions_s)) query[idx].version_date = custom.datepicker('getDate');
                else if (custom.parent().is(corrections_s)) query[idx].correction_date = custom.val();
                else query[idx].date = custom.val();
            };

            var delete_handler = function (entry) {
                if (!menu.opts[entry]) return;
                if (menu.opts.length === 1 && query.length) return remove_ext_opts(entry), reset_query(entry);
                var idx = query.pluck('pos').indexOf(menu.opts[entry].data('pos')),
                    sel_pos = menu.opts[entry].data('pos');
                menu.delete_handler(menu.opts[entry]);
                if (menu.opts.length) {
                    for (var i = ~idx ? idx : sel_pos, len = query.length; i < len; query[i++].pos -= 1);
                    if (~idx) return remove_entry(idx), display_query(); // fire; optsrepositioned
                }
            };

            var display_datepicker = function (entry) {
                if (!menu.opts[entry]) return;
                var custom_dp = $(custom_s, menu.opts[entry]), widget;
                custom_dp.datepicker({
                    dateFormat:'yy-mm-dd',
                    onSelect: function () {
                        date_handler(entry);
                        widget.off(events.preventblurkill);
                    }
                })
                .datepicker('show');
                widget = custom_dp.datepicker('widget').on(events.preventblurkill, function(event) {
                    menu.open();
                });
            };

            var display_query = function () {
                if(query.length) {
                    var i = 0, arr = [];
                    query.sort(menu.sort_opts).forEach(function (entry) { // revisit the need for sorting this..
                        if (i > 0) arr[i++] = $dom.toggle_infix.html() + " ";
                        arr[i++] = entry;
                    });
                    $query.html(arr.reduce(function (a, v) {
                        return a += v.type ? menu.capitalize(v.type) + ":" + v.src : menu.capitalize(v);
                    }, ''));
                } else $query.text(default_sel_txt);
            };

            var enable_extra_options = function (entry, val) {
                if (!menu.opts[entry]) return;
                var inputs = $(extra_opts_s, menu.opts[entry]).find('input');
                if (!inputs) return;
                if (val) inputs.removeAttr('disabled').filter(latest_s).addClass(active_s);
                else inputs.attr('disabled', true).filter('.'+active_s).removeClass(active_s);
                inputs.filter(custom_s).removeClass(active_s+ ' ' +date_selected_s).val(custom_val);
            };

            var get_snapshot = function (id) {
                if (!id) return;
                return Object.keys(snapshots).filter(function(key) {
                    return snapshots[key] === id;
                })[0];
            };

            var init = function (config) {
                $dom = menu.$dom;
                if ($dom) {
                    $query = $('.datasources-query', $dom.toggle);
                    $option = $(wrapper).append('<option>');
                    $.when(
                        og.api.text({module: 'og.analytics.form_datasources_snapshot_opts_tash'}),
                        og.api.text({module: 'og.analytics.form_datasources_historical_opts_tash'}),
                        og.api.rest.configs.get({type: 'HistoricalTimeSeriesRating'})
                    ).then(function(snapshot, historical, res_keys){
                        if (!snapshot.error) $snapshot_opts = $(wrapper).append(snapshot);
                        if (!historical.error) $historical_opts = $(wrapper).append(historical);
                        if (!res_keys.error &&  'data' in res_keys && res_keys.data &&
                            'data' in res_keys.data && res_keys.data.data)
                            res_keys.data.data.forEach(function (entry) {resolver_keys.push(entry.split('|')[1]);});
                        if ($dom.menu) {
                            $dom.menu.find('.type-opts select').attr('class', 'type');
                            $dom.menu.find('.ds-opts select').attr('class', 'source');
                            menu.block
                                .on('click', 'input, button, div.og-icon-delete, a.OG-link-add', menu_handler)
                                .on('change', 'select', menu_handler);
                            init_listeners();
                        }
                        populate_src_options(0);
                        source_handler(0);
                        menu.fire('initialized', [initialized = true]);
                    });
                }
            };

            var init_listeners = function () {
                menu.on(events.queryresequested, remove_orphans);
                menu.on(events.resetquery, menu.reset);
            };

            var menu_handler = function (event) {
                var entry, elem = $(event.srcElement || event.target), parent = elem.parents(parent_s);
                if (!parent) return;
                entry = parent.data('pos');
                if (elem.is(menu.$dom.add)) {
                    return menu.stop(event), add_handler({source:'livedatasources', name:'live', val:'Bloomberg'});
                }
                if (elem.is(del_s)) return menu.stop(event), delete_handler(entry);
                if (elem.is(type_s)) return type_handler(entry);
                if (elem.is(source_s)) return source_handler(entry);
                if (elem.is(custom_s)) return display_datepicker(entry);
                if (elem.is(latest_s)) return remove_date(entry);
                if (elem.is('button')) return menu.button_handler(elem.text());
            };

            var populate_historical = function (entry, config) {
                if (!menu.opts[entry]) return;
                var source_select = $(source_s, menu.opts[entry]);
                if (resolver_keys.length && source_select) {
                    if (config && 'pre_handler' in config && config.pre_handler) config.pre_handler();
                    populate_src_options(entry, resolver_keys);
                    source_select.after($historical_opts.html());
                    if (config && 'post_handler' in config && config.post_handler) config.post_handler();
                }
            };

            var populate_livedatasources = function (entry, config) {
                if (!menu.opts[entry]) return;
                og.api.rest.livedatasources.get({cache_for: 5000}).pipe(function (resp) {
                    if (resp.error) return;
                    if (config && 'pre_handler' in config && config.pre_handler) config.pre_handler();
                    if (resp.data) populate_src_options(entry, resp.data);
                }).pipe(function () {
                    if (config && 'post_handler' in config && config.post_handler) config.post_handler();
                });
            };

            var populate_marketdatasnapshots = function (entry, config) {
                if (!menu.opts[entry]) return;
                og.api.rest.marketdatasnapshots.get({cache_for: 5000}).pipe(function (resp) {
                    if (resp.error) return;
                    if (config && 'pre_handler' in config && config.pre_handler) config.pre_handler();
                    if (resp.data && resp.data[0]) populate_src_options(entry, resp.data[0].snapshots);
                }).pipe(function () {
                    if (config && 'post_handler' in config && config.post_handler) config.post_handler();
                     // $source_select.after($snapshot_opts.html());
                });
            };

            var populate_src_options = function (entry, data) {
                if (!menu.opts[entry]) return;
                var source_select = $(source_s, menu.opts[entry]),
                    type_val = $(type_s, menu.opts[entry]).val().toLowerCase();
                if (!source_select) return;
                if (type_val) menu.opts[entry].data('type', type_val).addClass(type_val);
                if (data) {
                    source_select.hide();
                    data.forEach(function (d) {
                        if ($.isPlainObject(d) && 'name' in d && typeof d.name === 'string') snapshots[d.name] = d.id;
                        source_select.append($($option.html()).text(d.name || d));
                    });
                    source_select.show();
                }
            };

            var remove_date = function (entry) {
                if (!menu.opts[entry]) return;
                var custom = $(custom_s, menu.opts[entry]).removeClass(active_s+ ' ' +date_selected_s).val(custom_val),
                    latest = $(latest_s, menu.opts[entry]).addClass(active_s);
                if (custom.parent().is(versions_s)) delete query[entry].version_date;
                else if (custom.parent().is(corrections_s)) delete query[entry].correction_date;
                else delete query[entry].date;
            };

            var remove_entry = function (entry) {
                if (menu.opts.length === 1 && query.length === 1) return query.length = 0; // fire; resetquery
                if (~entry) query.splice(entry, 1);
            };

            var remove_ext_opts = function (entry) {
                if (!menu.opts[entry]) return;
                var parent = menu.opts[entry];
                return reset_source_select(entry), parent.removeClass(parent.data('type')).find(extra_opts_s).remove();
            };

            var remove_orphans = function () {
                for (var i = menu.opts.length-1; i >= 0; i-=1){
                    if (menu.opts.length === 1) break;
                    var option = menu.opts[i];
                    if ($(type_s, option).val() === default_type_txt || $(source_s, option).val() === default_sel_txt)
                        menu.delete_handler(option);
                }
            };

            var reset_query = function (entry) {
                if (!menu.opts[entry]) return;
                var type_select = $(type_s, menu.opts[entry]);
                return $query.text(default_sel_txt), type_select.val(default_sel_txt).focus(), remove_entry();
            };

            var replay_post_handler = function (entry, src) {
                if (!menu.opts[entry] || !src) return;
                return function () {
                    source_handler(entry, src);
                };
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

            var set_type_select = function (entry, d) {
                if (!menu.opts[entry] || !d || !$.isPlainObject(d)) return;
                if (!('type' in d) || !d.type || typeof d.type !== 'string') return;
                var type_select = $(type_s, menu.opts[entry]);
                switch (d.type) {
                    case 'live' :
                    case 'snapshot': if (type_select) type_select.val(menu.capitalize(d.type)); break;
                    case 'latestHistorical':
                    case 'fixedHistorical': if (type_select) type_select.val('Historical'); break;
                }
            };

            var source_handler = function (entry, preload) {
                if (!menu.opts[entry]) return;
                var val, src, option, sel_pos = menu.opts[entry].data('pos'),
                    type_val = $(type_s, menu.opts[entry]).val().toLowerCase(),
                    source_select = $(source_s, menu.opts[entry]),
                    source_val = source_select.val(),
                    idx = query.pluck('pos').indexOf(sel_pos);
                if (preload) {
                    src = preload.src;
                    val = src.snapshotId ? get_snapshot(src.snapshotId) : src.resolverKey ?
                          src.resolverKey : src.source ? src.source : src.idx ? src.idx : "";
                    if (typeof val === 'string') source_select.val(val); else if (typeof val === 'number') {
                        option = $('option:eq('+val+')', source_select) ? $('option:eq('+val+')', source_select) :
                            $('option:eq(0)', source_select) ? $('option:eq(0)', source_select) : null;
                        if (option) option.attr('selected', 'selected');
                    }
                    source_val = source_select.val();
                }
                if (!preload && source_val === default_sel_txt) {
                    return remove_entry(idx), display_query(), enable_extra_options(entry, false);
                } else if (~idx) query[idx] = {pos:sel_pos, type:type_val, src:source_val};
                else query.splice(sel_pos, 0, {pos:sel_pos, type:type_val, src:source_val});
                enable_extra_options(entry, true);
                display_query();
                if (preload && 'date' in src && typeof src.date === 'string') date_handler(entry, src);
                // fire; dataselected
            };

            var type_handler = function (entry, conf) {
                if (!menu.opts[entry]) return;
                var parent = menu.opts[entry], type_select = $(type_s, parent),
                    type_val = type_select.val().toLowerCase(), idx = query.pluck('pos').indexOf(parent.data('pos'));
                if (type_val === default_type_txt.toLowerCase()){
                    if (menu.opts.length === 1 && query.length === 1) return remove_ext_opts(entry), reset_query(entry);
                    return remove_entry(idx), remove_ext_opts(entry), display_query(),
                        enable_extra_options(entry, false);
                }
                if (parent.hasClass(parent.data('type'))) remove_entry(idx); remove_ext_opts(entry); display_query();
                switch (type_val) {
                    case 'live': populate_livedatasources(entry, conf); break;
                    case 'snapshot': populate_marketdatasnapshots(entry, conf); break;
                    case 'historical': populate_historical(entry, conf); break;
                    //no default
                }
            };

            return menu = new og.analytics.DropMenu(default_conf, init),

            // Public
            menu.get_query = function () {
                if (!query.length) return;
                var arr = [];
                query.forEach(function (entry) {
                    var obj = {}, val = entry.type.toLowerCase();
                    switch (val) {
                        case 'live': obj['marketDataType'] = val, obj['source'] = entry.src; break;
                        case 'snapshot': obj['marketDataType'] = val, obj['snapshotId'] = snapshots[entry.src]; break;
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
                menu.fire(events.queryresequested);
                return arr;
            },

            menu.replay = function (conf) {
                if (!conf && !conf.providers || !$.isArray(conf.providers) || !conf.providers.length) return;

                var datasources = conf.providers.map(function (entry) {
                    var obj = {};
                    if (entry.marketDataType) {
                        switch (entry.marketDataType) {
                            case 'live' : obj.source = 'livedatasources'; break;
                            case 'snapshot':
                                obj.source = 'marketdatasnapshots'; break;
                            case 'fixedHistorical':
                            case 'latestHistorical': obj.source = 'configs'; break;
                        }
                    }
                    if (entry.source) obj.val = entry.source;
                    else if (entry.snapshotId) obj.val = entry.snapshotId;
                    else if (entry.resolverKey) {
                        obj.val = entry.resolverKey;
                        if (entry.date) obj.date = entry.date;
                    }
                    obj.rest_options = obj.source === 'configs' ?
                                    {type: 'HistoricalTimeSeriesRating'} :
                                    {cache_for: 5000};
                    return obj;
                });

                var replay_opts = function () {
                    var i, len, src;
                    menu.opts.forEach(function (option) { option.remove(); }); menu.opts.length = 0; query = [];
                    for (i = 0, len = datasources.length; i < len; i+=1) {
                        add_handler({
                            source: datasources[i].source,
                            val: datasources[i].val,
                            rest_options: datasources[i].rest_options || {},
                            date: datasources[i].date || null
                        });
                    }
                };
                if (!initialized) menu.on('initialized', replay_opts); else replay_opts();
            },

            menu.reset = function () {
                menu.opts.forEach(function (option) { option.remove(); });
                menu.opts.length = 0;
                query = [];
                return add_handler({source:'livedatasources', val:'Bloomberg'}), reset_query();
            },

            menu;
        };
    }
});