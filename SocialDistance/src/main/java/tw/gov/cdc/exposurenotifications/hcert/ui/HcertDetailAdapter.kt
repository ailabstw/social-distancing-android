package tw.gov.cdc.exposurenotifications.hcert.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_hcert_detail.view.*
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.common.QRCodeEncoder

class HcertDetailAdapter(
    private val actionHandler: HcertDetailActionHandler,
    private val onCurrentListChanged: (itemCount: Int) -> Unit
) : ListAdapter<HcertModel, HcertDetailViewHolder>(HcertDetailDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HcertDetailViewHolder {
        return HcertDetailViewHolder.createViewHolder(parent, viewType, actionHandler)
    }

    override fun onBindViewHolder(holder: HcertDetailViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun onCurrentListChanged(previousList: MutableList<HcertModel>, currentList: MutableList<HcertModel>) {
        super.onCurrentListChanged(previousList, currentList)
        onCurrentListChanged(currentList.size)
    }
}

sealed class HcertDetailViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    abstract fun bind(item: HcertModel, position: Int)

    class HcertDetailHolder(v: View, private val actionHandler: HcertDetailActionHandler) : HcertDetailViewHolder(v) {

        private val scrollView by lazy { v.item_hcert_detail_scroll_view }
        private val qrCodeImage by lazy { v.item_hcert_detail_qrcode_image }
        private val name by lazy { v.item_hcert_detail_name }
        private val nameTransliterated by lazy { v.item_hcert_detail_name_transliterated }
        private val birthday by lazy { v.item_hcert_detail_birthday }
        private val lastDoseDate by lazy { v.item_hecrt_detail_last_dose_date }
        private val detailText by lazy { v.item_hcert_detail_text }
        private val deleteButton by lazy { v.item_hcert_detail_delete_button }

        override fun bind(item: HcertModel, position: Int) {
            scrollView.scrollTo(0, 0)

            qrCodeImage.post {
                try {
                    qrCodeImage.setImageBitmap(
                        QRCodeEncoder.getBitmap(
                            item.rawString, qrCodeImage.width,
                            ContextCompat.getColor(qrCodeImage.context, R.color.black),
                            ContextCompat.getColor(qrCodeImage.context, R.color.background)
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            name.text = item.name
            nameTransliterated.text = item.nameTransliterated
            birthday.text = item.dateOfBirth

            lastDoseDate.text = lastDoseDate.context.getString(
                R.string.hcert_last_dose_date, item.dateOfVaccination
            )

            detailText.text = buildSpannedString {
                bold { append(detailText.context.getString(R.string.hcert_detail_target)) }
                append("${item.targetDisease}\n")

                bold { append(detailText.context.getString(R.string.hcert_detail_vaccine)) }
                append("${item.vaccine}\n")

                bold { append(detailText.context.getString(R.string.hcert_detail_medicinal_product)) }
                append("${item.medicinalProduct}\n")

                bold { append(detailText.context.getString(R.string.hcert_detail_authorization_holder)) }
                append("${item.authorizationHolder}\n")

                bold { append(detailText.context.getString(R.string.hcert_detail_dose)) }
                append("${item.doseState}\n")

                bold { append(detailText.context.getString(R.string.hcert_detail_date)) }
                append("${item.dateOfVaccination}\n")

                bold { append(detailText.context.getString(R.string.hcert_detail_country)) }
                append("${item.country}\n")

                bold { append(detailText.context.getString(R.string.hcert_detail_certificate_issuer)) }
                append("${item.certificateIssuer}\n")

                bold { append(detailText.context.getString(R.string.hcert_detail_certificate_identifier)) }
                append(item.certificateIdentifier)
            }

            deleteButton.setOnClickListener {
                actionHandler.onHcertDelete(item, adapterPosition)
            }
        }
    }

    companion object {
        fun createViewHolder(parent: ViewGroup, viewType: Int, actionsHandler: HcertDetailActionHandler): HcertDetailViewHolder {
            return HcertDetailHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_hcert_detail, parent, false),
                actionsHandler
            )
        }
    }
}

class HcertDetailDiffCallback : DiffUtil.ItemCallback<HcertModel>() {
    override fun areItemsTheSame(oldItem: HcertModel, newItem: HcertModel): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: HcertModel, newItem: HcertModel): Boolean = oldItem == newItem
}
