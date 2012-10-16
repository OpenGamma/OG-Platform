
$.register_module({
    name: 'og.analytics.DatasourcesMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), opts = menu.opts, data = menu.data, query = [], ds_val, 
                type_val, sel_pos, default_type_txt = 'select type...', default_sel_txt = 'select data source...',
                del_s = '.og-icon-delete', parent_s = '.OG-dropmenu-options', wrapper = '<wrapper>', type_s = '.type', 
                source_s = '.source', menu_click_s = 'input, button, div.og-icon-delete, a.OG-link-add',
                extra_opts_s = '.extra-opts', latest_s = '.latest', custom_s = '.custom', custom_val = 'Custom',
                date_selected_s = 'date-selected', active_s = 'active', versions_s = '.versions', 
                corrections_s = '.corrections', $dom = menu.$dom,  $type_select, $source_select, $parent, $query,
                $option, $extra_opts, $snapshot_opts, $historical_opts, $latest, $custom, $datepicker,
                events = {
                    type_reset: 'dropmenu:ds:typereset',
                    type_selected:'dropmenu:ds:typesselected',
                    data_selected: 'dropmenu:ds:dataselected',
                    opts_repositioned: 'dropmenu:ds:optsrespositioned'
                },
                populate_src_options = function (data) {
                    $parent.data('type', type_val).addClass(type_val);
                    reset_source_select();
                    data.forEach(function (d) {$source_select.append($($option.html()).text(d.name || d));});
                },
                populate_livedatasources = function () {
                    og.api.rest.livedatasources.get().pipe(function (resp) { populate_src_options(resp.data); });
                },
                populate_marketdatasnapshots = function () {
                    og.api.rest.marketdatasnapshots.get().pipe(function (resp) {
                        populate_src_options(resp.data[0].snapshots);
                    }).pipe(function () { $source_select.after($snapshot_opts.html()); });
                },
                populate_historical = function () {
                    og.api.rest.timeseries.get().pipe(function (resp) {
                        // console.log(resp);
                        populate_src_options(['Mock A','Mock B','Mock C', 'Mock D']);
                    }).pipe(function () {$source_select.after($historical_opts.html());})
                },
                display_query = function () {
                    if(query.length) {
                        var i = 0, arr = [];
                        query.sort(sort_opts).forEach(function (entry) { // revisit the need for sorting this..
                            if (i > 0) arr[i++] = $dom.title_infix.html() + " ";
                            arr[i++] = entry;
                        });
                        $query.html(arr.reduce(function (a, v) { return a += v.type ? v.type + ":" + v.src : v; }, ''));
                    } else $query.text(default_sel_txt);
                },
                sort_opts = function (a, b) {
                    if (a.pos < b.pos) return -1;
                    if (a.pos > b.pos) return 1;
                    return 0;
                },
                remove_entry = function (entry) {
                    if (~entry) query.splice(entry, 1);
                    else if (query.length === 1) return query.length = 0; // emitEvent; reset_query
                },  
                remove_orphans = function () {
                    return reset_source_select(), $parent.removeClass($parent.data('type')).find('.extra-opts').remove();
                },
                reset_query = function () {
                    return $query.text(default_sel_txt), $type_select.val(default_sel_txt).focus(), remove_entry();
                },
                reset_source_select = function () {
                    $source_select.empty().append($($option.html()).text(default_sel_txt));
                },
                type_handler = function (entry) {
                    if (type_val === default_type_txt){
                        if (menu.opts.length === 1 && query.length === 1) return remove_orphans(), reset_query();
                        return remove_entry(entry), remove_orphans(), display_query(), enable_extra_options(false);
                        // emitEvent; type_selected
                    }
                    if ($parent.hasClass($parent.data('type'))) remove_entry(entry), remove_orphans(), display_query();
                    switch (type_val) {
                        case 'live': populate_livedatasources(); break;
                        case 'snapshot': populate_marketdatasnapshots(); break;
                        case 'historical': populate_historical(); break;
                    }
                },
                source_handler = function (entry) {
                    if (ds_val === default_sel_txt)
                        return remove_entry(entry), display_query(), enable_extra_options(false);
                    else if (~entry) query[entry] = {pos:sel_pos, type:type_val, src:ds_val};
                    else query.splice(sel_pos, 0, {pos:sel_pos, type:type_val, src:ds_val});
                    enable_extra_options(true);
                    display_query(); // emitEvent; data_selected 
                },
                delete_handler = function (entry) {
                    if (menu.opts.length === 1) return remove_orphans(), reset_query();
                    if ($type_select !== undefined) menu.del_handler($parent);
                    if (menu.opts.length) {
                        for (var i = ~entry ? entry : sel_pos, len = query.length; i < len; query[i++].pos -= 1);
                        if (~entry) return remove_entry(entry), display_query(); // emitEvent; opts_repositioned 
                    }
                },
                enable_extra_options = function (val) {
                    var $inputs = $extra_opts.find('input');
                    if (!$inputs) return;
                    if (val) $inputs.removeAttr('disabled').filter(latest_s).addClass(active_s);
                    else $inputs.attr('disabled', true).filter('.'+active_s).removeClass(active_s);
                    $inputs.filter(custom_s).removeClass(active_s+ ' ' +date_selected_s).val(custom_val);
                },
                date_handler = function (entry) {
                    if (!$custom.datepicker('isDisabled')) {
                        $custom.addClass(active_s+ ' ' +date_selected_s).focus().datepicker('hide');
                        $latest.removeClass(active_s);
                        if ($custom.parent().is(versions_s))
                            query[entry].version_date = $custom.datepicker('getDate');
                        else if ($custom.parent().is(corrections_s))
                            query[entry].correction_date = $custom.datepicker('getDate');
                        else query[entry].date = $custom.datepicker('getDate');
                    }
                },
                remove_date = function (entry) {
                    $latest.addClass(active_s);
                    $custom.removeClass(active_s+ ' ' +date_selected_s).val(custom_val);
                    if ($custom.parent().is(versions_s)) delete query[entry].version_date;
                    else if ($custom.parent().is(corrections_s)) delete query[entry].correction_date;
                    else delete query[entry].date;
                },
                menu_handler = function (event) {
                    var $elem = $(event.srcElement || event.target), entry;
                    $parent = $elem.parents(parent_s);
                    $type_select = $parent.find(type_s);
                    $source_select = $parent.find(source_s);
                    $extra_opts = $parent.find(extra_opts_s);
                    type_val = $type_select.val();
                    ds_val = $source_select.val();
                    sel_pos = $parent.data('pos');
                    entry = (query.pluck('pos').indexOf(sel_pos));
                    if ($elem.is(menu.$dom.add)) return menu.add_handler();
                    if ($elem.is(del_s)) return delete_handler(entry);
                    if ($elem.is($type_select)) return type_val = type_val.toLowerCase(), type_handler(entry);
                    if ($elem.is($source_select)) return source_handler(entry);
                    if ($elem.is(custom_s))
                        return $custom = $elem, $latest = $elem.siblings().filter(latest_s),
                            $custom.datepicker({onSelect:function () {date_handler(entry)}}).datepicker('show');
                    if ($elem.is(latest_s)) 
                        return $latest = $elem, $custom = $elem.siblings().filter(custom_s), remove_date(entry);
                };
            if ($dom) {
                $dom.title_infix.append('<span>then</span>'); // Move to DropMenu class
                $query = $('.datasources-query', $dom.title);
                $option = $(wrapper).append('<option>');
                $snapshot_opts = $(wrapper).append([
                '<div class="extra-opts">',
                    '<div class="versions">',
                        '<span>Versions:</span>',
                        '<input class="latest" type="button" value="Latest" disabled />',
                        '<input class="OG-js-datetimepicker custom" type="button" value="Custom" disabled="true" />',
                    '</div>',
                    '<div class="corrections">',
                        '<span>Correction:</span>',
                        '<input class="latest" type="button" value="Latest" disabled />',
                        '<input class="OG-js-datetimepicker custom" type="button" value="Custom" disabled="true" />',
                    '</div>',
                '</div>'
                ].join(''));
                $historical_opts = $(wrapper).append([
                '<div class="extra-opts">',
                    '<input class="latest" type="button" value="Latest" disabled />',
                    '<input class="OG-js-datetimepicker custom" type="button" value="Custom" disabled />',
                '</div>'
                ].join(''));
                if ($dom.title) $dom.title.on('click', menu.title_handler.bind(menu));
                if ($dom.menu) {
                    $dom.menu.on('click', menu_click_s, menu_handler).on('change', 'select', menu_handler);
                }
            };
            menu.getQuery = function () {
                return query;
            }
            return menu;
        };
    }
});