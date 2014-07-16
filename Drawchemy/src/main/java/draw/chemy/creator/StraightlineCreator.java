/*
 * This file is part of the Drawchemy project - https://code.google.com/p/drawchemy/
 *
 * Copyright (c) 2014 Pilmeyer Patrick
 *
 * Drawchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Drawchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Drawchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package draw.chemy.creator;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.LinkedList;

import draw.chemy.DrawManager;

public class StraightlineCreator extends ACreator {

    private boolean fSpanningFlag = true;
    private float fSpanningLimit = 50.f;

    LinkedList<PointF> fPreviousP;
    LinkedList<PointF> fPreviousPRedo;

    private StraightlineOperation fCurrentOperation;

    public StraightlineCreator(DrawManager aManager) {
        super(aManager);
        fPreviousP = new LinkedList<PointF>();
        fPreviousPRedo = new LinkedList<PointF>();
    }


    public boolean isSpanningFlag() {
        return fSpanningFlag;
    }

    public void setSpanningFlag(boolean aSpanningFlag) {
        fSpanningFlag = aSpanningFlag;
    }

    @Override
    public IDrawingOperation startDrawingOperation(float x, float y) {
        float finalX = x;
        float finalY = y;
        if (fSpanningFlag) {
            float lim = fSpanningLimit;
            for (PointF p : fPreviousP) {
                float dst = (float) Math.hypot(p.x - x, p.y - y);
                if (dst < lim) {
                    finalX = p.x;
                    finalY = p.y;
                    lim = dst;
                }
            }
        }
        fPreviousP.addLast(new PointF(finalX, finalY));
        fPreviousPRedo.clear();
        fCurrentOperation = new StraightlineOperation(fManager.getPaint(), finalX, finalY);
        return fCurrentOperation;
    }

    @Override
    public void updateDrawingOperation(float x, float y) {
        fCurrentOperation.setEndpoint(x, y);
        fManager.redraw();
    }

    @Override
    public void endDrawingOperation() {
        fPreviousP.addLast(new PointF(fCurrentOperation.fEx, fCurrentOperation.fEy));
        fPreviousPRedo.clear();
        fCurrentOperation = null;
    }

    public class StraightlineOperation implements IDrawingOperation {

        float fSx, fSy, fEx, fEy;
        Paint fPaint;

        public StraightlineOperation(Paint aPaint, float x, float y) {
            fPaint = new Paint();
            fPaint.setAntiAlias(true);
            fPaint.setStyle(Paint.Style.STROKE);
            fPaint.setStrokeWidth(aPaint.getStrokeWidth());
            fPaint.setColor(aPaint.getColor());
            fSx = fEx = x;
            fSy = fEy = y;
        }

        public synchronized void setEndpoint(float x, float y) {
            fEx = x;
            fEy = y;
        }

        @Override
        public synchronized void draw(Canvas aCanvas) {
            aCanvas.drawLine(fSx, fSy, fEx, fEy, fPaint);
        }

        @Override
        public Paint getPaint() {
            return fPaint;
        }

        @Override
        public synchronized void computeBounds(RectF aBoundSFCT) {
            float minX = fSx < fEx ? fSx : fEx;
            float minY = fSy < fEy ? fSy : fEy;
            float maxX = fSx > fEx ? fSx : fEx;
            float maxY = fSy > fEy ? fSy : fEy;
            aBoundSFCT.set(minX, minY, maxX, maxY);
        }

        @Override
        public void redo() {
            fPreviousP.addLast(fPreviousPRedo.removeLast());
            fPreviousP.addLast(fPreviousPRedo.removeLast());

        }

        @Override
        public void undo() {
            fPreviousPRedo.addLast(fPreviousP.removeLast());
            fPreviousPRedo.addLast(fPreviousP.removeLast());
        }

        @Override
        public void complete() {
            fPreviousP.removeFirst();
            fPreviousP.removeFirst();
        }
    }

    @Override
    public void clear() {
        super.clear();
        fPreviousP.clear();
        fPreviousPRedo.clear();
    }
}