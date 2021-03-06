/*
 *
 *  *    Copyright 2018. iota9star
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.afollestad.aesthetic;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static com.afollestad.aesthetic.Rx.onErrorLogAndRethrow;
import static com.afollestad.aesthetic.Util.adjustAlpha;
import static com.afollestad.aesthetic.Util.resolveResId;

/**
 * @author Aidan Follestad (afollestad)
 */
public class AestheticTextInputLayout extends TextInputLayout {

    private CompositeDisposable subscriptions;
    private int backgroundResId;

    public AestheticTextInputLayout(Context context) {
        super(context);
    }

    public AestheticTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AestheticTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            backgroundResId = resolveResId(context, attrs, android.R.attr.background);
        }
    }

    private void invalidateColors(int color) {
        TextInputLayoutUtil.setAccent(this, color);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        subscriptions = new CompositeDisposable();
        subscriptions.add(
                Aesthetic.get()
                        .textColorSecondary()
                        .compose(Rx.distinctToMainThread())
                        .subscribe(color -> TextInputLayoutUtil.setHint(AestheticTextInputLayout.this, adjustAlpha(color, 0.7f)), onErrorLogAndRethrow()));
        Observable<Integer> obs = ViewUtil.getObservableForResId(getContext(), backgroundResId, Aesthetic.get().colorAccent());
        if (obs != null) {
            subscriptions.add(
                    obs.compose(Rx.distinctToMainThread())
                            .subscribe(this::invalidateColors, onErrorLogAndRethrow()));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (subscriptions != null) {
            subscriptions.clear();
        }
        super.onDetachedFromWindow();
    }
}
