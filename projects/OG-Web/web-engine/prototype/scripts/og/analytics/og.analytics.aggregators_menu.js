
$.register_module({
    name: 'og.analytics.AggregatorsMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), $dom = menu.$dom, opts = menu.opts, data = menu.data,
                ag_opts = [], $ag_selection = $('.aggregation-selection', $dom.title),
                default_sel_txt = 'select aggregation type...', options_s = '.OG-dropmenu-options';
                $dom.title_prefix.append('<span>Aggregated by</span>');
                $dom.title_infix.append('<span>then</span>');
                process_ag_opts = function () {
                    var i = 0, arr = [], query, str;
                    ag_opts.sort(sort_ag_opts).forEach(function (entry) {
                        if (i > 0) arr[i++] = $dom.title_infix.html() + " ";
                        arr[i++] = entry;
                    });
                    query = arr.reduce(function (a, v) {return a += v.val ? v.val : v;}, '');
                    $ag_selection.html(query);
                },
                sort_ag_opts = function (a, b) {
                    if (a.pos < b.pos) return -1;
                    if (a.pos > b.pos) return 1;
                    return 0;
                }
                replace_entry = function (pos, val) {
                    ag_opts[pos].val = val;
                },
                remove_entry = function (pos) {
                    if (ag_opts.length === 1) return $ag_selection.text(default_sel_txt), ag_opts = [];
                    ag_opts.splice(pos, 1);
                },
                add_infix = function (array) {
                    var arr = this;
                    if (arr.length > 1) arr.splice(-1, 0, $dom.title_infix.html());
                },
                del_handler = function (elem) {
                    var parent = elem.parents(options_s), pos = parent.data('pos'), 
                        entry = ag_opts.pluck('pos').indexOf(pos);
                    menu.del_handler(parent);
                    remove_entry(entry);
                    process_ag_opts();
                },
                add_handler = function () {
                    if (data.length === opts.length) return;
                    menu.add_handler(); 
                };
                select_handler = function (elem) {
                    var parent = elem.parents(options_s), pos = parent.data('pos'), text, val = elem.val(),
                        entry = ag_opts.pluck('pos').indexOf(pos);
                    if (val === default_sel_txt) remove_entry(entry);
                    else if (entry !== -1) replace_entry(entry, val);
                    else ag_opts.splice(pos, 0, {pos:pos, val:val});
                    process_ag_opts();
                },
                /*checkbox_handler = function (elem) {
                     var parent = elem.closest(options_s), pos = parent.data('data'),
                        entry = ag_opts.pluck('pos').indexOf(pos);
                    if (elem.checked && entry === -1) ag_opts[pos].required_field = true; // probably use add_infix here!?
                    else if (elem.checked && entry !== -1) ag_opts[entry].required_field = true;
                    elem.focus();
                },*/
                menu_handler = function (event) {
                    var elem = $(event.target);
                    if (elem.is(menu.$dom.add)) add_handler();
                    if (elem.is('.og-icon-delete')) del_handler(elem);
                    if (elem.is(':checkbox')) checkbox_handler(elem);
                    if (elem.is('select')) select_handler(elem);
                    menu.stop(event);
                };
            $dom.title.on('click', menu.title_handler.bind(menu));
            $dom.menu.on('click', menu_handler).on('change', menu_handler);
            return menu;
        };
    }
});