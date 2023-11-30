/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
function inverse($f) {
	for (let $i = 0; $i < $f.elements.length; $i++) {
		if ($f.elements[$i].type === "checkbox") {
			$f.elements[$i].checked = !$f.elements[$i].checked;
		}
	}
	return false;
}

function checkAll($f) {
	for (let $i = 0; $i < $f.elements.length; $i++) {
		if ($f.elements[$i].type === "checkbox") {
			$f.elements[$i].checked = true;
		}
	}
	return false;
}

/**
 * Requires prototype.js (http://www.prototypejs.org/)
 */
Ajax.ImgUpdater = Class.create();
Ajax.ImgUpdater.prototype = {
	initialize: function(imgID, timeout, newSrc) {
		this.img = document.getElementById(imgID);
		if (newSrc) {
			this.src = newSrc;
		} else {
			this.src = this.img.src;
		}
		this.timeout = timeout;
		this.start();
	},

	start: function() {
		var now = new Date();
		this.img.src = this.src + '&t=' + now.getTime();
		this.timer = setTimeout(this.start.bind(this), this.timeout * 1000);
	},

	stop: function() {
		if (this.timer) {
			clearTimeout(this.timer);
		}
	}
}

function togglePanel(container, remember_url) {
	if (Element.getStyle(container, "display") === 'none') {
		if (remember_url) {

		}
		if (document.getElementById('invisible_' + container)) {
			Element.hide('invisible_' + container);
		}
		if (document.getElementById('visible_' + container)) {
			Element.show('visible_' + container);
		}

		Effect.Grow(container);
	} else {
		if (remember_url) {

		}
		if (document.getElementById('visible_' + container)) {
			Element.hide('visible_' + container);
		}
		if (document.getElementById('invisible_' + container)) {
			Element.show('invisible_' + container);
		}
		Effect.Shrink(container);
	}
	return false;
}

function scaleImage(v, minX, maxX, minY, maxY) {
	var images = document.getElementsByClassName('scale-image');
	var w = (maxX - minX) * v + minX;
	var h = (maxY - minY) * v + minY;
	if(v > 0.8) {
		w = w -30;
		h = h - 100;
	}
	for (let i = 0; i < images.length; i++) {
		$(images[i]).setStyle({
			"width": w + 'px',
			"height": h + 'px'
		});
	}
	return v;
}

function toggleAndReloadPanel(container, url) {
	if (Element.getStyle(container, "display") === 'none') {
		Effect.BlindDown(container);
	} else {
		Effect.Shrink(container);
	}
}

function getWindowHeight() {
	var myHeight = 0;
	if (typeof( window.innerHeight ) == 'number') {
		//Non-IE
		myHeight = window.innerHeight;
	} else if (document.documentElement && document.documentElement.clientHeight) {
		//IE 6+ in 'standards compliant mode'
		myHeight = document.documentElement.clientHeight;
	}
	return myHeight;
}

function getWindowWidth() {
	var myWidth = 0;
	if (typeof document.body.clientWidth === 'number' && document.body) {
		// Non-IE or IE 6+ in 'standards compliant mode'
		myWidth = document.body.clientWidth;
	}
	return myWidth;
}

var helpTimerID;

function setupHelpToggle(url) {
	var rules = {
		'li#abbreviations': function(element) {
			element.onclick = function() {
				var help_container = 'help';
				if (Element.getStyle(help_container, "display") === 'none') {
					Element.show(help_container);
				} else {
					Element.hide(help_container);
				}
				Effect.toggle(help_container, 'appear');
				if (helpTimerID) {
					clearTimeout(helpTimerID);
				}
				helpTimerID = setTimeout('Effect.Fade("' + help_container + '")', 15000);
				return false;
			}
		}
	}
	Behaviour.register(rules);
}

function addAjaxTooltip(activator, tooltip, url) {
	Tooltip.closeText = null;
	Tooltip.autoHideTimeout = null;
	Tooltip.showMethod = function(e) {
		Effect.Appear(e, {
			to: 0.9
		});
	}

	Tooltip.add(activator, tooltip);
	var tt_container = $$('#' + tooltip + ' .tt_content')[0];
	Event.observe(activator, 'click', function(e) {

		var t_title = $('tt_title');

		if (t_title) {
			t_title.hide();
		}

		tt_container.style.width = '300px';

		tt_container.innerHTML = '<div class="ajax_activity">&nbsp;</div>';


	});
}
