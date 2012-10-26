/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.DatasourcesMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), opts = menu.opts, data = menu.data, query = [], ds_val,
                type_val, sel_pos, default_type_txt = 'select type...', default_sel_txt = 'select data source...',
                del_s = '.og-icon-delete', parent_s = '.OG-dropmenu-options', wrapper = '<wrapper>', type_s = '.type',
                source_s = '.source',  extra_opts_s = '.extra-opts', latest_s = '.latest', custom_s = '.custom',
                custom_val = 'Custom', date_selected_s = 'date-selected', active_s = 'active', versions_s = '.versions',
                resolver_keys = [], corrections_s = '.corrections', $dom = menu.$dom,  $type_select, $source_select,
                $parent, $query, $option, $extra_opts, $snapshot_opts, $historical_opts, $latest, $custom, $datepicker, 
                snapshots = {},
                events = {
                    sourcespopulated: 'dropmenu:ds:sourcespopulated',
                    typereset: 'dropmenu:ds:typereset',
                    typeselected:'dropmenu:ds:typesselected',
                    dataselected: 'dropmenu:ds:dataselected',
                    optsrepositioned: 'dropmenu:ds:optsrespositioned'
                },
                populate_src_options = function (data) {
                    $parent.data('type', type_val).addClass(type_val);
                    data.forEach(function (d) {
                        if (d.name) snapshots[d.name] = d.id;
                        $source_select.append($($option.html()).text(d.name || d));
                    });
                },
                populate_livedatasources = function (callback) {
                    og.api.rest.livedatasources.get().pipe(function (resp) {
                        if (resp.error) return;
                        populate_src_options(resp.data);
                    });
                },
                populate_marketdatasnapshots = function () {
                    og.api.rest.marketdatasnapshots.get().pipe(function (resp) {
                        if (resp.error) return;
                        populate_src_options(resp.data[0].snapshots);
                    });
                    /*.pipe(function () { 
                        $source_select.after($snapshot_opts.html());
                    });*/
                },
                populate_historical = function () {
                    if (resolver_keys) {
                        populate_src_options(resolver_keys);
                        $source_select.after($historical_opts.html());
                    }
                },
                display_query = function () {
                    if(query.length) {
                        var i = 0, arr = [];
                        query.sort(menu.sort_opts).forEach(function (entry) { // revisit the need for sorting this..
                            if (i > 0) arr[i++] = $dom.toggle_infix.html() + " ";
                            arr[i++] = entry;
                        });
                        $query.html(arr.reduce(function (a, v) { return a += v.type ? v.type + ":" + v.src : v; }, ''));
                    } else $query.text(default_sel_txt);
                },
                remove_entry = function (entry) {
                    if (menu.opts.length === 1 && query.length === 1) return query.length = 0; // emitEvent; resetquery
                    if (~entry) query.splice(entry, 1);
                },  
                remove_orphans = function () {
                    return reset_source_select(), $parent.removeClass($parent.data('type'))
                        .find('.extra-opts').remove();
                },
                reset_query = function () {
                    return $query.text(default_sel_txt), $type_select.val(default_sel_txt).focus(), remove_entry();
                },
                reset_source_select = function () {
                    // IE doesn't seem to update Select on the fly, so, take it out of DOM, update childNodes, append
                    // it back to the parent
                    var sel_parent = $source_select.parent(), select = $source_select.remove();
                    select.empty().append($($option.html()).text(default_sel_txt));
                    sel_parent.append(select);
                },
                type_handler = function (entry) {
                    if (type_val === default_type_txt){
                        if (menu.opts.length === 1 && query.length === 1) return remove_orphans(), reset_query();
                        return remove_entry(entry), remove_orphans(), display_query(), enable_extra_options(false);
                        // emitEvent; typeselected
                    }
                    if ($parent.hasClass($parent.data('type'))) remove_entry(entry), remove_orphans(), display_query();
                    switch (type_val) {
                        case 'live': populate_livedatasources(); break;
                        case 'snapshot': populate_marketdatasnapshots(); break;
                        case 'historical': populate_historical(); break;
                        //no default
                    }
                },
                source_handler = function (entry) {
                    if (ds_val === default_sel_txt) {
                        return remove_entry(entry), display_query(), enable_extra_options(false);
                    } else if (~entry) {
                        query[entry] = {pos:sel_pos, type:type_val, src:ds_val};
                    } else {
                        query.splice(sel_pos, 0, {pos:sel_pos, type:type_val, src:ds_val});
                    }
                    enable_extra_options(true);
                    display_query(); // emitEvent; dataselected 
                },
                delete_handler = function (entry) {
                    if (menu.opts.length === 1) return remove_orphans(), reset_query();
                    if ($type_select !== undefined) menu.delete_handler($parent);
                    if (menu.opts.length) {
                        for (var i = ~entry ? entry : sel_pos, len = query.length; i < len; query[i++].pos -= 1);
                        if (~entry) return remove_entry(entry), display_query(); // emitEvent; optsrepositioned
                    }
                },
                enable_extra_options = function (val) {
                    var $inputs = $extra_opts.find('input');
                    if (!$inputs) return;
                    if (val) $inputs.removeAttr('disabled').filter(latest_s).addClass(active_s);
                    else $inputs.attr('disabled', true).filter('.'+active_s).removeClass(active_s);
                    $inputs.filter(custom_s).removeClass(active_s+ ' ' +date_selected_s).val(custom_val);
                },
                date_handler = function () { // TODO AG: refocus custom, hide datepicker, rm duplication entry retrieval
                    var entry = query.pluck('pos').indexOf(sel_pos);
                    $custom.addClass(active_s+ ' ' +date_selected_s); 
                    $latest.removeClass(active_s);
                    if ($custom.parent().is(versions_s)) query[entry].version_date = $custom.datepicker('getDate');
                    else if ($custom.parent().is(corrections_s))
                        query[entry].correction_date = $custom.val();
                    else query[entry].date = $custom.val();
                },
                remove_date = function (entry) {
                    $latest.addClass(active_s);
                    $custom.removeClass(active_s+ ' ' +date_selected_s).val(custom_val);
                    if ($custom.parent().is(versions_s)) delete query[entry].version_date;
                    else if ($custom.parent().is(corrections_s)) delete query[entry].correction_date;
                    else delete query[entry].date;
                },
                get_snapshot = function (id) {
                     return Object.keys(snapshots).filter(function(key) {
                        return snapshots[key] === id;
                     })[0];
                }
                menu_handler = function (event) { // TODO AG: Refactor
                    var $elem = $(event.srcElement || event.target), entry;
                    $parent = $elem.parents(parent_s);
                    $type_select = $parent.find(type_s);
                    $source_select = $parent.find(source_s);
                    $extra_opts = $parent.find(extra_opts_s);
                    type_val = $type_select.val();
                    ds_val = $source_select.val();
                    sel_pos = $parent.data('pos');
                    entry = query.pluck('pos').indexOf(sel_pos);
                    if ($elem.is(menu.$dom.add)) return menu.stop(event), menu.add_handler();
                    if ($elem.is(del_s)) return menu.stop(event), delete_handler(entry);
                    if ($elem.is($type_select)) return type_val = type_val.toLowerCase(), type_handler(entry);
                    if ($elem.is($source_select)) return source_handler(entry);
                    if ($elem.is(custom_s))
                        return $custom = $elem, $latest = $elem.siblings().filter(latest_s),
                            $custom.datepicker({onSelect: date_handler, dateFormat:'yy-mm-dd'}).datepicker('show');
                    if ($elem.is(latest_s))
                        return $latest = $elem, $custom = $elem.siblings().filter(custom_s), remove_date(entry);
                    if ($elem.is('button')) return menu.button_handler($elem.text());
                };
            menu.replay_query = function (config) { // TODO AG: refactor initial replay implementation
                menu.opts.forEach(function (option, index) {
                    option.remove();
                });
                menu.opts.length = 0;
                query = [];
                var reinit_menu_elems = function (index) {
                        $parent = menu.opts[index];
                        $type_select = $parent.find(type_s);
                        $source_select = $parent.find(source_s);
                        $extra_opts = $parent.find(extra_opts_s);
                    },
                    set_select_vals = function (src, index) {
                        var source = src.snapshotId ? get_snapshot(src.snapshotId) : src.source,
                            type = menu.capitalize(src.marketDataType);
                        $type_select.val(type);
                        $source_select.val(source);
                        query.splice(index, 0, {pos: index, src: source, type: type});
                        display_query();
                    };
                config.datasources.forEach(function (src, index) {
                    menu.add_handler();
                    type_val = src.marketDataType;
                    switch (type_val) {
                        case 'live': 
                            og.api.rest.livedatasources.get().pipe(function (resp) {
                                if (resp.error) return;
                                reinit_menu_elems(index);
                                populate_src_options(resp.data);
                            }).pipe(function () {
                                set_select_vals(src, index);
                            });
                            break;
                        case 'snapshot': 
                            og.api.rest.marketdatasnapshots.get().pipe(function (resp) {
                                if (resp.error) return;
                                reinit_menu_elems(index);
                                populate_src_options(resp.data[0].snapshots);
                            }).pipe(function () {
                                set_select_vals(src, index);
                            });
                            break;
                        case 'historical': populate_historical(); break;
                        //no default
                    };
                });
            };
            menu.get_query = function () {
                if (!query.length) return;
                var arr = [];
                query.forEach(function (entry) {
                    var obj = {}, val = entry.type.toLowerCase();
                    switch (val) {
                        case 'historical':
                            if (entry.date) {
                                obj['marketDataType'] = 'fixedHistorical';
                                obj['date'] = entry.date;
                            } else obj.marketDataType = 'latestHistorical';
                            obj['resolverKey'] = entry.src;
                            break;
                        case 'snapshot':
                            obj['marketDataType'] = val;
                            obj['snapshotId'] = snapshots[entry.src];
                            break;
                        case 'live':
                            obj['marketDataType'] = val;
                            obj['source'] = entry.src;
                            break;

                        //no default
                    }
                    arr.push(obj);
                });
                return arr;
            };
            menu.destroy = function () {

            };
            if ($dom) {
                $query = $('.datasources-query', $dom.toggle);
                $option = $(wrapper).append('<option>');
                $.when(
                    og.api.text({module: 'og.analytics.form_datasources_snapshot_opts_tash'}),
                    og.api.text({module: 'og.analytics.form_datasources_historical_opts_tash'}),
                    og.api.rest.configs.get({type: 'HistoricalTimeSeriesRating'})
                ).then(function(snapshot, historical, res_keys){
                    $snapshot_opts = $(wrapper).append(snapshot);
                    $historical_opts = $(wrapper).append(historical);
                    res_keys.data.data.forEach(function (entry) {
                        resolver_keys.push(entry.split('|')[1]);
                    });
                    if ($dom.menu) {
                        $dom.menu.on('click', 'input, button, div.og-icon-delete, a.OG-link-add', menu_handler)
                                 .on('change', 'select', menu_handler);
                        }
                });
            }
            return menu;
        };
    }
});