package de.hs_rm.recipe_me.ui.component

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.button.MaterialButton
import de.hs_rm.recipe_me.R

/**
 * Alert Dialog with custom design. Use [CustomAlertDialog.Builder] to create an instance
 * and use show() to open it
 */
class CustomAlertDialog private constructor(
    private val activity: Activity,
    private var title: String,
    private var message: String,
    private var customIcon: Int,
    private var positiveButtonText: CharSequence,
    private var positiveButtonListener: OnClickListener,
    private var negativeButtonText: CharSequence,
    private var negativeButtonListener: OnClickListener
) : Dialog(activity) {

    @SuppressLint("ClickableViewAccessibility") // is handled in dismissOnMotionUp()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setContentView(R.layout.delete_alert_dialog)

        val width = (activity.resources.displayMetrics.widthPixels * 0.90).toInt()
        window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        if (customIcon != DEFAULT_ICON_VALUE) {
            val iconView = findViewById<ImageButton>(R.id.delete_icon)
            iconView.background = ResourcesCompat.getDrawable(activity.resources, customIcon, null)
        }

        // to reach in one action and to dismiss the dialog after clicking
        // we set two different listeners here, found no better solution
        val positiveButton = findViewById<MaterialButton>(R.id.alert_button_positive)
        positiveButton.text = positiveButtonText
        positiveButton.setOnClickListener(positiveButtonListener)
        positiveButton.setOnTouchListener { view, motionEvent ->
            dismissOnMotionUp(view, motionEvent)
            return@setOnTouchListener true
        }

        val negativeButton = findViewById<MaterialButton>(R.id.alert_button_negative)
        negativeButton.text = negativeButtonText
        negativeButton.setOnClickListener(negativeButtonListener)
        negativeButton.setOnTouchListener { view, motionEvent ->
            dismissOnMotionUp(view, motionEvent)
            return@setOnTouchListener true
        }

        val titleView = findViewById<TextView>(R.id.alert_headline)
        titleView.text = title

        val messageView = findViewById<TextView>(R.id.alert_text)
        messageView.text = message
    }

    /**
     * Dismiss dialog after touch event action up on selected elements
     */
    private fun dismissOnMotionUp(view: View, motionEvent: MotionEvent) {
        if (motionEvent.action == MotionEvent.ACTION_UP) {
            dismiss()
        } else {
            view.performClick()
        }
    }

    /**
     * Builder for [CustomAlertDialog]
     */
    data class Builder(
        val activity: Activity,
        var title: String = "",
        var message: String = "",
        var icon: Int = DEFAULT_ICON_VALUE,
        var positiveButtonText: CharSequence = "",
        var positiveButtonListener: OnClickListener = OnClickListener {},
        var negativeButtonText: CharSequence = "",
        var negativeButtonListener: OnClickListener? = OnClickListener {},
    ) {
        /**
         * Headline of the dialog
         */
        fun title(id: Int) = apply { this.title = activity.getString(id) }

        /**
         * Message of the dialog
         */
        fun message(id: Int) = apply { this.message = activity.getString(id) }

        /**
         * Icon of the dialog
         */
        @Suppress("unused")
        fun icon(id: Int) = apply { this.icon = id }

        /**
         * Button Text and Listener for the positive button (action button)
         */
        fun positiveButton(textId: Int, listener: OnClickListener) = apply {
            this.positiveButtonText = activity.getString(textId)
            this.positiveButtonListener = listener
        }

        /**
         * Button Text and Listener for the negative button (cancel button)
         */
        fun negativeButton(textId: Int, listener: OnClickListener) = apply {
            this.negativeButtonText = activity.getString(textId)
            this.negativeButtonListener = listener
        }

        /**
         * Create and return the dialog
         * @return CustomAlertDialog
         */
        fun create() = CustomAlertDialog(
            activity,
            title,
            message,
            icon,
            positiveButtonText,
            positiveButtonListener,
            negativeButtonText,
            negativeButtonListener!!
        )
    }

    companion object {
        private const val DEFAULT_ICON_VALUE = -1
    }

}
