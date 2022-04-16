package tw.gov.cdc.exposurenotifications.hcert.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnAttach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.fragment_hcert_detail.*
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.common.overrideScreenBrightness

class HcertDetailFragment : Fragment(), HcertDetailActionHandler {

    private var selfView: View? = null

    private val viewModel by activityViewModels<HcertViewModel>()

    private val viewPager by lazy { hcert_detail_view_pager }
    private val prevButton by lazy { hcert_detail_prev_button }
    private val nextButton by lazy { hcert_detail_next_button }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return selfView ?: inflater.inflate(R.layout.fragment_hcert_detail, container, false).also { selfView = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun updateButtonUI(itemCount: Int) {
            if (itemCount == 0) {
                requireActivity().onBackPressed()
            }
            viewPager.post {
                prevButton.isEnabled = viewPager.currentItem > 0
                nextButton.isEnabled = viewPager.currentItem < itemCount - 1
            }
        }

        val adapter = HcertDetailAdapter(this) { updateButtonUI(it) }
        viewPager.adapter = adapter

        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        viewPager.registerOnPageChangeCallback(onPageChangeCallback)

        viewModel.allItems.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            if (viewPager.currentItem != position) viewPager.doOnAttach {
                viewPager.setCurrentItem(position, false)
            }
            updateButtonUI(adapter.itemCount)
        }

        prevButton.setOnClickListener {
            viewPager.currentItem = viewPager.currentItem - 1
        }

        nextButton.setOnClickListener {
            viewPager.currentItem = viewPager.currentItem + 1
        }

        viewPager.doOnAttach {
            viewPager.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.overrideScreenBrightness(true)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.overrideScreenBrightness(false)
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                viewModel.updatePosition(viewPager.currentItem)
            }
        }
    }

    override fun onHcertDelete(hcert: HcertModel, position: Int) {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.hcert_delete_confirm_title)
                .setMessage(it.getString(R.string.hcert_delete_confirm_message, hcert.name))
                .setPositiveButton(R.string.confirm) { _, _ -> viewModel.deleteAt(position) }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .setCancelable(false)
                .create()
                .show()
        }
    }
}