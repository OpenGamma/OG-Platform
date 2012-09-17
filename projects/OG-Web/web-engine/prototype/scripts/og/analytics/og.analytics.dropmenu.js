
$.register_module({
    name: 'og.analytics.dropmenu',
    dependencies: [],
    obj: function () { 
        return function (config) {
            var Dropmenu = this, Menu, data = config.data || {}, events = {
                    focus: 'dropmenu:focus',
                    focused:'dropmenu:focused',
                    open: 'dropmenu:open',
                    opened: 'dropmenu:opened',
                    close: 'dropmenu:close',
                    closed: 'dropmenu:closed'
                },
                title_click =  function (event) {
                    event.stopPropagation();
                    Dropmenu.state === 'open' ? Dropmenu.close() : Dropmenu.open().focus();
                },
                add_click = function (event) {
                    event.stopPropagation();
                };
            return Menu = function () {
                var menu = this;
                return menu.state = 'closed', menu.opened = false, tmpl = config.tmpl, data = config.data,
                    menu.$cntr = config.$cntr.html($((Handlebars.compile(tmpl))(data))),
                    menu.$title = $('.og-option-title', menu.$cntr).on('click', title_click),
                    menu.$menu = $('.OG-analytics-form-menu', menu.$cntr), 
                    menu.$add = $('OG-link-add', menu.$menu).on('click', add_click),
                    menu.$opts = $('.OG-dropmenu-options', menu.$menu),
                    menu.focus = function () {
                        return menu.$menu.find('select').first().focus(), menu.state = 'focused',
                            menu.emitEvent(events.focused, [menu]), menu;
                    },
                    menu.open = function () {
                        return menu.$menu.show(), menu.state = 'open', menu.opened = true,
                            menu.$title.addClass('og-active'), menu.emitEvent(events.opened, [menu]), menu;
                    },
                    menu.close = function () {
                        return (menu.$menu ? menu.$menu.hide() : null), menu.state = 'closed', menu.opened = false,
                            menu.$title.removeClass('og-active'), menu.emitEvent(events.closed, [menu]), menu;
                    },
                    menu.addListener(events.open, menu.open),
                    menu.addListener(events.close, menu.close),
                    menu.addListener(events.focus, menu.focus),
                    menu;
            }, Menu.prototype = EventEmitter.prototype, Dropmenu = new Menu();
        }
    }
});