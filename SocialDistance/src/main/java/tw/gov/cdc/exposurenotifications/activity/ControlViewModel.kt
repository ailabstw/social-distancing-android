package tw.gov.cdc.exposurenotifications.activity

import android.widget.CompoundButton
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.common.PreferenceManager
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager

class ControlViewModel : ViewModel() {

    companion object {
        private const val TAG = "ControlViewModel"
    }

    private val _allItems = MutableLiveData<List<ControlItem>>()

    val allItems: LiveData<List<ControlItem>> = _allItems

    fun updateItems(state: ExposureNotificationManager.ExposureNotificationState) {
        _allItems.apply {
            value = listOf(
                ControlItem.Switch(
                    textRes = R.string.control_exposure,
                    isChecked = state == ExposureNotificationManager.ExposureNotificationState.Enabled,
                    isEnabled = true,
                    onCheckedChangeListener = onServiceCheckedChange
                ),
                ControlItem.Switch(
                    textRes = R.string.control_not_found_notification,
                    isChecked = state == ExposureNotificationManager.ExposureNotificationState.Enabled && PreferenceManager.isNotFoundNotificationEnabled,
                    isEnabled = state == ExposureNotificationManager.ExposureNotificationState.Enabled,
                    onCheckedChangeListener = onNotificationCheckedChange
                ),
                ControlItem.Page(
                    textRes = R.string.menu_about,
                    onClick = { onPageOptionClick?.invoke(PageOption.ABOUT) }
                ),
                ControlItem.Page(
                    textRes = R.string.menu_privacy,
                    onClick = { onPageOptionClick?.invoke(PageOption.PRIVACY) }
                ),
                ControlItem.Page(
                    textRes = R.string.menu_faq,
                    onClick = { onPageOptionClick?.invoke(PageOption.FAQ) }
                ),
                ControlItem.Page(
                    textRes = R.string.menu_hints,
                    onClick = { onPageOptionClick?.invoke(PageOption.HINTS) },
                    isLastOne = true
                )
            )
        }
    }

    var onServiceCheckedChange: CompoundButton.OnCheckedChangeListener? = null
    var onNotificationCheckedChange: CompoundButton.OnCheckedChangeListener? = null
    var onPageOptionClick: ((PageOption) -> Unit)? = null

    sealed class ControlItem {
        data class Switch(
            @StringRes val textRes: Int,
            val isChecked: Boolean,
            val isEnabled: Boolean,
            val onCheckedChangeListener: CompoundButton.OnCheckedChangeListener?,
            val isLastOne: Boolean = false
        ) : ControlItem()

        data class Page(@StringRes val textRes: Int, val onClick: () -> Unit, val isLastOne: Boolean = false) : ControlItem()

        val viewType: Int
            get() {
                return when (this) {
                    is Switch -> ControlAdapter.ViewType.SWITCH.value
                    is Page -> ControlAdapter.ViewType.PAGE.value
                }
            }
    }

    enum class PageOption {
        ABOUT, PRIVACY, FAQ, HINTS
    }
}