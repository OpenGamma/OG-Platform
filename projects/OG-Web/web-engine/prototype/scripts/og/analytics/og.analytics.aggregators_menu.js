
$.register_module({
    name: 'og.analytics.AggregatorsMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), $dom = menu.$dom, opts = menu.opts, data = menu.data,
                menu_handler = function (event) {
                    if (data.length === opts.length) return;
                    var elem = $(event.target);
                    if (elem.is(menu.$dom.add)) {
                        menu.add_handler(); 
                        menu.stop(event);
                    }
                    if (elem.is('.og-icon-delete')) {
                        menu.del_handler(elem.closest('.OG-dropmenu-options'));
                        menu.stop(event);
                    }
                    if (elem.is(':checkbox')) {elem.focus();}
                };
            $dom.menu.on('click', menu_handler);
            return menu;
        }
    }
});