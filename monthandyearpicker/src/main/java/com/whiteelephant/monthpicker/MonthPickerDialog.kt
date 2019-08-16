package com.whiteelephant.monthpicker

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.DatePicker
import androidx.annotation.IntRange
import com.example.prem.firstpitch.R
import java.util.*


class MonthPickerDialog
/**
 * @param context     The context the dialog is to run in.
 * @param theme       the theme to apply to this dialog
 * @param callBack    How the parent is notified that the date is set.
 * @param year        The initial year of the dialog.
 * @param monthOfYear The initial month of the dialog.
 */
private constructor(context: Context,
                    theme: Int,
                    private val _callBack: OnDateSetListener?,
                    year: Int,
                    monthOfYear: Int) : Dialog(context, theme), OnClickListener, DatePicker.OnDateChangedListener {
	/**
	 * Gets the [DatePicker] contained in this dialog.
	 *
	 * @return The calendar view1.
	 */
	private var datePicker: MonthPickerView? = null
	private var view1: View

	/**
	 * @param context     The context the dialog is to run in.
	 * @param callBack    How the parent is notified that the date is set.
	 * @param year        The initial year of the dialog.
	 * @param monthOfYear The initial month of the dialog.
	 */
	private constructor(context: Context,
	                    callBack: OnDateSetListener,
	                    year: Int,
	                    monthOfYear: Int) : this(context, 0, callBack, year, monthOfYear) {
	}

	init {
		requestWindowFeature(Window.FEATURE_NO_TITLE) // before
		window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

		val inflater = context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		view1 = inflater.inflate(R.layout.month_picker_dialog, null)

		setContentView(view1)
		datePicker = view1.findViewById<View>(R.id.monthPicker) as MonthPickerView
		datePicker?.setOnDateListener(object : MonthPickerView.OnDateSet {
			override fun onDateSet() {
				tryNotifyDateSet()
				this@MonthPickerDialog.dismiss()
			}
		})
		datePicker?.setOnCancelListener(object : MonthPickerView.OnCancel {
			override fun onCancel() {
				this@MonthPickerDialog.dismiss()
			}
		})

		// to show dialog bigger view1 in landscape mode we are increasing the
		// height and width of the dialog. If we do that android don't dismiss the dialog after
		// rotation and try to render landscape UI in portrait mode which is not correct.
		// so dismissing the dialog on each time when orientation changes.
		datePicker?.setOnConfigurationChanged(object : OnConfigChangeListener {
			override fun onConfigChange() {
				dismiss()
			}
		})
		datePicker?.init(year, monthOfYear)
	}

	override fun show() {
		if (this.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			val lp = WindowManager.LayoutParams()
			if (window != null) {
				lp.copyFrom(window!!.attributes)
				lp.width = (this.context.resources.displayMetrics.widthPixels * 0.94).toInt()
				lp.height = (this.context.resources.displayMetrics.heightPixels * 0.94).toInt()
				// show the dialog as per super implementation
				super.show()
				// now dialog attached to window so apply the size
				window?.setLayout(lp.width, lp.height)
			}

			return
		} else {
			dismiss()
		}
		super.show()
	}

	override fun onClick(dialog: DialogInterface, which: Int) {
		tryNotifyDateSet()
	}

	override fun onDateChanged(view: DatePicker, year: Int, month: Int, day: Int) {
		datePicker?.init(year, month)
	}

	internal fun tryNotifyDateSet() {
		if (_callBack != null) {
			datePicker?.clearFocus()
			datePicker?.run {
				_callBack.onDateSet(month, year)
			}
		}
	}

	private fun setMinMonth(minMonth: Int) {
		datePicker?.setMinMonth(minMonth)
	}

	private fun setMaxMonth(maxMonth: Int) {
		datePicker?.setMaxMonth(maxMonth)
	}

	private fun setMinYear(minYear: Int) {
		datePicker?.setMinYear(minYear)
	}

	private fun setMaxYear(maxYear: Int) {
		datePicker?.setMaxYear(maxYear)
	}

	private fun setActivatedMonth(activatedMonth: Int) {
		datePicker?.setActivatedMonth(activatedMonth)
	}

	private fun setActivatedYear(activatedMonth: Int) {
		datePicker?.setActivatedYear(activatedMonth)
	}

	private fun setLocale(locale: Locale?) {
		datePicker?.setLocale(locale)
	}

	private fun setMonthPickerTitle(title: String) {
		datePicker?.setTitle(title)
	}

	private fun showMonthOnly() {
		datePicker?.showMonthOnly()
	}

	private fun showYearOnly() {
		datePicker?.showYearOnly()
	}

	private fun setOnMonthChangedListener(onMonthChangedListener: OnMonthChangedListener?) {
		if (onMonthChangedListener != null) {
			datePicker?.setOnMonthChangedListener(onMonthChangedListener)
		}
	}

	private fun setOnYearChangedListener(onYearChangedListener: OnYearChangedListener?) {
		if (onYearChangedListener != null) {
			datePicker?.setOnYearChangedListener(onYearChangedListener)
		}
	}

	private fun init() {
		datePicker?.init()
	}

	class Builder
	/**
	 * Build a Dialog with month and year with given context.
	 *
	 * @param context  Context: the parent context
	 * @param callBack MonthPickerDialog.OnDateSetListener: the listener to call
	 * when the user sets the date
	 * @param year     the initially selected year
	 * @param month    the initially selected month (0-11 for compatibility with
	 * [Calendar]Calender.MONTH or Calendar.JANUARY, Calendar.FEBRUARY etc)
	 */
	(private val _context: Context,
	 private val _callBack: OnDateSetListener,
	 year: Int,
	 @IntRange(from = Calendar.JANUARY.toLong(), to = Calendar.DECEMBER.toLong()) month: Int) {
		companion object {
			private val TAG = MonthPickerDialog.Builder::class.java.name
		}

		private var _activatedMonth: Int = 0
		private var _activatedYear: Int = 0
		private var _minMonth = Calendar.JANUARY
		private var _maxMonth = Calendar.DECEMBER
		private var _minYear: Int = 0
		private var _maxYear: Int = 0
		private var monthOnly: Boolean = false
		private var yearOnly: Boolean = false
		private var title: String? = null
		private var monthPickerDialog: MonthPickerDialog? = null
		private var _onYearChanged: OnYearChangedListener? = null
		private var _onMonthChanged: OnMonthChangedListener? = null
		private var defaultLocale: Locale? = null

		init {
			if (month >= Calendar.JANUARY && month <= Calendar.DECEMBER) {
				this._activatedMonth = month
			} else {
				throw IllegalArgumentException("Month range should be between 0 " + "(Calender.JANUARY) to 11 (Calendar.DECEMBER)")
			}

			if (year >= 1) {
				this._activatedYear = year
			} else {
				throw IllegalArgumentException("Selected year should be > 1")
			}

			if (year > MonthPickerView.minYear) {
				_minYear = MonthPickerView.minYear
			} else {
				_minYear = year
				MonthPickerView.minYear = year
			}

			if (year > MonthPickerView.maxYear) {
				_maxYear = year
				MonthPickerView.maxYear = year
			} else {
				_maxYear = MonthPickerView.maxYear
			}
		}

		/**
		 * Minimum enable month in picker (0-11 for compatibility with Calender.MONTH or
		 * Calendar.JANUARY, Calendar.FEBRUARY etc).
		 *
		 * @param minMonth
		 * @return Builder
		 */
		fun setMinMonth(@IntRange(from = Calendar.JANUARY.toLong(), to = Calendar.DECEMBER.toLong())
		                minMonth: Int): Builder {
			if (minMonth >= Calendar.JANUARY && minMonth <= Calendar.DECEMBER) {
				this._minMonth = minMonth
				return this
			} else {
				throw IllegalArgumentException("Month range should be between 0 " + "(Calender.JANUARY) to 11 (Calendar.DECEMBER)")
			}
		}

		/**
		 * Maximum enabled month in picker (0-11 for compatibility with Calender.MONTH or
		 * Calendar.JANUARY, Calendar.FEBRUARY etc).
		 *
		 * @param maxMonth
		 * @return
		 */
		fun setMaxMonth(@IntRange(from = Calendar.JANUARY.toLong(), to = Calendar.DECEMBER.toLong())
		                maxMonth: Int): Builder {
			/* if (maxMonth >= Calendar.JANUARY && maxMonth <= Calendar.DECEMBER) {*/
			this._maxMonth = maxMonth
			return this
			/*} else {
                throw new IllegalArgumentException("Month range should be between 0 " +
                        "(Calender.JANUARY) to 11 (Calendar.DECEMBER)");
            }*/
		}


		/**
		 * Starting year in the picker.
		 *
		 * @param minYear
		 * @return Builder
		 */
		fun setMinYear(minYear: Int): Builder {
			this._minYear = minYear
			return this
		}

		/**
		 * Ending year in the picker.
		 *
		 * @param maxYear
		 * @return Builder
		 */
		fun setMaxYear(maxYear: Int): Builder {
			this._maxYear = maxYear
			return this
		}

		/**
		 * Initially selected month (0-11 for compatibility with Calender.MONTH or
		 * Calendar.JANUARY, Calendar.FEBRUARY etc).
		 *
		 * @param activatedMonth
		 * @return Builder
		 */
		fun setActivatedMonth(@IntRange(from = Calendar.JANUARY.toLong(), to = Calendar.DECEMBER.toLong())
		                      activatedMonth: Int): Builder {
			this._activatedMonth = activatedMonth
			return this
		}

		/**
		 * Initially selected year (0-11 for compatibility with Calender.MONTH or
		 * Calendar.JANUARY, Calendar.FEBRUARY etc).
		 *
		 * @param activatedYear
		 * @return Builder
		 */
		fun setActivatedYear(activatedYear: Int): Builder {
			this._activatedYear = activatedYear
			return this
		}


		/**
		 * Minimum and Maximum enable month in picker (0-11 for compatibility with Calender.MONTH or
		 * Calendar.JANUARY, Calendar.FEBRUARY etc).
		 *
		 * @param minMonth minimum enabled month.
		 * @param maxMonth maximum enabled month.
		 * @return Builder
		 */
		fun setMonthRange(@IntRange(from = Calendar.JANUARY.toLong(), to = Calendar.DECEMBER.toLong())
		                  minMonth: Int,
		                  @IntRange(from = Calendar.JANUARY.toLong(), to = Calendar.DECEMBER.toLong())
		                  maxMonth: Int): Builder {
			if (minMonth >= Calendar.JANUARY && minMonth <= Calendar.DECEMBER &&
					maxMonth >= Calendar.JANUARY && maxMonth <= Calendar.DECEMBER) {
				this._minMonth = minMonth
				this._maxMonth = maxMonth
				return this
			} else {
				throw IllegalArgumentException("Month range should be between 0 " + "(Calender.JANUARY) to 11 (Calendar.DECEMBER)")
			}
		}

		/**
		 * Starting and ending year show in picker
		 *
		 * @param minYear starting year
		 * @param maxYear ending year
		 * @return
		 */
		fun setYearRange(minYear: Int, maxYear: Int): Builder {
			if (minYear <= maxYear) {
				this._minYear = minYear
				this._maxYear = maxYear
				return this
			} else {
				throw IllegalArgumentException("Minimum year should be less then Maximum year")
			}

		}

		/**
		 * Set the Minimum, maximum enabled months and starting , ending years.
		 *
		 * @param minMonth minimum enabled month in picker
		 * @param maxMonth maximum enabled month in picker
		 * @param minYear  starting year
		 * @param maxYear  ending year
		 * @return
		 */
		fun setMonthAndYearRange(@IntRange(from = Calendar.JANUARY.toLong(), to = Calendar.DECEMBER.toLong())
		                         minMonth: Int,
		                         @IntRange(from = Calendar.JANUARY.toLong(), to = Calendar.DECEMBER.toLong())
		                         maxMonth: Int,
		                         minYear: Int, maxYear: Int): Builder {
			if (minMonth >= Calendar.JANUARY && minMonth <= Calendar.DECEMBER &&
					maxMonth >= Calendar.JANUARY && maxMonth <= Calendar.DECEMBER) {
				this._minMonth = minMonth
				this._maxMonth = maxMonth

			} else {
				throw IllegalArgumentException("Month range should be between 0 " + "(Calender.JANUARY) to 11 (Calendar.DECEMBER)")
			}

			if (minYear <= maxYear) {
				this._minYear = minYear
				this._maxYear = maxYear
			} else {
				throw IllegalArgumentException("Minimum year should be less then Maximum year")
			}
			return this
		}

		/**
		 * User can select month only. Year won't be shown to user once user select the month.
		 *
		 * @return Builder
		 */
		fun showMonthOnly(): Builder {

			if (yearOnly) {
				Log.e(TAG, "yearOnly also set to true before. Now setting yearOnly to false" + " monthOnly to true")
			}
			this.yearOnly = false
			this.monthOnly = true
			return this
		}

		/**
		 * User can select year only. Month won't be shown to user once user select the month.
		 *
		 * @return Builder
		 */
		fun showYearOnly(): Builder {
			if (monthOnly) {
				Log.e(TAG, "monthOnly also set to true before. Now setting monthOnly to " + "false and yearOnly to true")
			}
			this.monthOnly = false
			this.yearOnly = true
			return this
		}

		/**
		 * Set the title to the picker.
		 *
		 * @param title
		 * @return Builder
		 */
		fun setTitle(title: String?): Builder {
			this.title = title
			return this
		}

		/**
		 * Sets the callback that will be called when user click on any month.
		 *
		 * @param onMonthChangedListener
		 * @return Builder
		 */
		fun setOnMonthChangedListener(onMonthChangedListener: OnMonthChangedListener): Builder {
			this._onMonthChanged = onMonthChangedListener
			return this
		}

		/**
		 * Sets the callback that will be called when the user select any year.
		 *
		 * @param onYearChangedListener
		 * @return Builder
		 */
		fun setOnYearChangedListener(onYearChangedListener: OnYearChangedListener): Builder {
			this._onYearChanged = onYearChangedListener
			return this
		}

		fun build(): MonthPickerDialog {

			if (_minMonth > _maxMonth) {
				throw IllegalArgumentException("Minimum month should always " + "smaller then maximum month.")
			}

			if (_minYear > _maxYear) {
				throw IllegalArgumentException("Minimum year should always " + "smaller then maximum year.")
			}

			if (_activatedMonth < _minMonth || _activatedMonth > _maxMonth) {
				throw IllegalArgumentException("Activated month should always " + "in between Minimum and maximum month.")
			}

			if (_activatedYear < _minYear || _activatedYear > _maxYear) {
				throw IllegalArgumentException("Activated year should always " + "in between Minimum year and maximum year.")
			}


			monthPickerDialog = MonthPickerDialog(_context, _callBack, _activatedYear, _activatedMonth)
			if (monthOnly) {
				monthPickerDialog?.showMonthOnly()
				_minYear = 0
				_maxYear = 0
				_activatedYear = 0
			} else if (yearOnly) {
				monthPickerDialog?.showYearOnly()
				_minMonth = 0
				_maxMonth = 0
				_activatedMonth = 0
			}

			monthPickerDialog?.apply {
				setLocale(defaultLocale)
				setMinMonth(_minMonth)
				setMaxMonth(_maxMonth)
				setMinYear(_minYear)
				setMaxYear(_maxYear)
				setActivatedMonth(_activatedMonth)
				setActivatedYear(_activatedYear)
				init()
			}

			_onMonthChanged?.run { monthPickerDialog?.setOnMonthChangedListener(this) }

			_onYearChanged?.run { monthPickerDialog?.setOnYearChangedListener(this) }

			if (title != null) {
				monthPickerDialog?.setMonthPickerTitle(title!!.trim { it <= ' ' })
			}
			return monthPickerDialog!!
		}

		fun setLocale(locale: Locale?): Builder {
			defaultLocale = locale

			return this
		}
	}

	/**
	 * The callback used to indicate the user is done selecting month.
	 */
	interface OnDateSetListener {
		/**
		 * @param selectedMonth The month that was set (0-11) for compatibility with [Calendar].
		 * @param selectedYear  The year that was set.
		 */
		fun onDateSet(selectedMonth: Int, selectedYear: Int)
	}

	/**
	 * The callback used to indicate the user click on month
	 */
	interface OnMonthChangedListener {
		/**
		 * @param selectedMonth The month that was set (0-11) for compatibility
		 * with [Calendar].
		 */
		fun onMonthChanged(selectedMonth: Int)
	}

	/**
	 * The callback used to indicate the user click on year.
	 */
	interface OnYearChangedListener {
		/**
		 * Called upon a year change.
		 *
		 * @param year The year that was set.
		 */
		fun onYearChanged(year: Int)
	}

	interface OnConfigChangeListener {

		fun onConfigChange()
	}
}
