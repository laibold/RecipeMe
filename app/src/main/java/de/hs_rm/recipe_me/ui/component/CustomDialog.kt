package de.hs_rm.recipe_me.ui.component

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

open class CustomDialog<T : ViewDataBinding>(
    private val activity: Activity,
    private val layout: Int
) : Dialog(activity) {

    protected lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context), layout, null, false
        )
        setContentView(binding.root)

        // Set width to 90% of screen
        val width = (activity.resources.displayMetrics.widthPixels * 0.90).toInt()
        window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }

}
