package com.whiteelephant.monthpicker

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.prem.firstpitch.R
import java.text.DateFormatSymbols
import java.util.*

class MonthPickerView @JvmOverloads constructor(
		context: Context, private val attrs: AttributeSet? = null, private val defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
	companion object {
		var minYear = 1900
		var maxYear = Calendar.getInstance().get(Calendar.YEAR)
	}

	var yearView: YearPickerView
	private lateinit var monthViewAdapter: MonthViewAdapter
	var monthList: ListView
	var monthTV: TextView
	var yearTV: TextView
	var titleTV: TextView
	var headerFontColorSelected: Int = 0
	var headerFontColorNormal: Int = 0
	var showMonthOnly: Boolean = false
	var month: Int = 0
	var year: Int = 0
	var onYearChanged: MonthPickerDialog.OnYearChangedListener? = null
	var onMonthChanged: MonthPickerDialog.OnMonthChangedListener? = null
	var onDateSet: OnDateSet? = null
	var onCancel: OnCancel? = null
	private var monthNames: Array<String> = arrayOf()
	private var headerBgColor: Int = ContextCompat.getColor(context, R.color.fontWhiteEnable)
	private var monthBgColor: Int = ContextCompat.getColor(context, R.color.fontWhiteEnable)
	private var monthBgSelectedColor: Int = 0
	private var monthFontColorNormal: Int = 0
	private var monthFontColorSelected: Int = 0
	private var monthFontColorDisabled: Int = 0
	private var actionButtonColor: Int = 0
	private var headerTitleColor: Int = 0

	var defaultLocale: Locale? = Locale.getDefault()
		get() = field ?: Locale.getDefault()
		set(value) {
			field = value ?: Locale.getDefault()
			monthNames = DateFormatSymbols(field).shortMonths
			monthViewAdapter.monthNames = monthNames
		}

	var configChangeListener: MonthPickerDialog.OnConfigChangeListener? = null

	init {

		inflate(context, R.layout.month_picker_view, this)

		monthList = findViewById<View>(R.id.listview) as ListView
		yearView = findViewById<View>(R.id.yearView) as YearPickerView
		monthTV = findViewById<View>(R.id.month) as TextView
		yearTV = findViewById<View>(R.id.year) as TextView
		titleTV = findViewById<View>(R.id.title) as TextView

		monthViewAdapter = MonthViewAdapter(context)

		val a = context.obtainStyledAttributes(attrs, R.styleable.MonthPickerView,
				defStyleAttr, 0)

		headerBgColor = a.getColor(R.styleable.MonthPickerView_headerBgColor, 0)
		headerFontColorNormal = a.getColor(R.styleable.MonthPickerView_headerFontColorNormal, 0)
		headerFontColorSelected = a.getColor(R.styleable.MonthPickerView_headerFontColorSelected, 0)
		monthBgColor = a.getColor(R.styleable.MonthPickerView_monthBgColor, 0)
		monthBgSelectedColor = a.getColor(R.styleable.MonthPickerView_monthBgSelectedColor, 0)
		monthFontColorNormal = a.getColor(R.styleable.MonthPickerView_monthFontColorNormal, 0)
		monthFontColorSelected = a.getColor(R.styleable.MonthPickerView_monthFontColorSelected, 0)
		monthFontColorDisabled = a.getColor(R.styleable.MonthPickerView_monthFontColorDisabled, 0)
		headerTitleColor = a.getColor(R.styleable.MonthPickerView_headerTitleColor, 0)
		actionButtonColor = a.getColor(R.styleable.MonthPickerView_dialogActionButtonColor, 0)

		a.recycle()

		if (monthFontColorNormal == 0) {
			monthFontColorNormal = ContextCompat.getColor(context, R.color.fontBlackEnable)
		}

		if (monthFontColorSelected == 0) {
			monthFontColorSelected = ContextCompat.getColor(context, R.color.fontWhiteEnable)
		}

		if (monthFontColorDisabled == 0) {
			monthFontColorDisabled = ContextCompat.getColor(context, R.color.fontBlackDisable)

		}
		if (headerFontColorNormal == 0) {
			headerFontColorNormal = ContextCompat.getColor(context, R.color.fontWhiteDisable)
		}
		if (headerFontColorSelected == 0) {
			headerFontColorSelected = ContextCompat.getColor(context, R.color.fontWhiteEnable)
		}
		if (headerTitleColor == 0) {
			headerTitleColor = ContextCompat.getColor(context, R.color.fontWhiteEnable)
		}
		if (monthBgColor == 0) {
			monthBgColor = ContextCompat.getColor(context, R.color.fontWhiteEnable)
		}

		if (headerBgColor == 0) {
			headerBgColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				android.R.attr.colorAccent
			} else {
				//Get colorAccent defined for AppCompat
				context.resources.getIdentifier("colorAccent",
						"attr", context.packageName)
			}
			val outValue = TypedValue()
			context.theme.resolveAttribute(headerBgColor, outValue, true)
			headerBgColor = outValue.data
		}

		if (monthBgSelectedColor == 0) {

			monthBgSelectedColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				android.R.attr.colorAccent
			} else {
				//Get colorAccent defined for AppCompat
				context.resources.getIdentifier("colorAccent",
						"attr", context.packageName)
			}

			val outValue = TypedValue()
			context.theme.resolveAttribute(monthBgSelectedColor, outValue, true)
			monthBgSelectedColor = outValue.data
		}

	}

	fun init() {

		// getting default values based on the user's theme.

		/*

	   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				headerBgColor = android.R.attr.colorAccent;
			} else {
				//Get colorAccent defined for AppCompat
				headerBgColor = context.getResources().getIdentifier("colorAccent", "attr", context.getPackageName());
			}
			TypedValue outValue = new TypedValue();
			context.getTheme().resolveAttribute(headerBgColor, outValue, true);
			int color = outValue.data;

		// OR
		TypedValue typedValue = new TypedValue();

		TypedArray a = mContext.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent, R.attr.colorPrimary });
		int color = a.getColor(0, 0);

		a.recycle();

		// OR

		final TypedValue value = new TypedValue ();
		context.getTheme ().resolveAttribute (R.attr.colorAccent, value, true);
		int color = value.data
	*/

		val map = HashMap<String, Int>()
		if (monthBgColor != 0)
			map["monthBgColor"] = monthBgColor
		if (monthBgSelectedColor != 0)
			map["monthBgSelectedColor"] = monthBgSelectedColor
		if (monthFontColorNormal != 0)
			map["monthFontColorNormal"] = monthFontColorNormal
		if (monthFontColorSelected != 0)
			map["monthFontColorSelected"] = monthFontColorSelected
		if (monthFontColorDisabled != 0)
			map["monthFontColorDisabled"] = monthFontColorDisabled

		val config = Configuration(context.resources.configuration)
		config.setLocale(defaultLocale)
		val okText = context.createConfigurationContext(config).getText(android.R.string.ok).toString()
		val cancelText = context.createConfigurationContext(config).getText(android.R.string.cancel).toString()

		val pickerBg = findViewById<View>(R.id.picker_view) as RelativeLayout
		val header = findViewById<View>(R.id.header) as LinearLayout
		val actionBtnLay = findViewById<View>(R.id.action_btn_lay) as RelativeLayout
		val ok = findViewById<View>(R.id.ok_action) as TextView
		ok.text = okText
		val cancel = findViewById<View>(R.id.cancel_action) as TextView
		cancel.text = cancelText

		if (actionButtonColor != 0) {
			ok.setTextColor(actionButtonColor)
			cancel.setTextColor(actionButtonColor)
		} else {
			ok.setTextColor(headerBgColor)
			cancel.setTextColor(headerBgColor)
		}

		if (headerFontColorSelected != 0)
			monthTV.setTextColor(headerFontColorSelected)
		if (headerFontColorNormal != 0)
			yearTV.setTextColor(headerFontColorNormal)
		if (headerTitleColor != 0)
			titleTV.setTextColor(headerTitleColor)
		if (headerBgColor != 0)
			header.setBackgroundColor(headerBgColor)
		if (monthBgColor != 0)
			pickerBg.setBackgroundColor(monthBgColor)
		if (monthBgColor != 0)
			actionBtnLay.setBackgroundColor(monthBgColor)

		ok.setOnClickListener { onDateSet?.onDateSet() }
		cancel.setOnClickListener { onCancel?.onCancel() }
		monthViewAdapter.setColors(map)
		monthViewAdapter.setOnDaySelectedListener(object : MonthViewAdapter.OnDaySelectedListener {
			override fun onDaySelected(view: MonthViewAdapter, selectedMonth: Int) {
				Log.d("----------------", "MonthPickerDialogStyle selected month = $selectedMonth")
				this@MonthPickerView.month = selectedMonth
				monthTV.text = monthNames[selectedMonth]
				if (!showMonthOnly) {
					monthList.visibility = View.GONE
					yearView.visibility = View.VISIBLE
					monthTV.setTextColor(headerFontColorNormal)
					yearTV.setTextColor(headerFontColorSelected)
				}
				onMonthChanged?.onMonthChanged(selectedMonth)
			}
		})
		monthList.adapter = monthViewAdapter

		yearView.setRange(minYear, maxYear)
		yearView.colors = map
		yearView.setYear(Calendar.getInstance().get(Calendar.YEAR))
		yearView.onYearSelectedListener = object : YearPickerView.OnYearSelectedListener {
			override fun onYearChanged(view: YearPickerView, year: Int) {
				Log.d("----------------", "selected year = $year")
				this@MonthPickerView.year = year
				yearTV.text = year.toString()
				yearTV.setTextColor(headerFontColorSelected)
				monthTV.setTextColor(headerFontColorNormal)
				onYearChanged?.onYearChanged(year)
			}
		}
		monthTV.setOnClickListener {
			if (monthList.visibility == View.GONE) {
				yearView.visibility = View.GONE
				monthList.visibility = View.VISIBLE
				yearTV.setTextColor(headerFontColorNormal)
				monthTV.setTextColor(headerFontColorSelected)
			}
		}
		yearTV.setOnClickListener {
			if (yearView.visibility == View.GONE) {
				monthList.visibility = View.GONE
				yearView.visibility = View.VISIBLE
				yearTV.setTextColor(headerFontColorSelected)
				monthTV.setTextColor(headerFontColorNormal)
			}
		}
	}

	fun init(year: Int, month: Int) {
		this.year = year
		this.month = month
	}

	fun setMaxMonth(maxMonth: Int) {
		if (maxMonth <= Calendar.DECEMBER && maxMonth >= Calendar.JANUARY) {
			monthViewAdapter.setMaxMonth(maxMonth)
		} else {
			throw IllegalArgumentException("Month out of range please send months between " + "Calendar.JANUARY, Calendar.DECEMBER")
		}
	}


	fun setMinMonth(minMonth: Int) {
		if (minMonth >= Calendar.JANUARY && minMonth <= Calendar.DECEMBER) {
			monthViewAdapter.setMinMonth(minMonth)
		} else {
			throw IllegalArgumentException("Month out of range please send months between" + " Calendar.JANUARY, Calendar.DECEMBER")
		}
	}

	fun setMinYear(minYear: Int) {
		yearView.setMinYear(minYear)
	}

	fun setMaxYear(maxYear: Int) {
		yearView.setMaxYear(maxYear)
	}

	fun showMonthOnly() {
		showMonthOnly = true
		yearTV.visibility = View.GONE
	}

	fun showYearOnly() {
		monthList.visibility = View.GONE
		yearView.visibility = View.VISIBLE

		monthTV.visibility = View.GONE
		yearTV.setTextColor(headerFontColorSelected)
	}

	fun setActivatedMonth(activatedMonth: Int) {
		if (activatedMonth >= Calendar.JANUARY && activatedMonth <= Calendar.DECEMBER) {
			monthViewAdapter.setActivatedMonth(activatedMonth)
			monthTV.text = monthNames[activatedMonth]
		} else {
			throw IllegalArgumentException("Month out of range please send months between Calendar.JANUARY, Calendar.DECEMBER")
		}

	}

	fun setActivatedYear(activatedYear: Int) {
		yearView.setActivatedYear(activatedYear)
		yearTV.text = activatedYear.toString()
	}

	protected fun setMonthRange(minMonth: Int, maxMonth: Int) {
		if (minMonth < maxMonth) {
			setMinMonth(minMonth)
			setMaxYear(maxMonth)
		} else {
			throw IllegalArgumentException("maximum month is less then minimum month")
		}
	}

	protected fun setYearRange(minYear: Int, maxYear: Int) {
		if (minYear < maxYear) {
			setMinYear(minYear)
			setMaxYear(maxYear)
		} else {
			throw IllegalArgumentException("maximum year is less then minimum year")
		}
	}

	protected fun setMonthYearRange(minMonth: Int, maxMonth: Int, minYear: Int, maxYear: Int) {
		setMonthRange(minMonth, maxMonth)
		setYearRange(minYear, maxYear)
	}

	fun setTitle(dialogTitle: String?) {
		if (dialogTitle != null && dialogTitle.trim { it <= ' ' }.isNotEmpty()) {
			titleTV.text = dialogTitle
			titleTV.visibility = View.VISIBLE
		} else {
			titleTV.visibility = View.GONE
		}
	}

	fun setOnMonthChangedListener(onMonthChangedListener: MonthPickerDialog.OnMonthChangedListener?) {
		if (onMonthChangedListener != null) {
			this.onMonthChanged = onMonthChangedListener
		}
	}

	fun setOnYearChangedListener(onYearChangedListener: MonthPickerDialog.OnYearChangedListener?) {
		if (onYearChangedListener != null) {
			this.onYearChanged = onYearChangedListener
		}
	}

	fun setOnDateListener(onDateSet: OnDateSet) {
		this.onDateSet = onDateSet
	}

	fun setOnCancelListener(onCancel: OnCancel) {
		this.onCancel = onCancel
	}


	interface OnDateSet {
		fun onDateSet()
	}

	interface OnCancel {
		fun onCancel()
	}

	fun setOnConfigurationChanged(configChangeListener: MonthPickerDialog.OnConfigChangeListener) {
		this.configChangeListener = configChangeListener
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		configChangeListener?.onConfigChange()
		super.onConfigurationChanged(newConfig)
	}

	fun setLocale(locale: Locale?) {
		defaultLocale = locale
	}
}
