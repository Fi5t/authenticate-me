package com.redmadrobot.authenticateme.internal.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import com.redmadrobot.authenticateme.R

@SuppressWarnings("MagicNumber")
class NumericKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val COLUMN_COUNT = 3
        private const val ROW_COUNT = 4
    }

    private var buttonVerticalMargin = 0
    private var buttonHorizontalMargin = 0
    private var buttonWidth = -1
    private var buttonHeight = -1

    @DrawableRes
    private var buttonBackground = -1

    @StyleRes
    private var buttonTextAppearance = -1

    @FontRes
    private var buttonFontId = -1

    private lateinit var leftCustomButtonContainer: FrameLayout
    private lateinit var rightCustomButtonContainer: FrameLayout

    var keyboardClickListener: (Char) -> Unit = {}

    init {
        lateinit var attrsArray: TypedArray
        try {
            attrsArray = context.obtainStyledAttributes(attrs, R.styleable.NumericKeyboard)
            with(attrsArray) {
                buttonWidth = getDimensionPixelSize(R.styleable.NumericKeyboard_buttonWidth, -1)
                buttonHeight = getDimensionPixelSize(R.styleable.NumericKeyboard_buttonHeight, -1)
                buttonHorizontalMargin = getDimensionPixelSize(R.styleable.NumericKeyboard_buttonHorizontalMargin, 0)
                buttonVerticalMargin = getDimensionPixelSize(R.styleable.NumericKeyboard_buttonVerticalMargin, 0)
                buttonBackground = getResourceId(R.styleable.NumericKeyboard_buttonBackground, -1)
                buttonTextAppearance = getResourceId(R.styleable.NumericKeyboard_buttonTextAppearance, -1)
                buttonFontId = getResourceId(R.styleable.NumericKeyboard_buttonFont, -1)
            }
        } finally {
            attrsArray.recycle()
        }

        validateAttrs()

        initViews()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return !isEnabled
    }

    fun setLeftCustomButton(label: String, textAppearance: Int, action: () -> Unit) {
        val leftCustomButton = createButton(label, textAppearance, action)
        leftCustomButtonContainer.removeAllViews()
        leftCustomButtonContainer.addView(leftCustomButton, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    fun setRightCustomButton(label: String, textAppearance: Int? = null, action: () -> Unit) {
        val leftCustomButton = createButton(label, textAppearance, action)
        rightCustomButtonContainer.removeAllViews()
        rightCustomButtonContainer.addView(leftCustomButton, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    fun setLeftCustomButton(@DrawableRes drawable: Int, action: () -> Unit) {
        val leftCustomButton = createButton(drawable, action)
        leftCustomButtonContainer.removeAllViews()
        leftCustomButtonContainer.addView(leftCustomButton, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    fun setRightCustomButton(@DrawableRes drawable: Int, action: () -> Unit) {
        val leftCustomButton = createButton(drawable, action)
        rightCustomButtonContainer.removeAllViews()
        rightCustomButtonContainer.addView(leftCustomButton, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    fun removeRightCustomButton() {
        rightCustomButtonContainer.removeAllViews()
    }

    fun removeLeftCustomButton() {
        leftCustomButtonContainer.removeAllViews()
    }

    private fun initViews() {
        orientation = HORIZONTAL
        columnCount = COLUMN_COUNT

        setupNumericKeys()
    }

    private fun setupNumericKeys() {
        addSimpleButton("1", '1', 0, 0)
        addSimpleButton("2", '2', 1, 0)
        addSimpleButton("3", '3', 2, 0)
        addSimpleButton("4", '4', 0, 1)
        addSimpleButton("5", '5', 1, 1)
        addSimpleButton("6", '6', 2, 1)
        addSimpleButton("7", '7', 0, 2)
        addSimpleButton("8", '8', 1, 2)
        addSimpleButton("9", '9', 2, 2)

        leftCustomButtonContainer = FrameLayout(context)
        addView(leftCustomButtonContainer, getButtonLayoutParams(0, 3))

        addSimpleButton("0", '0', 1, 3)

        rightCustomButtonContainer = FrameLayout(context)
        addView(rightCustomButtonContainer, getButtonLayoutParams(2, 3))
    }

    private fun addSimpleButton(label: String, value: Char, column: Int, row: Int) {
        val button = createButton(label) { keyboardClickListener(value) }
        addView(button, getButtonLayoutParams(column, row))
    }

    private fun createButton(label: String, textAppearance: Int? = null, action: () -> Unit): View {
        return TextView(context).apply {
            text = label
            gravity = Gravity.CENTER

            if (buttonBackground != -1) setBackgroundResource(buttonBackground)

            when {
                textAppearance != null -> setTextAppearance(this, textAppearance)
                buttonTextAppearance != -1 -> setTextAppearance(this, buttonTextAppearance)

                else -> throw IllegalStateException("You have to set textAppearance via XML or programmatically!")
            }

            if (buttonFontId != -1) {
                val customFont = ResourcesCompat.getFont(context, buttonFontId)
                customFont?.let { typeface = it }
            }

            setOnClickListener { action.invoke() }
        }
    }

    private fun createButton(@DrawableRes drawable: Int, action: () -> Unit): View {
        return ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER
            setImageResource(drawable)
            if (buttonBackground != -1) setBackgroundResource(buttonBackground)
            setOnClickListener { action.invoke() }
        }
    }

    private fun getButtonLayoutParams(column: Int, row: Int): LayoutParams {
        return GridLayout.LayoutParams().apply {
            if (column < COLUMN_COUNT - 1) marginEnd = buttonHorizontalMargin
            if (row < ROW_COUNT - 1) bottomMargin = buttonVerticalMargin
            width = buttonWidth
            height = buttonHeight
        }
    }

    private fun setTextAppearance(buttonTextView: TextView, @StyleRes textAppearance: Int) {
        buttonTextView.setTextAppearance(textAppearance)
    }

    private fun validateAttrs() {
        require(width != -1) { "NumericKeyboard: buttonWidth should be set" }
        require(height != -1) { "NumericKeyboard: buttonHeight should be set" }
    }
}

