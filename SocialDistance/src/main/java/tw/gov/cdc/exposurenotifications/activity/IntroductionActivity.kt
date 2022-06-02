package tw.gov.cdc.exposurenotifications.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.activity_introduction.*
import kotlinx.android.synthetic.main.introduction_page.view.*
import tw.gov.cdc.exposurenotifications.R

data class Introduction(val title: Int, val description: Int, var image: Int)

class IntroductionActivity : BaseActivity() {

    companion object {
        const val EXTRA_SHOW_START_BUTTON = "EXTRA_SHOW_START_BUTTON"
    }

    private val introductions: Array<Introduction> = arrayOf(
        Introduction(
            R.string.introduction_family_title,
            R.string.introduction_family_description,
            R.drawable.introduction_family
        ),
        Introduction(
            R.string.introduction_privacy_title,
            R.string.introduction_privacy_description,
            R.drawable.introduction_privacy
        ),
        Introduction(
            R.string.introduction_intro_title,
            R.string.introduction_intro_description,
            R.drawable.introduction_upload
        ),
        Introduction(
            R.string.introduction_hcert_title,
            R.string.introduction_hcert_description,
            R.drawable.introduction_hcert
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)
        setSupportActionBar(toolbar)

        val showStartButton = intent.getBooleanExtra(EXTRA_SHOW_START_BUTTON, false)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            if (showStartButton) {
                hide()
            }
        }

        viewPager.adapter = ViewPagerAdapter(
            baseView = R.layout.introduction_page,
            introductions = introductions,
            activity = this,
            showStartButton = showStartButton
        )
        tabLayout.setupWithViewPager(viewPager)
    }

    // ActionBar

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.no_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

private class ViewPagerAdapter(
    @LayoutRes private val baseView: Int,
    private val introductions: Array<Introduction>,
    private val activity: Activity,
    private val showStartButton: Boolean
) : PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return introductions.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val introduction = introductions[position]
        val inflater = LayoutInflater.from(activity)

        val layout = inflater.inflate(baseView, container, false) as ViewGroup

        layout.textTitle.setText(introduction.title)
        layout.textDescription.apply {
            setText(introduction.description)
            movementMethod = ScrollingMovementMethod()
        }
        layout.image.setImageResource(introduction.image)

        val startButton: Button = layout.button

        if (showStartButton && position == count - 1) {
            startButton.visibility = View.VISIBLE
            startButton.setOnClickListener {
                activity.startActivity(Intent(activity, MainActivity::class.java))
                activity.finish()
            }
        } else if (showStartButton) {
            startButton.visibility = View.INVISIBLE
        } else {
            startButton.visibility = View.GONE
            (layout.textTitle.layoutParams as ConstraintLayout.LayoutParams).apply {
                topMargin = layout.context.run { resources.getDimensionPixelSize(R.dimen.main_margin_vertical) }
            }
        }

        container.addView(layout)
        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }
}
