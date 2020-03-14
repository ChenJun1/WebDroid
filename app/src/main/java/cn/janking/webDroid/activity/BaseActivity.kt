package cn.janking.webDroid.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import cn.janking.webDroid.R
import cn.janking.webDroid.model.Config
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_webdroid.*
import kotlinx.android.synthetic.main.layout_nav.*

/**
 * @author Janking
 */
abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener {
    /**
     * 布局ID
     */
    abstract val layoutId: Int
    /**
     * toolbar右边菜单id
     */
    open val toolBarMenuId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        initToolBar()
        initViews()
    }

    /**
     * toolbar右边的菜单
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        toolBarMenuId?.let {
            menuInflater.inflate(it, menu)
            return true
        }
        return false
    }

    /**
     * toolbar右边的菜单 点击事件
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onClickViewId(item.itemId)
        return super.onOptionsItemSelected(item)
    }


    protected open fun initToolBar() {
        toolbar.title = Config.instance.appName
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        drawerNavigation.setNavigationItemSelectedListener(this)
        drawerNavigation.getHeaderView(0).findViewById<LinearLayout>(R.id.navHeader)
            .setOnClickListener(this)
    }

    /**
     * 点击侧边导航栏的菜单
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        onClickViewId(item.itemId)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onClick(v: View?) {
        v?.let {
            onClickViewId(it.id)
        }
    }

    /**
     * 根据view的id判断点击事件
     * 集中处理点击事件
     */
    protected open fun onClickViewId(viewId : Int){
        when(viewId){
            /*toolbar菜单*/

            /*侧边导航栏header*/
            R.id.navHeader ->{

            }
            /*侧边导航栏菜单*/
            R.id.nav_settings -> {

            }
            R.id.nav_about -> {

            }
        }
    }

    /**
     * 对view初始化
     */
    protected open fun initViews() {

    }

    /**
     * 监听返回键
     */
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (!onPageBackPressed()) {
                super.onBackPressed()
            }
        }
    }

    /**
     * 返回true表示page已经处理
     */
    protected open fun onPageBackPressed(): Boolean {
        return false
    }
}