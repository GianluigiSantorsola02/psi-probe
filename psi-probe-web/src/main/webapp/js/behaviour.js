

function element() {
    return this;
}

function handleAttributeSelector(tagName, attrName, attrOperator, attrValue, currentContext) {
    let elements = [];
    for (let element of currentContext) {
        if (element.getAttribute(attrName) === attrValue) {
            elements.push(element);
        }
    }
    return elements;
}

function handleToken(token, currentContext) {
    let tagName;

    if (token.indexOf('#') > -1) {
        // Token is an ID selector
        let bits = token.split('#');
        tagName = bits[0];
        let id = bits[1];
        let element = document.getElementById(id);
        if (tagName && element.nodeName.toLowerCase() !== tagName) {
            // tag with that ID not found, return false
            return [];
        }
        // Set currentContext to contain just this element
        currentContext = [element];
        return currentContext;
    }

    handleClassSelector(token, currentContext);

    // Code to deal with attribute selectors
    if (token.match(/^(\w*)\[(\w+)([=~|^$*]?)=?"?([^\]"]*)"?]$/)) {
        const matchResult = token.match(/^(\w+)\[(\w+)([=~|^$*]?)="?([^"\]]*)"?$/);
        if (matchResult ?. matchResult[1]) {
            tagName = matchResult[1];
            const attrName = matchResult[2];
            const attrOperator = matchResult[3];
            const attrValue = matchResult[4];
            handleAttributeSelector(tagName, attrName, attrOperator, attrValue, currentContext);
        }
    }

    return currentContext;
}
function createCheckFunction(attrOperator, attrName, attrValue) {
    let checkFunction;

    switch (attrOperator) {
        case '=': // Equality
            checkFunction = function(e) {
                return e.getAttribute(attrName) === attrValue;
            };
            break;
        case '~': // Match one of space separated words
            checkFunction = function(e) {
                return e.getAttribute(attrName).match(new RegExp('\\b' + attrValue + '\\b'));
            };
            break;
        case '|': // Match start with value followed by optional hyphen
            checkFunction = function(e) {
                return e.getAttribute(attrName).match(new RegExp('^' + attrValue + '-?'));
            };
            break;
        case '^': // Match starts with value
            checkFunction = function(e) {
                return e.getAttribute(attrName).indexOf(attrValue) === 0;
            };
            break;
        case '$': // Match ends with value - fails with "Warning" in Opera 7
            checkFunction = function(e) {
                return e.getAttribute(attrName).lastIndexOf(attrValue) === e.getAttribute(attrName).length - attrValue.length;
            };
            break;
        case '*': // Match ends with value
            checkFunction = function(e) {
                return e.getAttribute(attrName).indexOf(attrValue) > -1;
            };
            break;
        default:
            // Just test for existence of attribute
            checkFunction = function(e) {
                return e.getAttribute(attrName);
            };
    }

    return checkFunction;
}
function handleClassSelector(token, currentContext) {
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
        found.forEach(() => {
            let k;
            if (found[k]?.className?.match(new RegExp('\\b' + className + '\\b'))) {
                currentContext[currentContextIndex++] = found[k];
            }
        });
        return currentContext;
    }
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
        let sheet = Behaviour.list[0];
        let list;
        for (let h = 0; sheet === Behaviour.list[h]; h++) {
            let sheet = Behaviour.list[h];
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

        handleToken(token, currentContext);
            // Grab all the tagName elements within current context
            let found = [];
            let foundCount = 0;
            currentContext.forEach(() => {
                let elements;
                if (tagName === '*') {
                    elements = getAllChildren(currentContext[h]);
                } else {
                    elements = currentContext[h].getElementsByTagName(tagName);
                }
                elements.forEach(() => {
                    found[foundCount++] = elements[j];
                });
            });
            let checkFunction; // This function will be used to filter the elements
            let attrOperator = attrOperator;
            createCheckFunction(attrOperator, attrName, attrValue);
            currentContext = [];
            let currentContextIndex = 0;
            found.forEach(() => {
                if (checkFunction(found[k])) {
                    currentContext[currentContextIndex++] = found[k];
                }
            });
        }
    }

        // If we get here, token is JUST an element (not a class or ID selector)

    let tagName = token;

        let foundCount = 0;
        for (const context of currentContext) {
            let elements = context.getElementsByTagName(tagName);
            for (const element of elements) {
                found[foundCount++] = element;
            }

    }
