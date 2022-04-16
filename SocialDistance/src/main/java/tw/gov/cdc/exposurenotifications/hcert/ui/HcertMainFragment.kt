package tw.gov.cdc.exposurenotifications.hcert.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnAttach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.fragment_hcert_main.*
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.activity.BarcodeScanningActivity

class HcertMainFragment : Fragment(), HcertMainActionHandler {

    private var selfView: View? = null

    private val viewModel by activityViewModels<HcertViewModel>()

    private val mainViewGroup by lazy { hcert_main_view_group }
    private val viewPager by lazy { hcert_view_pager }
    private val dotsIndicator by lazy { hcert_dotsIndicator }
    private val buttonList by lazy { hcert_list_button }
    private val buttonAddMore by lazy { hcert_add_more_button }

    private val emptyViewGroup by lazy { hcert_empty_view_group }
    private val buttonAdd by lazy { hcert_add_button }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return selfView ?: inflater.inflate(R.layout.fragment_hcert_main, container, false).also { selfView = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HcertMainAdapter(this)
        viewPager.adapter = adapter
        dotsIndicator.setViewPager2(viewPager)

        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        viewPager.registerOnPageChangeCallback(onPageChangeCallback)

        viewModel.allItems.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            viewPager.post {
                dotsIndicator.setViewPager2(viewPager)
            }
        }

        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            if (viewPager.currentItem != position) viewPager.doOnAttach {
                viewPager.setCurrentItem(position, false)
            }
        }

        buttonList.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HcertListFragment())
                .addToBackStack(HcertListFragment::class.java.canonicalName)
                .commit()
        }

        buttonAddMore.setOnClickListener {
            Intent(context, BarcodeScanningActivity::class.java).apply {
                putExtra(BarcodeScanningActivity.EXTRA_HCERT_MODE, true)
            }.let(::startActivity)
        }

        buttonAdd.setOnClickListener {
            Intent(context, BarcodeScanningActivity::class.java).apply {
                putExtra(BarcodeScanningActivity.EXTRA_HCERT_MODE, true)
            }.let(::startActivity)
        }

        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            emptyViewGroup.visibility = if (isEmpty) View.VISIBLE else View.GONE
            mainViewGroup.visibility = if (isEmpty) View.GONE else View.VISIBLE
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

    override fun onHcertClick() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HcertDetailFragment())
            .addToBackStack(HcertDetailFragment::class.java.canonicalName)
            .commit()
    }
}