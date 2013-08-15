*Status:* Works for me.  No further updates planned.

Rook Reader is a "PDF" reader for the Nook Simple Touch designed for
reading technical papers.  It's simple, fast, and designed for
two-column layouts (but also works for single column text).

The built-in Nook PDF reader is terrible for technical papers: reflow
mode makes a mess of two-column text and figures, while layout mode
shrinks the text into oblivion.  At the same time, standard Android
PDF readers aren't designed for the unique properties of eInk
displays.  And both are simply too slow on the Nook's limited CPU.

Rook takes a split approach.  PDF files are pre-rendered on a real
computer by `conv.py` and Rook Reader simply views these pre-rendered
files.  This makes Rook fast and also means we can put time into
pre-rendering pages nicely.

User interface
--------------

Rook has a full-page view and a thumbnail view.  In thumbnail view,
swipe or use the physical buttons to scroll and tap a page to view it.
In the full-page view, move through pages using either the physical
device buttons or by tapping at the top or bottom of the screen.  From
full-page view, you can re-enter thumbnail view by tapping the
thumbnails icon to the right of the page.

Requirements
------------

On the desktop side, `conv.py` requires Poppler (specifically
poppler-utils) and the ImageMagick command-line tools.  poppler-utils
0.20 or better is recommended, since earlier versions only accounted
for text when cropping the page, which could cut off figures.

Installing Rook Reader requires side-loading an APK, which requires a
rooted Nook.  Rook itself depends on an OpenIntents-enabled file
browser such as OI File Manager.
