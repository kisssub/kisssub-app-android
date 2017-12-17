/*
 *
 *  *    Copyright 2017. iota9star
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

package star.iota.kisssub.ui.anime

import android.support.v7.app.AppCompatActivity
import android.view.View
import com.github.florent37.glidepalette.BitmapPalette
import com.github.florent37.glidepalette.GlidePalette
import jp.wasabeef.glide.transformations.CropSquareTransformation
import kotlinx.android.synthetic.main.item_anime.view.*
import star.iota.kisssub.R
import star.iota.kisssub.base.BaseViewHolder
import star.iota.kisssub.ext.addFragmentToActivity
import star.iota.kisssub.glide.GlideApp
import star.iota.kisssub.glide.GlideOptions
import star.iota.kisssub.room.Record
import star.iota.kisssub.ui.item.search.SearchFragment
import star.iota.kisssub.ui.item.search.SearchHelper

class AnimeViewHolder(itemView: View) : BaseViewHolder<Record>(itemView) {

    override fun bindView(bean: Record) {
        itemView.apply {
            GlideApp.with(itemView)
                    .load(bean.cover)
                    .listener(GlidePalette.with(bean.cover)
                            .use(BitmapPalette.Profile.VIBRANT_LIGHT)
                            .intoBackground(imageViewCover, BitmapPalette.Swatch.RGB)
                            .crossfade(true))
                    .apply(GlideOptions.bitmapTransform(CropSquareTransformation()))
                    .into(imageViewCover)
            textViewTitle.text = bean.title
            linearLayoutContainer.setOnClickListener {
                if (!bean.title.isNullOrBlank()) {
                    (context as AppCompatActivity).addFragmentToActivity(SearchFragment.newInstance(bean.title!!, bean.title!!, SearchHelper.getParam(context)), R.id.frameLayoutContainer)
                }
            }
        }
    }
}
