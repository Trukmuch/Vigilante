package com.crazylegend.vigilante.power.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.crazylegend.crashyreporter.CrashyReporter
import com.crazylegend.database.DBResult
import com.crazylegend.database.handle
import com.crazylegend.kotlinextensions.dateAndTime.toString
import com.crazylegend.kotlinextensions.fragments.shortToast
import com.crazylegend.kotlinextensions.fragments.viewCoroutineScope
import com.crazylegend.kotlinextensions.views.visibleIfTrueGoneOtherwise
import com.crazylegend.viewbinding.viewBinding
import com.crazylegend.vigilante.R
import com.crazylegend.vigilante.abstracts.AbstractBottomSheet
import com.crazylegend.vigilante.databinding.DialogPowerDetailsBinding
import com.crazylegend.vigilante.power.db.PowerModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

/**
 * Created by crazy on 11/7/20 to long live and prosper !
 */
@AndroidEntryPoint
class PowerDetailsDialog : AbstractBottomSheet<DialogPowerDetailsBinding>() {
    override val binding by viewBinding(DialogPowerDetailsBinding::bind)
    override val viewRes: Int
        get() = R.layout.dialog_power_details

    private val powerDetailsVM by viewModels<PowerDetailsVM>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope.launchWhenResumed {
            powerDetailsVM.powerModel.collectLatest {
                binding.loading.visibleIfTrueGoneOtherwise(it is DBResult.Querying)
                it.handle(
                        dbError = { throwable ->
                            handleDBError(throwable)
                        },
                        success = {
                            updateUI(this)
                        }
                )
            }
        }
    }

    private fun handleDBError(throwable: Throwable) {
        CrashyReporter.logException(throwable)
        dismissAllowingStateLoss()
        shortToast(R.string.error_occurred)
    }

    private fun updateUI(model: PowerModel) {
        binding.powerIcon.setImageResource(model.chargingDrawable)
        binding.powerTitle.text = getString(model.chargingTitle)
        binding.date.image.setImageResource(R.drawable.ic_calendar)
        binding.date.text.text = model.date.toString(prefsProvider.getDateFormat)
        binding.chargingType.root.visibleIfTrueGoneOtherwise(model.chargingTypeTitle != null)
        model.chargingTypeDrawable?.let { binding.chargingType.image.setImageResource(it) }
        binding.chargingType.text.text = model.chargingTypeTitle?.let { getString(it) }
        binding.batteryCapacity.text.text = model.batteryPercentage.toString()
        binding.batteryCapacity.image.setImageResource(R.drawable.ic_percentage)
    }
}