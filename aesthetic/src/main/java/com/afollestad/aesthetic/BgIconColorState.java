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

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import io.reactivex.functions.BiFunction;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @author Aidan Follestad (afollestad)
 */
@RestrictTo(LIBRARY_GROUP)
final class BgIconColorState {

    @ColorInt
    private final int bgColor;
    private final ActiveInactiveColors iconTitleColor;

    private BgIconColorState(@ColorInt int bgColor, ActiveInactiveColors iconTitleColor) {
        this.bgColor = bgColor;
        this.iconTitleColor = iconTitleColor;
    }

    static BgIconColorState create(@ColorInt int color, ActiveInactiveColors iconTitleColors) {
        return new BgIconColorState(color, iconTitleColors);
    }

    static BiFunction<Integer, ActiveInactiveColors, BgIconColorState> creator() {
        return BgIconColorState::create;
    }

    @ColorInt
    int bgColor() {
        return bgColor;
    }

    @Nullable
    ActiveInactiveColors iconTitleColor() {
        return iconTitleColor;
    }
}
