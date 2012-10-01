
$.register_module({
    name: 'og.analytics.DatasourcesMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), $dom = menu.$dom, opts = menu.opts, data = menu.data,
                ds_opts = [], $ds_selection = $('.datasources-selection', $dom.title), ds_val, type_val, sel_pos,
                $sel_parent, $type_select, $ds_select, $sel_checkbox, default_sel_txt = 'select data source...',
                del_s = '.og-icon-delete', options_s = '.OG-dropmenu-options', type_s = '.type', ds_s = '.source',
                select_s = 'select', checkbox_s = '.og-option :checkbox', $sel_opt = $('<option>').wrap('<div>'),
                populate_marketdatasnapshots = function () {
                    if (ds_opts[type_val] === undefined) {
                        og.api.rest.marketdatasnapshots.get().pipe(function (resp) {
                            if (resp) ds_opts[type_val] = resp;
                        });
                    }
                },
                populate_livedatasources = function () {
                    if (ds_opts[type_val] === undefined) {
                        og.api.rest.livedatasources.get().pipe(function (resp) {
                            if (resp) ds_opts[type_val] = resp;
                        });
                    }
                },
                select_handler = function (entry) {
                    switch(type_val) {
                        case 'Snapshot': populate_marketdatasnapshots(); break;
                        case 'Live': populate_livedatasources(); break;
                    }
                },
                menu_handler = function (event) {
                    var target = event.srcElement || event.target, //eh!?
                        elem = $(target), entry;
                        $sel_parent = elem.parents(options_s);
                        $type_select = $sel_parent.find(type_s);
                        $ds_select = $sel_parent.find(ds_s);
                        $sel_checkbox = $sel_parent.find(checkbox_s);
                        type_val = $type_select.val();
                        ds_val = $ds_select.val();
                        sel_pos = $sel_parent.data('pos');
                        entry = ds_opts.pluck('pos').indexOf(sel_pos);
                    if (elem.is(menu.$dom.add)) return add_handler();
                    if (elem.is(del_s)) return  del_handler(entry);
                    if (elem.is($sel_checkbox)) return checkbox_handler(entry); 
                    if (elem.is(select_s)) return select_handler(entry);
                };
            if ($dom) {
                if ($dom.title) $dom.title.on('click', menu.title_handler.bind(menu));
                if ($dom.menu) $dom.menu.on('click', menu_handler).on('change', menu_handler);
            }
            return menu;
        };
    }
});