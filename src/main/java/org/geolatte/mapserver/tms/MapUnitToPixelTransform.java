/*
 * Copyright 2009-2010  Geovise BVBA, QMINO BVBA
 *
 * This file is part of GeoLatte Mapserver.
 *
 * GeoLatte Mapserver is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoLatte Mapserver is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoLatte Mapserver.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geolatte.mapserver.tms;

import org.geolatte.mapserver.util.BoundingBox;
import org.geolatte.mapserver.util.Pixel;
import org.geolatte.mapserver.util.PixelRange;
import org.geolatte.mapserver.util.Point;

/**
 * Transforms between coordinates in map units and pixel-coordinates.
 * <p/>
 * <p>
 * A <code>MapUnitToPixelTransform</code> associates a <code>BoundingBox</code> in map units with a
 * <code>PixelRange</code>, such that the upper-left corner of the <code>BoundingBox</code> is mapped
 * to the pixel (minPixelX, minPixelY), and the bottom-right corner to (maxPixelX, maxPixelY).
 * </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jul 8, 2010
 */
public class MapUnitToPixelTransform {
    private final BoundingBox extent;
    private final double mapUnitsPerPixelX;
    private final double mapUnitsPerPixelY;
    private final PixelRange pixelRange;

    /**
     * Constructor
     * @param extent the maximum extent in map units
     * @param pixelRange the maximum extent in pixel coordinates
     */
    public MapUnitToPixelTransform(BoundingBox extent, PixelRange pixelRange) {
        this.extent = extent;
        this.mapUnitsPerPixelX = extent.getWidth() / pixelRange.getWidth();
        this.mapUnitsPerPixelY = extent.getHeight() / pixelRange.getHeight();
        this.pixelRange = pixelRange;
    }

    /**
     * Constructor
     * @param extent the maximum extent in map units
     * @param minPixelX the minimum pixel coordinate in the X-axis
     * @param minPixelY the minimum pixel coordinate in the Y-axis
     * @param mapUnitsPerPixel the map units per pixel for the <code>PixelRange</code>
     */
    public MapUnitToPixelTransform(BoundingBox extent, int minPixelX, int minPixelY, double mapUnitsPerPixel) {
        this.extent = extent;
        this.mapUnitsPerPixelX = mapUnitsPerPixel;
        this.mapUnitsPerPixelY = mapUnitsPerPixel;
        int pixelRangeWidth = (int) Math.ceil(this.extent.getWidth() / mapUnitsPerPixel);
        int pixelRangeHeight = (int) Math.ceil(this.extent.getHeight() / mapUnitsPerPixel);
        this.pixelRange = new PixelRange(minPixelX, minPixelY, pixelRangeWidth, pixelRangeHeight);
    }

    /**
     * Constructor
     * @param extent extent the maximum extent in map units
     * @param mapUnitsPerPixel  the map units per pixel for the <code>PixelRange</code>
     */
    public MapUnitToPixelTransform(BoundingBox extent, double mapUnitsPerPixel) {
        this(extent, 0, 0, mapUnitsPerPixel);
    }

    /**
     * Maps a pixel to a point in this instance's <code>BoundingBox</code>
     * <p/>
     * <p>The point corresponds exactly to the the upper-left corner of the pixel.</p>
     *
     * @param pixel
     * @return
     */
    public Point toPoint(Pixel pixel) {
        double x = extent.getMinX() + mapUnitsPerPixelX * (pixel.x - this.pixelRange.getMinX());
        double y = extent.getMaxY() - mapUnitsPerPixelY * (pixel.y - this.pixelRange.getMinY());
        return Point.valueOf(x, y);
    }


    /**
     * Maps a point to a pixel in this instance's <code>PixelRange</code>
     *
     * <p>If the point falls on the boundary between two pixels it is mapped to the pixel on the right and/or below the
     * boundary</p>
     * @param point
     * @return
     */
    public Pixel toPixel(Point point) {
        if (point.equals(this.extent.upperRight())) {
            return toPixel(point, true, false);
        }
        if (point.equals(this.extent.upperLeft())) {
            return toPixel(point, false, false);
        }
        if (point.equals(this.extent.lowerLeft())) {
            return toPixel(point, false, true);
        }
        if (point.equals(this.extent.lowerRight())) {
            return toPixel(point, true, true);
        }
        return toPixel(point, false, false);
    }

    /**
     * Maps a point to a pixel in this instance's <code>PixelRange</code>
     *
     * <p>   </p>
     * @param point the <code>Point</code> to map
     * @param leftBorderInclusive if true, points that map to the left-boundary of a pixel are mapped to that pixel
     * @param lowerBorderInclusive if true, points that map to the lower-boundary of a pixel are mapped to that pixel
     * @return
     */
    public Pixel toPixel(Point point, boolean leftBorderInclusive, boolean lowerBorderInclusive) {
        double xOffset = (point.x - extent.getMinX());
        double yOffset = (extent.getMaxY() - point.y);
        double x = this.pixelRange.getMinX() + xOffset / mapUnitsPerPixelX;
        double y = this.pixelRange.getMinY() + yOffset / mapUnitsPerPixelY;
        int xPix = (x == Math.floor(x) && leftBorderInclusive) ? (int) (x - 1) : (int) Math.floor(x);
        int yPix = (y == Math.floor(y) && lowerBorderInclusive) ? (int) (y - 1) : (int) Math.floor(y);
        return new Pixel(xPix, yPix);
    }

    /**
     * The <code>PixelRange</code> that corresponds to the specified <code>BoundingBox</code>
     * @param bbox
     * @return
     */
    public PixelRange toPixelRange(BoundingBox bbox) {
        Pixel ulPx = toPixel(bbox.upperLeft(), false, false);
        Pixel lrPx = toPixel(bbox.lowerRight(), true, true);
        int minX = ulPx.x;
        int minY = ulPx.y;
        int width = lrPx.x - ulPx.x + 1;
        int height = lrPx.y - ulPx.y + 1;
        return new PixelRange(minX, minY, width, height);
    }
}
