
$.register_module({
    name: 'og.analytics.dropmenu',
    dependencies: [],
    obj: function () { 
        return function (config) {
            var emitter = new EventEmitter(), Dropmenu, events = {
                    focus: 'dropmenu:focus',
                    focused:'dropmenu:focused',
                    open: 'dropmenu:open',
                    opened: 'dropmenu:opened',
                    close: 'dropmenu:close',
                    closed: 'dropmenu:closed',
                    closeall: 'dropmenu:closeall'
                };
            return Dropmenu = function () {
                var Dropmenu = this, Menu;
                return Menu = function () {
                    var menu = this, cta_click =  function (event) {
                            event.stopPropagation();
                            Dropmenu.state === 'open' ? Dropmenu.close() : Dropmenu.open().focus();
                        }, cta = config.$cta.on('click', cta_click);
                    return menu.state = 'closed', menu.opened = false, menu.$el = config.$menu,
                        menu.focus = function () {
                            return menu.$el.find('select').first().focus(), menu.state = 'focused',
                                menu.emitEvent(events.focused, [menu]), menu;
                        },
                        menu.open = function () {
                            return menu.$el.show().blurkill(menu.close), menu.state = 'open', menu.opened = true,
                                cta.$el.addClass('og-active'), menu.emitEvent(events.opened, [menu]), menu;
                        },
                        menu.close = function () {
                            return (menu.$el ? menu.$el.hide() : null), menu.state = 'closed', menu.opened = false,
                                cta.$el.removeClass('og-active'), menu.emitEvent(events.closed, [menu]), menu;
                        },
                        menu.addListener(events.open, menu.open),
                        menu.addListener(events.close, menu.close),
                        menu.addListener(events.focus, menu.focus),
                        menu;
                }, Menu.prototype = EventEmitter.prototype, Dropmenu = new Menu();
            };
        }
    }
});