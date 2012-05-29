#!/usr/bin/python

import sys, os, subprocess, tempfile, glob, zipfile, shutil
import xml.etree.ElementTree as ET

class Bounds(object):
    def __init__(self, t, l, b, r):
        self.t, self.l, self.b, self.r = t, l, b, r
        self.w = self.r-self.l
        self.h = self.t-self.b

    @staticmethod
    def fromObj(obj, page):
        # Compute the PDF bounds from a text or page object.
        pageProps = Bounds.__objProps(page)
        objProps = Bounds.__objProps(obj)

        return Bounds(pageProps[2] - objProps[0],
                      objProps[1],
                      pageProps[2] - objProps[2],
                      objProps[3])

    @staticmethod
    def __objProps(obj):
        t, l = int(obj.get("top")), int(obj.get("left"))
        w, h = int(obj.get("width")), int(obj.get("height"))
        return t, l, t+h, l+w

    def __or__(self, o):
        return Bounds(max(self.t, o.t), min(self.l, o.l),
                      min(self.b, o.b), max(self.r, o.r))

class PDFInfo(object):
    def __init__(self, srcPath):
        p = subprocess.Popen(["pdftohtml", "-stdout", "-enc", "UTF-8", "-xml",
                              "-zoom", "1", srcPath], stdout=subprocess.PIPE)
        # For some PDF's pdftohtml generates control characters in
        # text nodes, which Expat rejects.  We don't actually care, so
        # strip them out.
        data = p.stdout.read().decode("utf8")
        data = data.translate(dict((c, None) for c in range(ord(' '))))
        # ElementTree seems to expect *encoded* data (or at least it
        # barfs on any character above chr(127)).
        self.__xml = ET.fromstring(data.encode("utf8"))

    # XXX Silly interface
    def getBounds(self):
        for page in self.__xml.findall("page"):
            bound = None
            for obj in page:
                # Note: 'image' objects require poppler-utils 0.20 or
                # later
                if obj.tag not in ("text", "image"):
                    continue
                if obj.text and obj.text.isdigit():
                    # Lame test to cut off page numbers.  Should check
                    # that it's the sole, bottom-most text, too.
                    continue
                bounds = Bounds.fromObj(obj, page)
                if bound:
                    bound |= bounds
                else:
                    bound = bounds
            pageBounds = Bounds.fromObj(page, page)
            yield pageBounds, bound or pageBounds

def pdfToRook(srcPath, outPath):
    # We add a smidgen of top/bottom margin to make it more obvious
    # which frame of a page you're viewing.
    MARGIN = 72/4
    TEXT_WIDTH = 800

    outDir = tempfile.mkdtemp()

    # XXX Use a consistent resolution?

    info = PDFInfo(srcPath)
    for i, (pageBounds, textBounds) in enumerate(info.getBounds()):
        sys.stdout.write(".")
        sys.stdout.flush()

        if pageBounds != textBounds:
            textBounds.t += MARGIN
            textBounds.b -= MARGIN
            textBounds.h += MARGIN*2

        # If textBounds.w is WIDTH pixels, how wide is the page?
        pageWidth = pageBounds.w * TEXT_WIDTH / textBounds.w
        # Maintaining aspect ratio, how tall is the page?
        pageHeight = pageBounds.h * pageWidth / pageBounds.w
        # Similarly, how tall is the text?
        textHeight = textBounds.h * TEXT_WIDTH / textBounds.w
        # Compute the crop corner by scaling points to pixels
        cropX = textBounds.l * TEXT_WIDTH / textBounds.w
        cropY = (pageBounds.t - textBounds.t) * TEXT_WIDTH / textBounds.w

        # Extract page
        subprocess.check_call(
            ["pdftoppm", "-gray", "-f", str(i+1), "-l", str(i+1),
             "-scale-to-x", str(pageWidth), "-scale-to-y", str(pageHeight),
             "-W", str(TEXT_WIDTH), "-H", str(textHeight),
             "-x", str(cropX), "-y", str(cropY),
             srcPath, os.path.join(outDir, "page")])

    # Convert to 4-bit PNG
    sys.stdout.write("*")
    sys.stdout.flush()
    subprocess.check_call(
        ["mogrify", "-type", "Grayscale", "-depth", "4", "-format", "png"]
        + glob.glob(os.path.join(outDir, "page-*.pgm")))

    # Create Rook ZIP
    sys.stdout.write("*")
    out = zipfile.ZipFile(outPath, "w")
    for page in glob.glob(os.path.join(outDir, "page-*.png")):
        out.write(page, os.path.basename(page), zipfile.ZIP_STORED)
    out.close()

    shutil.rmtree(outDir)
    print

if len(sys.argv) == 1:
    print >> sys.stderr, "%s pdf.." % sys.argv[0]
    sys.exit(2)

for srcPath in sys.argv[1:]:
    srcPathParts = os.path.splitext(srcPath)
    outPath = srcPathParts[0] + ".rook"

    print srcPath, "=>", os.path.basename(outPath),
    sys.stdout.flush()
    pdfToRook(srcPath, outPath)
