package com.github.axet.lookup;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.axet.lookup.common.FontFamily;
import com.github.axet.lookup.common.FontSymbol;
import com.github.axet.lookup.common.FontSymbolLookup;
import com.github.axet.lookup.common.GPoint;
import com.github.axet.lookup.common.ImageBinaryGrey;
import com.github.axet.lookup.proc.CannyEdgeDetector;
import com.github.axet.lookup.proc.NCC;

public class OCRCore {

    static public int compareBigFirst(int o1, int o2, int val) {
        if (Math.abs(o1 - o2) < val)
            return 0;

        return compareBigFirst(o1, o2);
    }

    static public int compareBigFirst(int o1, int o2) {
        return new Integer(o2).compareTo(new Integer(o1));
    }

    static public int compareBigFirst(double o1, double o2) {
        return new Double(o2).compareTo(new Double(o1));
    }

    static public int compareSmallFirst(double o1, double o2) {
        return new Double(o1).compareTo(new Double(o2));
    }

    static class BiggerFirst implements Comparator<FontSymbolLookup> {

        @Override
        public int compare(FontSymbolLookup arg0, FontSymbolLookup arg1) {
            int o = Math.max(arg0.size(), arg1.size());

            int r = OCRCore.compareBigFirst(arg0.size(), arg1.size(), o / 2);

            // beeter qulity goes first
            if (r == 0)
                r = OCRCore.compareBigFirst(arg0.g, arg1.g);

            if (r == 0)
                r = OCRCore.compareBigFirst(arg0.size(), arg1.size());

            return r;
        }
    }

    class Left2Right implements Comparator<FontSymbolLookup> {

        @Override
        public int compare(FontSymbolLookup arg0, FontSymbolLookup arg1) {
            int r = 0;

            if (r == 0) {
                if (!arg0.yCross(arg1))
                    r = OCRCore.compareSmallFirst(arg0.y, arg1.y);
            }

            if (r == 0)
                r = OCRCore.compareSmallFirst(arg0.x, arg1.x);

            if (r == 0)
                r = OCRCore.compareSmallFirst(arg0.y, arg1.y);

            return r;
        }
    }

    Map<String, FontFamily> fontFamily = new HashMap<String, FontFamily>();

    CannyEdgeDetector detector = new CannyEdgeDetector();

    // 1.0f == exact match, -1.0f - completely different images
    float threshold = 0.70f;

    public OCRCore() {
        detector.setLowThreshold(3f);
        detector.setHighThreshold(3f);
        detector.setGaussianKernelWidth(2);
        detector.setGaussianKernelRadius(1f);
    }

    List<FontSymbol> getSymbols() {
        List<FontSymbol> list = new ArrayList<FontSymbol>();

        for (FontFamily f : fontFamily.values()) {
            list.addAll(f);
        }

        return list;
    }

    List<FontSymbol> getSymbols(String fontFamily) {
        return this.fontFamily.get(fontFamily);
    }

    List<FontSymbolLookup> findAll(List<FontSymbol> list, ImageBinaryGrey bi) {
        return findAll(list, bi, 0, 0, bi.getWidth(), bi.getHeight());
    }

    List<FontSymbolLookup> findAll(List<FontSymbol> list, ImageBinaryGrey bi, int x1, int y1, int x2, int y2) {
        List<FontSymbolLookup> l = new ArrayList<FontSymbolLookup>();

        for (FontSymbol fs : list) {
            List<GPoint> ll = NCC.lookupAll(bi, x1, y1, x2, y2, fs.image, threshold);
            for (GPoint p : ll)
                l.add(new FontSymbolLookup(fs, p.x, p.y, p.g));
        }

        return l;
    }

}
