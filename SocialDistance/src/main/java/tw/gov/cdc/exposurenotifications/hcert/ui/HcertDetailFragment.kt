package tw.gov.cdc.exposurenotifications.hcert.ui

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
import tw.gov.cdc.exposurenotifications.hcert.decode.data.GreenCertificate

class HcertDetailFragment : Fragment(), HcertDetailActionHandler {

    private var selfView: View? = null

    private val viewModel by activityViewModels<HcertViewModel>()

    private val viewPager by lazy { hcert_detail_view_pager }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return selfView ?: inflater.inflate(R.layout.fragment_hcert_detail, container, false).also { selfView = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HcertDetailAdapter(this)
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
        }

        viewPager.doOnAttach {
            viewPager.visibility = View.VISIBLE
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                viewModel.updatePosition(viewPager.currentItem)
            }
        }
    }

    override fun onHcertDelete(hcert: GreenCertificate, position: Int) {
        viewModel.deleteAt(position)
    }
}