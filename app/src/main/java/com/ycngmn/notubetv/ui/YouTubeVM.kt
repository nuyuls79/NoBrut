package com.ycngmn.notubetv.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ycngmn.notubetv.utils.ReleaseData

class YoutubeVM : ViewModel() {

    // ✅ Private mutable state
    private val _scriptData = mutableStateOf<String?>(null)
    val scriptData: State<String?> = _scriptData

    private val _updateData = mutableStateOf<ReleaseData?>(null)
    val updateData: State<ReleaseData?> = _updateData

    // ✅ Set script (hindari update berulang)
    fun setScript(data: String) {
        if (_scriptData.value != data) {
            _scriptData.value = data
        }
    }

    // ✅ Set update (hindari trigger ulang)
    fun setUpdate(data: ReleaseData) {
        if (_updateData.value != data) {
            _updateData.value = data
        }
    }
}
