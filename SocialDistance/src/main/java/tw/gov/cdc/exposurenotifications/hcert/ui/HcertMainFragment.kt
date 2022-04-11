package tw.gov.cdc.exposurenotifications.hcert.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.fragment_hcert_main.*
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.activity.BarcodeScanningActivity

class HcertMainFragment : Fragment() {

    private var selfView: View? = null

    private val viewModel by activityViewModels<HcertViewModel>()

    private val emptyViewGroup by lazy { hcert_empty_view_group }
    private val buttonAdd by lazy { hcert_add_button }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return selfView ?: inflater.inflate(R.layout.fragment_hcert_main, container, false).also { selfView = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonAdd.setOnClickListener {
            Intent(context, BarcodeScanningActivity::class.java).apply {
                putExtra(BarcodeScanningActivity.EXTRA_HCERT_MODE, true)
            }.let(::startActivity)
        }

        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            emptyViewGroup.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }
}