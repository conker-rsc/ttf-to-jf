# ttf-to-jf

Converts a .ttf file to the .jf font format

**Note**: Currently only works on Linux or other operating systems utilizing X11

**Note**: Has not been tested with atypical font formats

## usage

```
usage: ttf-to-jf
-in,--input-file <arg>      path to ttf file
-name,--font-name <arg>     font name
-out,--output-path <arg>    output directory
-size,--font-size <arg>     font size
-style,--font-style <arg>   font style
```

## build

Execute `ant dist`

---

Core logic extracted from https://bitbucket.org/eggsampler/rsc/src/master/
