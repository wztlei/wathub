/*
 * Copyright 2013 Christian De Angelis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wztlei.wathub.datepicker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import io.github.wztlei.wathub.R;

/**
 * A text view which, when pressed or activated, displays a circle around the text.
 */
public class TextViewWithCircularIndicator extends CheckedTextView {

    private static final int SELECTED_CIRCLE_ALPHA = 255;
    private final int mCircleColor;
    private final String mItemIsSelectedText;
    private final Paint mCirclePaint = new Paint();
    private boolean mDrawCircle;

    public TextViewWithCircularIndicator(
            Context context,
            AttributeSet attrs) {
        super(context, attrs);
        final Resources res = context.getResources();
        mCircleColor = res.getColor(R.color.datetimepicker_background);
        mItemIsSelectedText = res.getString(R.string.datetimepicker_item_is_selected);

        init();
    }

    private void init() {
        mCirclePaint.setFakeBoldText(true);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setTextAlign(Align.CENTER);
        mCirclePaint.setStyle(Style.FILL);
        mCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);
    }

    public void setDrawIndicator(boolean drawCircle) {
        mDrawCircle = drawCircle;
        setChecked(drawCircle);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mDrawCircle) {
            final int width = getWidth();
            final int height = getHeight();
            int radius = Math.min(width, height) / 2;
            canvas.drawCircle(width / 2, height / 2, radius, mCirclePaint);
        }
        super.onDraw(canvas);
    }

    @Override
    public CharSequence getContentDescription() {
        CharSequence itemText = getText();
        if (mDrawCircle) {
            return String.format(mItemIsSelectedText, itemText);
        } else {
            return itemText;
        }
    }
}
