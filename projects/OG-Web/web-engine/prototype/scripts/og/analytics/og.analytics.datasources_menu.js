
$.register_module({
    name: 'og.analytics.DatasourcesMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), opts = menu.opts, data = menu.data, ds_opts = [], ds_val, 
                type_val, sel_pos, default_type_txt = 'select type...', default_sel_txt = 'select data source...',
                del_s = '.og-icon-delete', options_s = '.OG-dropmenu-options', dummy_s = '<wrapper>', type_s = '.type', 
                ds_s = '.source', select_s = 'select', menu_click_s = 'input, div.og-icon-delete, a.OG-link-add', 
                menu_change_s = select_s, $dom = menu.$dom,  $type_select, $ds_select, $sel_parent, $ds_selection, 
                $sel_opt, $snapshot_opts, $snapshot_opts_tmpl,
                events = {
                    type_switch:'dropmenu:ds:typeswitch'
                },
                populate_type_opts = function (data) {
                    $ds_select.empty().append($($sel_opt.html()).text(default_sel_txt));
                    data.forEach(function (d) {$ds_select.append($($sel_opt.html()).text(d.name || d));});
                },
                populate_marketdatasnapshots = function () {
                    og.api.rest.marketdatasnapshots.get().pipe(function (resp) {
                        populate_type_opts(resp.data[0].snapshots);
                    }).pipe(function () {
                        $ds_select.after($snapshot_opts_tmpl.html());
                    });
                },
                populate_livedatasources = function () {
                    og.api.rest.livedatasources.get().pipe(function (resp) {
                        populate_type_opts(resp.data);
                    });
                },
                process_ds_opts = function () {
                    if(ds_opts.length) {
                        var i = 0, arr = [];
                        ds_opts.sort(sort_opts).forEach(function (entry) { // revisit the need for sorting this..
                            if (i > 0) arr[i++] = $dom.title_infix.html() + " ";
                            arr[i++] = entry;
                        });
                        $ds_selection.html(arr.reduce(function (a, v) {
                            return a += v.type ? v.type + ":" + v.ds : v;
                        }, ''));
                    }
                },
                sort_opts = function (a, b) {
                    if (a.pos < b.pos) return -1;
                    if (a.pos > b.pos) return 1;
                    return 0;
                },
                type_select_handler = function (entry) {
                    if (type_val === '' || type_val === default_type_txt) return;
                    var p_type = $sel_parent.data('previous_type');
                    switch(type_val) {
                        case 'snapshot': populate_marketdatasnapshots(); break;
                        case 'live': populate_livedatasources(); break;
                    }
                    if (p_type) menu.emitEvent(events.type_switch, [{pos:entry, previous:p_type, current:type_val}]);
                    $sel_parent.data('previous_type', type_val).addClass($sel_parent.data('previous_type'));
                },
                ds_select_handler = function (entry) {
                    if (ds_val === default_sel_txt) {
                        remove_entry(entry);
                        if (ds_opts.length === 0) return $ds_selection.html(default_sel_txt);
                    } else if (~entry) ds_opts[entry] = {pos:sel_pos, type:type_val, ds:ds_val};
                    else ds_opts.splice(sel_pos, 0, {pos:sel_pos, type:type_val, ds:ds_val});
                    process_ds_opts();
                },
                remove_entry = function (entry) {
                    if (ds_opts.length === 1) return ds_opts.length = 0;
                    ds_opts.splice(entry, 1);
                },
                remove_orphans = function (obj) {
                    // TODO AG: remove any selected extra options from ds_opts
                    $sel_parent.removeClass(obj.previous);
                    switch (obj.previous) {
                        case 'snapshot' : $sel_parent.find('.extra-opts').remove();
                    }
                },
                add_handler = function () {
                    menu.add_handler(); 
                },
                del_handler = function (entry) {
                    if($type_select !== undefined) menu.del_handler($sel_parent);
                    if (menu.opts.length === 1 || menu.opts.length > 1 && ds_opts.length === 1) {
                        return $ds_selection.text(default_sel_txt), $type_select.val(default_sel_txt).focus(), 
                            $ds_select.empty().append($($sel_opt.html()).text(default_sel_txt)), remove_entry();
                    }
                    for (var i = ~entry ? entry : sel_pos, len = ds_opts.length; i < len; ds_opts[i++].pos-=1);
                    if (~entry) {
                        remove_entry(entry);
                        process_ds_opts();
                    }
                },
                menu_handler = function (event) {
                    var target = event.srcElement || event.target,
                        elem = $(target), entry;
                    $sel_parent = elem.parents(options_s);
                    $type_select = $sel_parent.find(type_s);
                    $ds_select = $sel_parent.find(ds_s);
                    type_val = $type_select.val();
                    ds_val = $ds_select.val();
                    sel_pos = $sel_parent.data('pos');
                    entry = (ds_opts.pluck('pos').indexOf(sel_pos));
                    if (elem.is(menu.$dom.add)) return add_handler();
                    if (elem.is(del_s)) return  del_handler(entry);
                    if (elem.is(select_s+'.'+type_s)) 
                        return type_val = type_val.toLowerCase(), type_select_handler(entry);
                    if (elem.is(select_s+'.'+ds_s)) return ds_select_handler(entry);
                };
            if ($dom) {
                $dom.title_infix.append('<span>then</span>'); // Move to DropMenu class
                $ds_selection = $('.datasources-selection', $dom.title);
                $sel_opt = $(dummy_s).append('<option>');
                $snapshot_opts_tmpl = $(dummy_s).append([
                    '<div class="extra-opts">',
                        '<span>Versions:</span>',
                        '<button class="latest active">Latest</button>', 
                        '<button class="custom">Custom</button>',
                        '<span>Correction:</span>',
                        '<button class="latest active">Latest</button>', 
                        '<button class="custom">Custom</button>',
                    '</div>'
                ].join(''));
                if ($dom.title) $dom.title.on('click', menu.title_handler.bind(menu));
                if ($dom.menu) {
                    $dom.menu.on('click', menu_click_s, menu_handler).on('change', menu_handler);
                }
            }
            menu.addListener(events.type_switch, remove_orphans);
            return menu;
        };
    }
});