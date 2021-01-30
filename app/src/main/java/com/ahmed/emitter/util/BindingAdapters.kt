package com.ahmed.emitter.util

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("app:isNetworkError", "app:usersList")
fun hideIfNetworkError(view: View, isNetworkError: Boolean, list: Any?) {
    view.visibility = if (list != null) View.GONE else View.VISIBLE
    if (isNetworkError) {
        view.visibility = View.GONE
    }
}