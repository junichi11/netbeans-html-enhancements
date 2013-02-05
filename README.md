# About
This is NetBeans Plugin

## Features

- width and height completion for img tag
- insert as HTML action
- update image size action

### Width And Height Completion for img Tag
You must write src attribute before with and height attributes.

```html
<img src="../../../assets/img/pic.png" width="[ctrl + space]" height="200"/>
<img src="http://junichi11.com/foo/img/pic.jpg" width="[ctrl + space]" />
```
If don't work well, please, push esc key.

### Insert As HTML Action (Image files)
Support for multiple image files.

1. move caret to position that you want to insert on Editor
2. choose image files on Project pane (order in which you want to insert)
3. Right-click > Insert as HTML

```html
<img src="../../../assets/img/pic1.png" width="100" height="200"/>
<img src="../../../assets/img/pic2.png" width="100" height="200"/>
```

### Update Image Size Action
You can update/insert image size(width/height) of img tag.  
This feature is not available as default. So you have to set shortcut key to KeyMap.  
Please search "Update" on KeyMap Option(Tools > Options > KeyMap)

## License
[Common Development and Distribution License (CDDL) v1.0 and GNU General Public License (GPL) v2](http://netbeans.org/cddl-gplv2.html)