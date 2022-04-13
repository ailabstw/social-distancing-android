package tw.gov.cdc.exposurenotifications.hcert.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_hcert.view.*
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.hcert.decode.data.GreenCertificate

class HcertMainAdapter(
    private val actionHandler: HcertMainActionHandler
) : ListAdapter<GreenCertificate, HcertViewHolder>(HcertDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HcertViewHolder {
        return HcertViewHolder.createViewHolder(parent, viewType, actionHandler)
    }

    override fun onBindViewHolder(holder: HcertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

sealed class HcertViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    abstract fun bind(item: GreenCertificate)

    class HcertCardHolder(v: View, private val actionHandler: HcertMainActionHandler) : HcertViewHolder(v) {

        private val cardView by lazy { v.item_card_view }
        private val qrCodeImage by lazy { v.item_hcert_qrcode_image }
        private val name by lazy { v.item_hcert_name }
        private val nameTransliterated by lazy { v.item_hcert_name_transliterated }
        private val birthday by lazy { v.item_hcert_birthday }
        private val lastDoseDate by lazy { v.item_last_dose_date }

        override fun bind(item: GreenCertificate) {
            qrCodeImage.post {
                val qrgEncoder = QRGEncoder(
                    item.rawString, null,
                    QRGContents.Type.TEXT,
                    qrCodeImage.width
                )
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

            birthday.text = item.dateOfBirthString.replace('-','.')

            lastDoseDate.text = lastDoseDate.context.getString(R.string.hcert_last_dose_date,
                item.vaccinations?.let { it.firstNotNullOf { it }.dateString.replace('-','.') } ?: "-"
            )

            cardView.setOnClickListener {
                actionHandler.onHcertClick(item)
            }
        }

        companion object {
            private val regex = """[a-zA-Z]""".toRegex()
        }
    }

    companion object {
        fun createViewHolder(parent: ViewGroup, viewType: Int, actionsHandler: HcertMainActionHandler): HcertViewHolder {
            return HcertCardHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_hcert, parent, false),
                actionsHandler
            )
        }
    }
}

class HcertDiffCallback : DiffUtil.ItemCallback<GreenCertificate>() {
    override fun areItemsTheSame(oldItem: GreenCertificate, newItem: GreenCertificate): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: GreenCertificate, newItem: GreenCertificate): Boolean = oldItem == newItem
}
