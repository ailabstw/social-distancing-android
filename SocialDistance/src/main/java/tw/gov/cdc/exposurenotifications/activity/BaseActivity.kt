package tw.gov.cdc.exposurenotifications.activity

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import tw.gov.cdc.exposurenotifications.R

open class BaseActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BaseActivity"
    }

    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.color.background)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onStart() {
        super.onStart()
        progressBar = findViewById(R.id.progressBar)
        progressBar?.visibility = View.GONE
    }

    override fun onPause() {
        setUserInteraction(true)
        super.onPause()
    }

    override fun onBackPressed() {
        setUserInteraction(true)
        super.onBackPressed()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboard()
        return super.dispatchTouchEvent(ev)
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        view?.let {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    fun showProgressBar() {
        progressBar?.let {
            it.visibility = View.VISIBLE
            setUserInteraction(false)
        }
    }

    fun hideProgressBar() {
        progressBar?.let {
            it.visibility = View.GONE
            setUserInteraction(true)
        }
    }

    private fun setUserInteraction(enable: Boolean) {
        when (enable) {
            true -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
            false -> {
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }
    }
}