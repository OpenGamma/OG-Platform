/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.DropMenu',
    dependencies: ['og.common.util.ui.DropMenu'],
    obj: function () {
        var events = {
                queryselected: 'dropmenu:queryselected',
                querycancelled: 'dropmenu:querycancelled'
            },
            DropMenu = function (config) {
                /**
                * TODO AG: Must provide Getters/Setters for static instance properties as these should
                * really be private and not accessible directly via the instance.
                */
                var menu = new og.common.util.ui.DropMenu(config), dummy_s = '<wrapper>';
                menu.$dom.toggle_prefix = $(dummy_s);
                menu.$dom.toggle_infix = $(dummy_s).append('<span>then</span>');
                if (menu.$dom.toggle) menu.$dom.toggle.on('mousedown', menu.toggle_menu.bind(menu));
                if (menu.$dom.menu) {
                    menu.$dom.menu_actions = $('.og-menu-actions', menu.$dom.menu);
                    menu.$dom.opt = $('.OG-dropmenu-options', menu.$dom.menu);
                    menu.$dom.opt.data('pos', ((menu.opts = []).push(menu.$dom.opt), menu.opts.length-1));
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
        DropMenu.prototype.add_handler = function () {
            var len, opt;
            return len = this.opts.length, opt = this.$dom.opt_cp.clone(true).data("pos", this.opts.length),
                this.opts.push(opt), this.$dom.add.focus(0), this.opts[len].find('.number span').text(this.opts.length),
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
            if (val === 'OK') this.emitEvent(this.events.close).emitEvent(events.queryselected, [this]);
            else if (val === 'Cancel') this.emitEvent(this.events.close).emitEvent(events.querycancelled, [this]);
        };
        DropMenu.prototype.capitalize = function (string) {
            return string.charAt(0).toUpperCase() + string.slice(1);
        };
        return DropMenu;
    }
});