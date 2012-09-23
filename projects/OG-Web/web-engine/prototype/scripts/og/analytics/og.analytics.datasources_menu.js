
$.register_module({
    name: 'og.analytics.DatasourcesMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), $dom = menu.$dom;
            $dom.title.on('click', menu.title_handler.bind(menu));
            $dom.menu.on('click', menu.menu_handler.bind(menu));
            return menu;
        }
    }
});