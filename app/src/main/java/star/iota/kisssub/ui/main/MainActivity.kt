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

package star.iota.kisssub.ui.main

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.provider.Settings
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.View
import com.afollestad.aesthetic.Aesthetic
import com.github.ikidou.fragmentBackHandler.BackHandlerHelper
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.android.synthetic.main.activity_main_drawer.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import star.iota.kisssub.KisssubUrl
import star.iota.kisssub.R
import star.iota.kisssub.base.BaseActivity
import star.iota.kisssub.eventbus.ChangeContentBackgroundEvent
import star.iota.kisssub.eventbus.ChangeDynamicBackgroundEvent
import star.iota.kisssub.ext.exit
import star.iota.kisssub.ext.removeFragmentsFromView
import star.iota.kisssub.ext.replaceFragmentInActivity
import star.iota.kisssub.glide.GlideApp
import star.iota.kisssub.helper.OfficialHelper
import star.iota.kisssub.helper.SearchHelper
import star.iota.kisssub.helper.ThemeHelper
import star.iota.kisssub.ui.about.AboutActivity
import star.iota.kisssub.ui.about.InfoBean
import star.iota.kisssub.ui.about.InfoContract
import star.iota.kisssub.ui.about.InfoPresenter
import star.iota.kisssub.ui.anime.AnimeFragment
import star.iota.kisssub.ui.collection.CollectionFragment
import star.iota.kisssub.ui.history.HistoryFragment
import star.iota.kisssub.ui.item.ItemFragment
import star.iota.kisssub.ui.item.search.SearchFragment
import star.iota.kisssub.ui.play.PlayFragment
import star.iota.kisssub.ui.rss.main.RssTagFragment
import star.iota.kisssub.ui.settings.SettingsActivity
import star.iota.kisssub.ui.subs.SubsFragment
import star.iota.kisssub.ui.tags.TagsFragment
import star.iota.kisssub.utils.UpdateUtils
import star.iota.kisssub.widget.M
import star.iota.kisssub.widget.ken.KenBurnsView


class MainActivity : BaseActivity(), InfoContract.View {
    override fun success(info: InfoBean) {
        UpdateUtils.show(this, info, false)
        if (OfficialHelper.acceptOfficialDynamicBackground(this)) {
            ThemeHelper.setDynamicBanner(this, info.dynamic)
            EventBus.getDefault().post(ChangeDynamicBackgroundEvent())
        }
        if (OfficialHelper.acceptOfficialContentBackground(this)) {
            ThemeHelper.setContentBanner(this, info.content)
            EventBus.getDefault().post(ChangeContentBackgroundEvent())
        }
    }

    override fun error(e: String?) {

    }

    override fun noData() {

    }

    override fun getContentViewId(): Int = R.layout.activity_main_drawer

    private val presenter: InfoPresenter by lazy { InfoPresenter(this) }
    override fun doSome() {
        initToolbar()
        initDrawer()
        initNavigationView()
        setFirstFragment()
        checkPermission()
        EventBus.getDefault().register(this)
        presenter.get(KisssubUrl.UPDATE_URL)
    }

    override fun getToolbar(): Toolbar? = toolbar

    private fun initToolbar() {
        toolbar?.menu?.clear()
        toolbar?.inflateMenu(R.menu.menu_main)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val menu = toolbar?.menu
        val searchView = menu?.findItem(R.id.menu_search)?.actionView as SearchView
        searchView.queryHint = "请输入关键字..."
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        val pan = menu.findItem(R.id.menu_pan)
        val collection = menu.findItem(R.id.menu_collection)
        pan?.isChecked = SearchHelper.getPan(this)
        collection?.isChecked = SearchHelper.getCollection(this)
        pan?.setOnMenuItemClickListener {
            pan.isChecked = !pan.isChecked
            SearchHelper.setPan(this@MainActivity, pan.isChecked)
            true
        }
        collection?.setOnMenuItemClickListener {
            collection.isChecked = !collection.isChecked
            SearchHelper.setCollection(this@MainActivity, collection.isChecked)
            true
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        presenter.unsubscribe()
        super.onDestroy()
    }

    private fun checkPermission() {
        RxPermissions(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe({
                    if (!it) {
                        M.create(this@MainActivity, "您拒绝读取文件权限，请点击颜文字前往设置授权", View.OnClickListener {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        })
                    }
                })
    }

    override fun onNewIntent(intent: Intent) {
        if (intent.action != Intent.ACTION_SEARCH) {
            return
        }
        val keywords = intent.getStringExtra(SearchManager.QUERY)
        removeFragmentsFromView(R.id.frameLayoutContainer)
        replaceFragmentInActivity(SearchFragment.newInstance("搜索：$keywords", keywords, SearchHelper.getParam(this)), R.id.frameLayoutContainer)
    }

    private fun setFirstFragment() {
        removeFragmentsFromView(R.id.frameLayoutContainer)
        replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_new), KisssubUrl.NEW), R.id.frameLayoutContainer)
    }


    private fun initDrawer() {
        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onBackPressed() {
        if (drawerLayout !== null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout?.closeDrawer(GravityCompat.START)
        } else if (!BackHandlerHelper.handleBackPress(this)) {
            exit()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDynamicBackgroundChange(event: ChangeDynamicBackgroundEvent) {
        setDynamicBackground()
    }

    private fun setDynamicBackground() {
        GlideApp.with(this)
                .load(ThemeHelper.getDynamicBanner(this))
                .into(navigationViewStart.getHeaderView(0).findViewById<KenBurnsView>(R.id.kenBurnsView))
        GlideApp.with(this)
                .load(ThemeHelper.getDynamicBanner(this))
                .into(navigationViewEnd.getHeaderView(0).findViewById<KenBurnsView>(R.id.kenBurnsView))
    }

    private fun initNavigationView() {
        setDynamicBackground()
        Aesthetic.get()
                .colorWindowBackground()
                .take(1)
                .subscribe {
                    val colors = intArrayOf(0x00000000, it)
                    val startMask = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors)
                    val endMask = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors)
                    navigationViewStart?.getHeaderView(0)?.findViewById<View>(R.id.viewMask)?.background = startMask
                    navigationViewEnd?.getHeaderView(0)?.findViewById<View>(R.id.viewMask)?.background = endMask
                }
        navigationViewStart?.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_play -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(PlayFragment.newInstance(getString(R.string.menu_play), KisssubUrl.PLAY), R.id.frameLayoutContainer)
                }
                R.id.menu_history -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(HistoryFragment.newInstance(), R.id.frameLayoutContainer)
                }
                R.id.menu_fans -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(AnimeFragment.newInstance(), R.id.frameLayoutContainer)
                }
                R.id.menu_member -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(SubsFragment.newInstance(), R.id.frameLayoutContainer)
                }
                R.id.menu_tags -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(TagsFragment.newInstance(), R.id.frameLayoutContainer)
                }
                R.id.menu_rss -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(RssTagFragment.newInstance(), R.id.frameLayoutContainer)
                }
                R.id.menu_favorite -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(CollectionFragment.newInstance(), R.id.frameLayoutContainer)
                }
                R.id.menu_settings -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
                R.id.menu_about -> {
                    startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                }
            }
            drawerLayout?.closeDrawer(GravityCompat.START)
            true
        }
        navigationViewEnd?.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_new -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_new), KisssubUrl.NEW), R.id.frameLayoutContainer)
                }
                R.id.menu_anime -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_anime), KisssubUrl.ANIME), R.id.frameLayoutContainer)
                }
                R.id.menu_comic -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_comic), KisssubUrl.COMIC), R.id.frameLayoutContainer)
                }
                R.id.menu_music -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_music), KisssubUrl.MUSIC), R.id.frameLayoutContainer)
                }
                R.id.menu_around -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_around), KisssubUrl.AROUND), R.id.frameLayoutContainer)
                }
                R.id.menu_other -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_other), KisssubUrl.OTHER), R.id.frameLayoutContainer)
                }
                R.id.menu_collection -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_collection), KisssubUrl.COLLECTION), R.id.frameLayoutContainer)
                }
                R.id.menu_ova -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_ova), KisssubUrl.OVA), R.id.frameLayoutContainer)
                }
                R.id.menu_raw -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_raw), KisssubUrl.RAW), R.id.frameLayoutContainer)
                }
                R.id.menu_discuss -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_discuss), KisssubUrl.DISCUSS), R.id.frameLayoutContainer)
                }
                R.id.menu_pan -> {
                    removeFragmentsFromView(R.id.frameLayoutContainer)
                    replaceFragmentInActivity(ItemFragment.newInstance(getString(R.string.menu_pan), KisssubUrl.PAN), R.id.frameLayoutContainer)
                }
            }
            drawerLayout?.closeDrawer(GravityCompat.END)
            true
        }
    }
}
