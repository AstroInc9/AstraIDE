/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2022  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 */
package io.github.rosemoe.sora.widget.component;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class DefaultCompletionLayout implements CompletionLayout {

    private ListView listView;
    private ProgressBar progressBar;
    private RelativeLayout rootView;
    private EditorAutoCompletion editorAutoCompletion;

    @Override
    public void setEditorCompletion(EditorAutoCompletion completion) {
        editorAutoCompletion = completion;
    }

    @Override
    public View inflate(Context context) {
        RelativeLayout layout = new RelativeLayout(context);
        listView = new ListView(context);
        layout.addView(listView, new LinearLayout.LayoutParams(-1, -1));
        progressBar = new ProgressBar(context);
        layout.addView(progressBar);
        var params = ((RelativeLayout.LayoutParams) progressBar.getLayoutParams());
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.width = params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics()));
        layout.setBackground(gd);
        rootView = layout;
        listView.setDividerHeight(0);
        setLoading(true);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                editorAutoCompletion.select(position);
            } catch (Exception e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        return layout;
    }

    @Override
    public void onApplyColorScheme(EditorColorScheme colorScheme) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, editorAutoCompletion.getContext().getResources().getDisplayMetrics()));
        gd.setStroke(1, colorScheme.getColor(EditorColorScheme.COMPLETION_WND_CORNER));
        gd.setColor(colorScheme.getColor(EditorColorScheme.COMPLETION_WND_BACKGROUND));
        rootView.setBackground(gd);
    }

    @Override
    public void setLoading(boolean state) {
        progressBar.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public ListView getCompletionList() {
        return listView;
    }

    /**
     * Perform motion events
     */
    private void performScrollList(int offset) {
        var adpView = getCompletionList();

        long down = SystemClock.uptimeMillis();
        var ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_DOWN, 0, 0, 0);
        adpView.onTouchEvent(ev);
        ev.recycle();

        ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_MOVE, 0, offset, 0);
        adpView.onTouchEvent(ev);
        ev.recycle();

        ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_CANCEL, 0, offset, 0);
        adpView.onTouchEvent(ev);
        ev.recycle();
    }

    @Override
    public void ensureListPositionVisible(int position, int increment) {
        listView.post(() -> {
            while (listView.getFirstVisiblePosition() + 1 > position && listView.canScrollList(-1)) {
                performScrollList(increment / 2);
            }
            while (listView.getLastVisiblePosition() - 1 < position && listView.canScrollList(1)) {
                performScrollList(-increment / 2);
            }
        });
    }
}
