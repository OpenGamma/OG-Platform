/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.DropMenu',
    dependencies: ['og.common.util.ui.DropMenu'],
    obj: function () {
        var events = {
                selected: 'selected',
                cancelled: 'cancelled'
            },
            DropMenu = function (config) {
                var menu = new og.common.util.ui.DropMenu(), dummy_s = '<wrapper>';
                menu.opts = [];
                menu.$dom.toggle_prefix = $(dummy_s);
                menu.$dom.toggle_infix = $(dummy_s).append('<span>then</span>');
                menu.$dom.cntr = config.cntr;
                menu.$dom.toggle = $('.og-menu-toggle', menu.$dom.cntr);
                menu.$dom.menu = $('.og-menu', menu.$dom.cntr);
                if (menu.$dom.toggle) menu.$dom.toggle.on('click', menu.toggle_menu.bind(menu));
                if (menu.$dom.menu) {
                    menu.$dom.menu_actions = $('.og-menu-actions', menu.$dom.menu);
                    menu.$dom.opt = $('.OG-dropmenu-options', menu.$dom.menu);
                    menu.$dom.opt.each(function (idx, elem){ // TODO AG: better solution needed to store type
                        var $elem = $(elem), cl = $elem.attr('class').replace(/\s*OG-dropmenu-options\s*/, "");
                        $elem.find('.number span').text(idx+1);
                        menu.opts.push($elem.data({ 'pos': idx, 'type': cl || null }));
                    });
                    menu.$dom.add = $('.OG-link-add', menu.$dom.menu);
                    menu.$dom.opt_cp = menu.$dom.opt.clone(true);
                }
                return menu;
            };
        DropMenu.prototype = og.common.util.ui.DropMenu.prototype;
        DropMenu.prototype.toggle_menu = function (event){
            this.toggle_handler();
            if (this.opened) this.opts[this.opts.length-1].find('select').first().focus(0);
        };
        DropMenu.prototype.add_handler = function (html) {
            var len = this.opts.length, opt = html.data("pos", this.opts.length);
            this.opts.push(opt);
            this.$dom.add.focus(0);
            this.opts[len].find('.number span').text(this.opts.length);
            this.$dom.menu_actions.before(this.opts[len]);
        };
        DropMenu.prototype.delete_handler = function (elem) {
            var data = elem.data();
            if (this.opts.length === 1) return;
            this.opts.splice(data.pos, 1);
            elem.remove();
            this.update_opt_nums(data.pos);
            if (data.pos < this.opts.length) this.opts[data.pos].find('select').first().focus();
            else this.opts[data.pos-1].find('select').first().focus();
        };
        DropMenu.prototype.update_opt_nums = function (pos) {
            for (var i = pos || 0, len = this.opts.length; i < len;)
                this.opts[i].data('pos', i).find('.number span').text(i+=1);
        };
        DropMenu.prototype.sort_opts = function (a, b) {
            return a.pos === b.pos ? 0 : (a.pos < b.pos ? -1 : 1);
        };
        DropMenu.prototype.button_handler = function (val) {
            if (val === 'OK') {
                this.fire(this.events.close);
                this.fire(events.queryselected);
            } else if (val === 'Cancel') {
                this.fire(this.events.close);
                this.fire(events.querycancelled);
            }
        };
        DropMenu.prototype.capitalize = function (string) {
            return string.charAt(0).toUpperCase() + string.slice(1);
        };
        return DropMenu;
    }
});