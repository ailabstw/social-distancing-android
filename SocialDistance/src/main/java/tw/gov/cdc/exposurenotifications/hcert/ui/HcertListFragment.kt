package tw.gov.cdc.exposurenotifications.hcert.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.fragment_hcert_list.*
import tw.gov.cdc.exposurenotifications.R

class HcertListFragment : Fragment(), HcertListActionHandler {

    private var selfView: View? = null

    private val viewModel by activityViewModels<HcertViewModel>()

    private val recyclerView by lazy { hcert_list_recycler_view }
    private val touchHelper by lazy { HcertListTouchHelper() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return selfView ?: inflater.inflate(R.layout.fragment_hcert_list, container, false).also { selfView = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        touchHelper.attachToRecyclerView(recyclerView)

        val adapter = HcertListAdapter(this)
        recyclerView.adapter = adapter

        viewModel.allItems.observe(viewLifecycleOwner) {
            if (it.size != adapter.itemCount) adapter.submitList(it)
        }
    }

    override fun onHcertClick(position: Int) {
        viewModel.updatePosition(position)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HcertDetailFragment())
            .addToBackStack(HcertDetailFragment::class.java.canonicalName)
            .commit()
    }

    override fun onDragStart(viewHolder: HcertListViewHolder) {
        touchHelper.startDrag(viewHolder)
    }
}