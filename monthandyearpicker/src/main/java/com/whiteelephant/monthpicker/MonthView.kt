package com.whiteelephant.monthpicker

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.widget.ListView
import com.example.prem.firstpitch.R
import java.text.DateFormatSymbols
import java.util.*

internal class MonthView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.style.MonthPickerDialogStyle) : ListView(context, attrs, defStyleAttr) {
	companion object {
		// constants
		private const val DEFAULT_HEIGHT = 100
		private const val DEFAULT_NUM_DAYS = 4
		private const val DEFAULT_NUM_ROWS = 3
		private const val MAX_NUM_ROWS = 3
		private const val DAY_SEPARATOR_WIDTH = 1

		private const val RECT_RADIUS = 37.5f
		private const val VERTICAL_OFFSET = 100f
	}

	// days to display
	private val numDays = DEFAULT_NUM_DAYS
	private var _numCells = numDays
	private var _numRows = DEFAULT_NUM_ROWS
	// layout padding
	private var _padding = 40
	private var _width: Int = 0
	private var rowHeight = DEFAULT_HEIGHT
	// paints
	private lateinit var monthNumberPaint: Paint
	private lateinit var monthNumberDisabledPaint: Paint
	private lateinit var _monthNumberSelectedPaint: Paint
	// month
	private val _monthNames: Array<String> = DateFormatSymbols(Locale.getDefault()).shortMonths
	private val monthTextSize: Int
	private val monthHeaderSize: Int
	private var monthSelectedCircleSize: Int = 0
	private var _monthBgSelectedColor: Int = 0
	private var monthFontColorNormal: Int = 0
	private var monthFontColorSelected: Int = 0
	private var _monthFontColorDisabled: Int = 0
	private var _maxMonth: Int = 0
	private var _minMonth: Int = 0
	private val _rowHeightKey: Int
	private var selectedMonth = -1
	// listener
	private var _onMonthClickListener: OnMonthClickListener? = null

	init {

		val displayMetrics = context.resources.displayMetrics

		monthTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				16f, displayMetrics).toInt()
		monthHeaderSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				16f, displayMetrics).toInt()

		monthSelectedCircleSize = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
			TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
					43f, displayMetrics).toInt()
		} else {
			TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
					43f, displayMetrics).toInt()
		}

		_rowHeightKey = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				250f, displayMetrics).toInt()
		rowHeight = (_rowHeightKey - monthHeaderSize) / MAX_NUM_ROWS

		_padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				16f, displayMetrics).toInt()
	}


	/**
	 * Sets up the text and style properties for painting.
	 */
	private fun initView() {

		_monthNumberSelectedPaint = Paint()
		_monthNumberSelectedPaint.isAntiAlias = true
		if (_monthBgSelectedColor != 0)
			_monthNumberSelectedPaint.color = _monthBgSelectedColor
		// _monthNumberSelectedPaint.setAlpha(200);
		_monthNumberSelectedPaint.textAlign = Paint.Align.CENTER
		_monthNumberSelectedPaint.style = Paint.Style.FILL
		_monthNumberSelectedPaint.isFakeBoldText = true

		monthNumberPaint = Paint()
		monthNumberPaint.isAntiAlias = true
		if (monthFontColorNormal != 0)
			monthNumberPaint.color = monthFontColorNormal
		monthNumberPaint.textSize = monthTextSize.toFloat()
		monthNumberPaint.textAlign = Paint.Align.CENTER
		monthNumberPaint.style = Paint.Style.FILL
		monthNumberPaint.isFakeBoldText = false

		monthNumberDisabledPaint = Paint()
		monthNumberDisabledPaint.isAntiAlias = true
		if (_monthFontColorDisabled != 0)
			monthNumberDisabledPaint.color = _monthFontColorDisabled
		monthNumberDisabledPaint.textSize = monthTextSize.toFloat()
		monthNumberDisabledPaint.textAlign = Paint.Align.CENTER
		monthNumberDisabledPaint.style = Paint.Style.FILL
		monthNumberDisabledPaint.isFakeBoldText = false
	}

	override fun onDraw(canvas: Canvas) {
		drawDays(canvas)
	}

	/**
	 * Draws the month days.
	 */
	private fun drawDays(canvas: Canvas) {
		var y = (rowHeight + monthTextSize) / 2 - DAY_SEPARATOR_WIDTH + monthHeaderSize
		val dayWidthHalf = (_width - _padding * 2) / (numDays * 2)
		var j = 0
		for (month in _monthNames.indices) {
			val x = (2 * j + 1) * dayWidthHalf + _padding

			if (selectedMonth == month) {
				val yy = (y - monthTextSize / 3).toFloat()

				canvas.drawRoundRect(RectF(x.toFloat() - VERTICAL_OFFSET, yy - 50f, x.toFloat() + VERTICAL_OFFSET, yy + 50f), RECT_RADIUS, RECT_RADIUS, _monthNumberSelectedPaint)

				if (monthFontColorSelected != 0) {
					monthNumberPaint.color = monthFontColorSelected
				}
			} else {
				if (monthFontColorNormal != 0) {
					monthNumberPaint.color = monthFontColorNormal
				}
			}

			val paint = if (month < _minMonth || month > _maxMonth) {
				monthNumberDisabledPaint
			} else {
				monthNumberPaint
			}

			canvas.drawText(_monthNames[month], x.toFloat(), y.toFloat(), paint)
			j++
			if (j == numDays) {
				j = 0
				y += rowHeight
			}
		}
	}


	/**
	 * Calculates the day that the given x position is in, accounting for week
	 * number. Returns the day or -1 if the position wasn't in a day.
	 *
	 * @param x The x position of the touch event
	 * @return The day number, or -1 if the position wasn't in a day
	 */
	private fun getMonthFromLocation(x: Float, y: Float): Int {
		val dayStart = _padding
		if (x < dayStart || x > _width - _padding) {
			return -1
		}
		// Selection is (x - start) / (pixels/day) == (x -s) * day / pixels
		val row = (y - monthHeaderSize).toInt() / rowHeight
		val column = ((x - dayStart) * numDays / (_width - dayStart - _padding)).toInt()
		var day = column + 1
		day += row * numDays
		return if (day < 0 || day > _numCells) {
			-1
		} else day - 1
		// position - 1 to match with Calender.JANUARY and Calender.DECEMBER
	}

	/**
	 * Called when the user clicks on a day. Handles callbacks to the
	 * [OnMonthClickListener] if one is set.
	 *
	 * @param day The day that was clicked
	 */
	private fun onDayClick(day: Int) {
		playSoundEffect(SoundEffectConstants.CLICK)
		_onMonthClickListener?.onMonthClick(this, day)
	}

	fun setColors(colors: HashMap<String, Int>) {
		if (colors.containsKey("monthBgSelectedColor"))
			_monthBgSelectedColor = colors["monthBgSelectedColor"]!!.toInt()
		if (colors.containsKey("monthFontColorNormal"))
			monthFontColorNormal = colors["monthFontColorNormal"]!!.toInt()
		if (colors.containsKey("monthFontColorSelected"))
			monthFontColorSelected = colors["monthFontColorSelected"]!!.toInt()
		if (colors.containsKey("monthFontColorDisabled"))
			_monthFontColorDisabled = colors["monthFontColorDisabled"]!!.toInt()
		initView()
	}

	/**
	 * Handles callbacks when the user clicks on a time object.
	 */
	interface OnMonthClickListener {
		fun onMonthClick(view: MonthView, month: Int)
	}

	fun setOnMonthClickListener(listener: OnMonthClickListener) {
		_onMonthClickListener = listener
	}

	fun setMonthParams(selectedMonth: Int, minMonth: Int, maxMonth: Int) {
		this.selectedMonth = selectedMonth
		this._minMonth = minMonth
		this._maxMonth = maxMonth
		_numCells = 12

	}

	fun reuse() {
		_numRows = DEFAULT_NUM_ROWS
	}


	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), rowHeight * _numRows + monthHeaderSize * 2)
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		_width = w
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		when (event.action) {
			MotionEvent.ACTION_UP -> {
				val day = getMonthFromLocation(event.x, event.y)
				if (day >= 0) {
					onDayClick(day)
				}
			}
			MotionEvent.ACTION_DOWN -> {

			}
		}
		return true
	}
}
