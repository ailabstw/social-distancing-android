package tw.gov.cdc.exposurenotifications.hcert.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_hcert_detail.view.*
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.hcert.decode.data.GreenCertificate

class HcertDetailAdapter(
    private val actionHandler: HcertDetailActionHandler
) : ListAdapter<GreenCertificate, HcertDetailViewHolder>(HcertDetailDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HcertDetailViewHolder {
        return HcertDetailViewHolder.createViewHolder(parent, viewType, actionHandler)
    }

    override fun onBindViewHolder(holder: HcertDetailViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
}

sealed class HcertDetailViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    abstract fun bind(item: GreenCertificate, position: Int)

    class HcertDetailHolder(v: View, private val actionHandler: HcertDetailActionHandler) : HcertDetailViewHolder(v) {

        private val scrollView by lazy { v.item_hcert_detail_scroll_view }
        private val qrCodeImage by lazy { v.item_hcert_detail_qrcode_image }
        private val name by lazy { v.item_hcert_detail_name }
        private val nameTransliterated by lazy { v.item_hcert_detail_name_transliterated }
        private val birthday by lazy { v.item_hcert_detail_birthday }
        private val lastDoseDate by lazy { v.item_hecrt_detail_last_dose_date }
        private val detailText by lazy { v.item_hcert_detail_text }
        private val deleteButton by lazy { v.item_hcert_detail_delete_button }

        override fun bind(item: GreenCertificate, position: Int) {
            scrollView.scrollTo(0, 0)

            qrCodeImage.post {
                val qrgEncoder = QRGEncoder(
                    item.rawString, null,
                    QRGContents.Type.TEXT,
                    qrCodeImage.width
                )
                qrgEncoder.colorWhite = ContextCompat.getColor(qrCodeImage.context, R.color.background)
                try {
                    val bitmap = qrgEncoder.bitmap
                    qrCodeImage.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            item.subject.run {
                if (familyName?.contains(regex) == true || givenName?.contains(regex) == true) {
                    name.text = "$givenName $familyName"
                    nameTransliterated.text = "${givenNameTransliterated?.replace("<", "-")} ${familyNameTransliterated?.replace("<", "-")}"
                } else {
                    name.text = "$familyName$givenName"
                    nameTransliterated.text = "${familyNameTransliterated?.replace("<", "-")}, ${givenNameTransliterated?.replace("<", "-")}"
                }
            }

            birthday.text = item.dateOfBirthString.replace('-', '.')

            item.vaccinations?.firstNotNullOf { it }?.let { vaccination ->
                val dateString = vaccination.dateString.replace('-', '.')

                lastDoseDate.text = lastDoseDate.context.getString(
                    R.string.hcert_last_dose_date, dateString
                )

                detailText.text = buildSpannedString {
                    bold { append(detailText.context.getString(R.string.hcert_detail_target)) }
                    append("${vaccination.target.valueSetEntry.display}\n")

                    bold { append(detailText.context.getString(R.string.hcert_detail_vaccine)) }
                    append("${vaccination.vaccine.valueSetEntry.display}\n")

                    bold { append(detailText.context.getString(R.string.hcert_detail_medicinal_product)) }
                    append("${vaccination.medicinalProduct.valueSetEntry.display}\n")

                    bold { append(detailText.context.getString(R.string.hcert_detail_authorization_holder)) }
                    append("${vaccination.authorizationHolder.valueSetEntry.display}\n")

                    bold { append(detailText.context.getString(R.string.hcert_detail_dose)) }
                    append("${vaccination.doseNumber}/${vaccination.doseTotalNumber}\n")

                    bold { append(detailText.context.getString(R.string.hcert_detail_date)) }
                    append("${dateString}\n")

                    bold { append(detailText.context.getString(R.string.hcert_detail_country)) }
                    append("${vaccination.country.valueSetEntry.display}\n")

                    bold { append(detailText.context.getString(R.string.hcert_detail_certificate_issuer)) }
                    append("${vaccination.certificateIssuer}\n")

                    bold { append(detailText.context.getString(R.string.hcert_detail_certificate_identifier)) }
                    append(vaccination.certificateIdentifier)
                }

            } ?: run {
                lastDoseDate.text = "-"
            }

            deleteButton.setOnClickListener {
                actionHandler.onHcertDelete(item, adapterPosition)
            }
        }

        companion object {
            private val regex = """[a-zA-Z]""".toRegex()
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

class HcertDetailDiffCallback : DiffUtil.ItemCallback<GreenCertificate>() {
    override fun areItemsTheSame(oldItem: GreenCertificate, newItem: GreenCertificate): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: GreenCertificate, newItem: GreenCertificate): Boolean = oldItem == newItem
}
