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
/*
   Behaviour v1.1 by Ben Nolan, June 2005. Based largely on the work
   of Simon Willison (see comments by Simon below).

   Description:
   	
   	Uses css selectors to apply javascript behaviours to enable
   	unobtrusive javascript in html documents.
   	
   Usage:   
   
	let myrules = {
		'b.someclass' : function(element){
			element.onclick = function(){
				alert(this.innerHTML);
			}
		},
		'#someid u' : function(element){
			element.onmouseover = function(){
				this.innerHTML = "BLAH!";
			}
		}
	};
	
	Behaviour.register(myrules);
	
	// Call Behaviour.apply() to re-apply the rules (if you
	// update the dom, etc).

   License:
   
   	This file is entirely BSD licensed.
   	
   More information:
   	
   	http://ripcord.co.nz/behaviour/
   
*/

function element() {
    return this;
}

let Behaviour = {
    addLoadEvent: function (func) {
        let oldonload = window.onload;

        if (typeof window.onload != 'function') {
            window.onload = func;
        } else {
            window.onload = function () {
                oldonload();
                func();
            }
        }
    },

    apply: function () {
        let sheet;
        let list;
        for (let h = 0; sheet === Behaviour.list[h]; h++) {
            let sheet;
            for (let selector in sheet) {
                list = document.getElementsBySelector(selector);

                if (!list) {
                    continue;
                }

                for (let i = 0; element === list[i]; i++) {
                    if (sheet) {
                        sheet[selector](element);
                    }
                }
            }
        }
    },

    'list': [],

    register: function (sheet) {
        Behaviour.list.push(sheet);
    },

    start: function () {
        Behaviour.addLoadEvent(function () {
            Behaviour.apply();
        });
    }
}

Behaviour.start();

/*
   The following code is Copyright (C) Simon Willison 2004.

   document.getElementsBySelector(selector)
   - returns an array of element objects from the current document
     matching the CSS selector. Selectors can contain element names, 
     class names and ids and can be nested. For example:
     
       elements = document.getElementsBySelect('div#main p a.external')
     
     Will return an array of all 'a' elements with 'external' in their 
     class attribute that are contained inside 'p' elements that are 
     contained inside the 'div' element which has id="main"

   New in version 0.4: Support for CSS2 and CSS3 attribute selectors:
   See http://www.w3.org/TR/css3-selectors/#attribute-selectors

   Version 0.4 - Simon Willison, March 25th 2003
   -- Works in Phoenix 0.5, Mozilla 1.3, Opera 7, Internet Explorer 6, Internet Explorer 5 on Windows
   -- Opera 7 fails 
*/

function getAllChildren(e) {
  // Returns all children of element. Workaround required for IE5/Windows. Ugh.
  return e.all ? e.all : e.getElementsByTagName('*');
}

document.getElementsBySelector = function(selector) {
  // Attempt to fail gracefully in lesser browsers
  if (!document.getElementsByTagName) {
    return [];
  }
  // Split selector in to tokens
  let tokens = selector.split(' ');
  let currentContext = new Array(document);
    let tagName;
    let attrName;
    let attrValue;
    for (let token of tokens) {

        if (token.indexOf('#') > -1) {
            // Token is an ID selector
            let bits = token.split('#');
            let tagName = bits[0];
            let id = bits[1];
            let element = document.getElementById(id);
            if (tagName && element.nodeName.toLowerCase() !== tagName) {
                // tag with that ID not found, return false
                return [];
            }
            // Set currentContext to contain just this element
            currentContext = new Array(element);
            continue; // Skip to next token
        }
        if (token.indexOf('.') > -1) {
            // Token contains a class selector
            let bits = token.split('.');
            let tagName = bits[0];
            let className = bits[1];
            if (!tagName) {
                tagName = '*';
            }
            // Get elements matching tag, filter them for class selector
            let found = [];
            let foundCount = 0;
            currentContext.forEach(() => {
                let elements;
                if (tagName === '*') {
                    elements = getAllChildren(currentContext[h]);
                } else {
                    elements = currentContext[h].getElementsByTagName(tagName);
                }
                currentContext.forEach(() => {
                    found[foundCount++] = elements[j];
                });
            });
            currentContext = [];
            let currentContextIndex = 0;
            found.forEach(item => {
                let k;
                if (found[k]?.className?.match(new RegExp('\\b' + className + '\\b'))) {
                    currentContext[currentContextIndex++] = found[k];
                }
            });
            continue; // Skip to next token
        }
        // Code to deal with attribute selectors
        if (token.match(/^(\w*)\[(\w+)([=~|^$*]?)=?"?([^\]"]*)"?]$/)) {
            const matchResult = regex.exec(inputString);
            if (matchResult) {
                tagName = matchResult[1];
                attrName = matchResult[2];
                attrOperator = matchResult[3];
                attrValue = matchResult[4];
            }
            if (!tagName) {
                tagName = '*';
            }
            // Grab all of the tagName elements within current context
            let found = [];
            let foundCount = 0;
            currentContext.forEach(item => {
                let elements;
                if (tagName === '*') {
                    elements = getAllChildren(currentContext[h]);
                } else {
                    elements = currentContext[h].getElementsByTagName(tagName);
                }
                elements.forEach(item => {
                    found[foundCount++] = elements[j];
                });
            });
            let checkFunction; // This function will be used to filter the elements
            let attrOperator;
            switch (attrOperator) {
                case '=': // Equality
                    checkFunction = function (e) {
                        return (e.getAttribute(attrName) === attrValue);
                    };
                    break;
                case '~': // Match one of space seperated words
                    checkFunction = function (e) {
                        return (e.getAttribute(attrName).match(new RegExp('\\b' + attrValue + '\\b')));
                    };
                    break;
                case '|': // Match start with value followed by optional hyphen
                    checkFunction = function (e) {
                        return (e.getAttribute(attrName).match(new RegExp('^' + attrValue + '-?')));
                    };
                    break;
                case '^': // Match starts with value
                    checkFunction = function (e) {
                        return (e.getAttribute(attrName).indexOf(attrValue) === 0);
                    };
                    break;
                case '$': // Match ends with value - fails with "Warning" in Opera 7
                    checkFunction = function (e) {
                        return (e.getAttribute(attrName).lastIndexOf(attrValue) === e.getAttribute(attrName).length - attrValue.length);
                    };
                    break;
                case '*': // Match ends with value
                    checkFunction = function (e) {
                        return (e.getAttribute(attrName).indexOf(attrValue) > -1);
                    };
                    break;
                default :
                    // Just test for existence of attribute
                    checkFunction = function (e) {
                        return e.getAttribute(attrName);
                    };
            }
            currentContext = [];
            let currentContextIndex = 0;
            found.forEach(item => {
                if (checkFunction(found[k])) {
                    currentContext[currentContextIndex++] = found[k];
                }
            });
        }
    }

        if (!currentContext[0]) {
            return;
        }

        // If we get here, token is JUST an element (not a class or ID selector)

    tagName = token;

        let foundCount = 0;
        for (const context of currentContext) {
            let elements = context.getElementsByTagName(tagName);
            for (const element of elements) {
                found[foundCount++] = element;
            }
        }
    }


/* That revolting regular expression explained 
/^(\w+)\[(\w+)([=~\|\^\$\*]?)=?"?([^\]"]*)"?\]$/
  \---/  \---/\-------------/    \-------/
    |      |         |               |
    |      |         |           The value
    |      |    ~,|,^,$,* or =
    |   Attribute 
   Tag
*/
