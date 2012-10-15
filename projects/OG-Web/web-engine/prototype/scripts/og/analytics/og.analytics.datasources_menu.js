
$.register_module({
    name: 'og.analytics.DatasourcesMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), opts = menu.opts, data = menu.data, query = [], ds_val, 
                type_val, sel_pos, default_type_txt = 'select type...', default_sel_txt = 'select data source...',
                del_s = '.og-icon-delete', parent_s = '.OG-dropmenu-options', wrapper = '<wrapper>', type_s = '.type', 
                source_s = '.source', menu_click_s = 'input, button, div.og-icon-delete, a.OG-link-add', 
                $dom = menu.$dom,  $type_select, $source_select, $parent, $query, $option, $snapshot_opts, $datepicker,
                $historical_opts, $latest, $custom, $extra_opts,
                events = {
                    type_reset: 'dropmenu:ds:typereset',
                    type_selected:'dropmenu:ds:typesselected',
                    data_selected: 'dropmenu:ds:dataselected',
                    opts_repositioned: 'dropmenu:ds:optsrespositioned'
                },
                populate_type_options = function (data) {
                    $parent.data('type', type_val).addClass(type_val);
                    $source_select.empty().append($($option.html()).text(default_sel_txt));
                    data.forEach(function (d) {$source_select.append($($option.html()).text(d.name || d));});
                },
                populate_livedatasources = function () {
                    og.api.rest.livedatasources.get().pipe(function (resp) { populate_type_options(resp.data); });
                },
                populate_marketdatasnapshots = function () {
                    og.api.rest.marketdatasnapshots.get().pipe(function (resp) {
                        populate_type_options(resp.data[0].snapshots);
                    }).pipe(function () { $source_select.after($snapshot_opts.html()); });
                },
                populate_historical = function () {
                    populate_type_options(['Mock A','Mock B','Mock C', 'Mock D']);
                    $source_select.after($historical_opts.html());
                },
                display_query = function () {
                    if(query.length) {
                        var i = 0, arr = [];
                        query.sort(sort_opts).forEach(function (entry) { // revisit the need for sorting this..
                            if (i > 0) arr[i++] = $dom.title_infix.html() + " ";
                            arr[i++] = entry;
                        });
                        $query.html(arr.reduce(function (a, v) { return a += v.type ? v.type + ":" + v.ds : v; }, ''));
                    } else $query.text(default_sel_txt);
                },
                sort_opts = function (a, b) {
                    if (a.pos < b.pos) return -1;
                    if (a.pos > b.pos) return 1;
                    return 0;
                },
                remove_entry = function (entry) {
                    if (query.length === 1) return query.length = 0; // emitEvent; reset_query
                    query.splice(entry, 1);
                },  
                remove_orphans = function (type) {
                    $parent.removeClass(type ? type : '').find('.extra-opts').remove(); 
                    $source_select.empty().append($($option.html()).text(default_sel_txt));
                },
                reset_query = function () {
                    return $query.text(default_sel_txt), $type_select.val(default_sel_txt).focus(), 
                        $source_select.empty().append($($option.html()).text(default_sel_txt)), remove_entry();
                },
                type_handler = function (entry) {
                    var type = $parent.data('type');
                    if (type_val === default_type_txt){
                        if (menu.opts.length === 1 && query.length === 1) return reset_query();
                        return remove_entry(entry), remove_orphans(type), display_query(), enable_extra_options(false);
                        // emitEvent; type_selected
                    }
                    if ($parent.hasClass(type)) remove_entry(entry), remove_orphans(type), display_query();
                    switch (type_val) {
                        case 'live': populate_livedatasources(); break;
                        case 'snapshot': populate_marketdatasnapshots(); break;
                        case 'historical': populate_historical(); break;
                    }
                },
                source_handler = function (entry) {
                    if (ds_val === default_sel_txt) {
                        return remove_entry(entry), display_query(), enable_extra_options(false);  
                    } 
                    else if (~entry) query[entry] = {pos:sel_pos, type:type_val, ds:ds_val};
                    else query.splice(sel_pos, 0, {pos:sel_pos, type:type_val, ds:ds_val});
                    enable_extra_options(true);
                    display_query(); // emitEvent; data_selected 
                },
                delete_handler = function (entry) {
                    if (menu.opts.length === 1) {
                        return remove_entry(entry), remove_orphans($parent.data('type')), reset_query();
                    }
                    if ($type_select !== undefined) menu.del_handler($parent);
                    if (menu.opts.length) {
                        for (var i = ~entry ? entry : sel_pos, len = query.length; i < len; query[i++].pos -= 1);
                        if (~entry) return remove_entry(entry), display_query(); // emitEvent; opts_repositioned 
                    }
                },
                enable_extra_options = function  (val) {
                    var $inputs = $extra_opts.find('input');
                    if (!$inputs) return;
                    if (val) $inputs.removeAttr('disabled').filter('.latest').addClass('active');
                    else  $inputs.attr('disabled', true).filter('.active').removeClass('active');
                },
                toggle_extra_options = function ($elem) {
                    if($elem.is('.custom')){
                        return $custom = $elem.datepicker({onSelect:calendar_blur}),
                            $custom.addClass('active').siblings('.latest').removeClass('active'), datetime_handler();
                    } else if ($elem.is('.latest')) {
                        return $latest = $elem.addClass('active').siblings('.custom')
                                                .removeClass('active').val('Custom');
                    }
                },
                calendar_blur = function (event) {
                    if (!$custom.datepicker('isDisabled')) {
                        $custom.focus().datepicker('hide');
                    }
                },
                datetime_handler = function () {
                    $custom.datepicker('show');
                },
                menu_handler = function (event) {
                    var elem = $(event.srcElement || event.target), entry;
                    $parent = elem.parents(parent_s);
                    $type_select = $parent.find(type_s);
                    $source_select = $parent.find(source_s);
                    $extra_opts = $parent.find('.extra-opts');
                    type_val = $type_select.val();
                    ds_val = $source_select.val();
                    sel_pos = $parent.data('pos');
                    entry = (query.pluck('pos').indexOf(sel_pos));
                    if (elem.is(menu.$dom.add)) return menu.add_handler();
                    if (elem.is(del_s)) return delete_handler(entry);
                    if (elem.is($type_select)) return type_val = type_val.toLowerCase(), type_handler(entry);
                    if (elem.is($source_select)) return source_handler(entry);
                    if (elem.parent($extra_opts)) return toggle_extra_options(elem);
                };
            if ($dom) {
                $dom.title_infix.append('<span>then</span>'); // Move to DropMenu class
                $query = $('.datasources-query', $dom.title);
                $option = $(wrapper).append('<option>');
                $snapshot_opts = $(wrapper).append([
                    '<div class="extra-opts">',
                        '<span>Versions:</span>',
                        '<input class="latest" type="button" value="Latest" disabled />',
                        '<input class="OG-js-datetimepicker custom" type="button" value="Custom" disabled />',
                        '<span>Correction:</span>',
                        '<input class="latest" type="button" value="Latest" disabled />',
                        '<input class="OG-js-datetimepicker custom" type="button" value="Custom" disabled />',
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
            }
            return menu;
        };
    }
});