# About

This is NetBeans Plugin for small HTML features.

## Features

- width and height completion for a `img` tag
- insert as HTML action
- update image size action

### Width And Height Completion for img Tag

You must write src attribute before with and height attributes.

```html
<img src="../../../assets/img/pic.png" width="[ctrl + space]" height="200"/>
<img src="http://junichi11.com/foo/img/pic.jpg" width="[ctrl + space]" />
```
If don't work well, please, push <kbd>esc</kbd> key.

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

You can update/insert image size(width/height) of a img tag.  
Default shortcut is <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>U</kbd>.  
If you would like to change shortcut, Please search "Update" on KeyMap Option(Tools > Options > KeyMap).

## Donation

<a href="https://github.com/sponsors/junichi11">https://github.com/sponsors/junichi11</a>

## License

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
