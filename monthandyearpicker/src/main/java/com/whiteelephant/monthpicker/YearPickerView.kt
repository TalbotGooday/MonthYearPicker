package com.whiteelephant.monthpicker

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.prem.firstpitch.R
import java.util.*


class YearPickerView(private val _context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ListView(_context, attrs, defStyleAttr) {
	private var _adapter: YearAdapter? = null
	val viewSize: Int
	val childSize: Int
	var onYearSelectedListener: OnYearSelectedListener? = null
	var colors: HashMap<String, Int>? = null

	val firstPositionOffset: Int
		get() {
			val firstChild = getChildAt(0) ?: return 0
			return firstChild.top
		}

	@JvmOverloads
	constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, R.style.AppTheme) {
		super.setSelector(android.R.color.transparent)
	}

	init {
		val frame = LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
		layoutParams = frame
		val res = _context.resources
		viewSize = res.getDimensionPixelOffset(R.dimen.datepicker_view_animator_height)
		childSize = res.getDimensionPixelOffset(R.dimen.datepicker_year_label_height)
		onItemClickListener = OnItemClickListener { parent, view, position, id ->
			_adapter?.getYearForPosition(position)?.run {
				_adapter?.setSelection(this)
				onYearSelectedListener?.onYearChanged(this@YearPickerView, this)
			}

		}
		_adapter = YearAdapter(context)
		adapter = _adapter
	}

	/**
	 * Sets the currently selected year. Jumps immediately to the new year.
	 *
	 * @param year the target year
	 */
	fun setYear(year: Int) {
		_adapter?.setSelection(year)
		post {
			val position = _adapter?.getPositionForYear(year) ?: 0
			if (position >= 0 /*&& position < getCount()*/) {
				setSelectionCentered(position)
			}
		}
	}

	fun setSelectionCentered(position: Int) {
		val offset = viewSize / 2 - childSize / 2
		setSelectionFromTop(position, offset)
	}

	fun setRange(min: Int, max: Int) {
		_adapter?.setRange(min, max)
	}

	private inner class YearAdapter(context: Context) : BaseAdapter() {
		private val layout = R.layout.year_label_text_view
		private val inflater: LayoutInflater = LayoutInflater.from(context)
		private var activatedYear: Int = 0
		private var minYear: Int = 0
		private var maxYear: Int = 0
		private var count: Int = 0

		fun setRange(min: Int, max: Int) {
			val yearCount = max - min + 1
			if (minYear != min || maxYear != max || count != yearCount) {
				minYear = min
				maxYear = max
				count = yearCount
				notifyDataSetInvalidated()
			}
		}

		fun setSelection(year: Int): Boolean {
			if (activatedYear != year) {
				activatedYear = year
				notifyDataSetChanged()
				return true
			}
			return false
		}

		override fun getCount(): Int {
			return count
		}

		override fun getItem(position: Int): Int? {
			return getYearForPosition(position)
		}

		override fun getItemId(position: Int): Long {
			return getYearForPosition(position).toLong()
		}

		fun getPositionForYear(year: Int): Int {
			return year - minYear
		}

		fun getYearForPosition(position: Int): Int {
			return minYear + position
		}

		override fun hasStableIds(): Boolean {
			return true
		}

		override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
			val v: TextView?
			val hasNewView = convertView == null
			v = if (hasNewView) {
				inflater.inflate(layout, parent, false) as TextView
			} else {
				convertView as? TextView?
			}
			val year = getYearForPosition(position)
			val activated = activatedYear == year

			val colors = colors

			if(colors != null && v != null) {
				if (hasNewView || v.tag != null || v.tag == activated) {
					if (activated) {
						if (colors.containsKey("monthBgSelectedColor")) {
							colors["monthBgSelectedColor"]?.run { v.setTextColor(this) }
						}
						v.textSize = 25f
					} else {
						if (colors.containsKey("monthFontColorNormal")) {
							colors["monthFontColorNormal"]?.run { v.setTextColor(this) }
						}
						v.textSize = 20f
					}
					v.tag = activated

				}
				v.text = year.toString()
			}
			return v
		}

		override fun getItemViewType(position: Int): Int {
			return 0
		}

		override fun getViewTypeCount(): Int {
			return 1
		}

		override fun isEmpty(): Boolean {
			return false
		}

		override fun areAllItemsEnabled(): Boolean {
			return true
		}

		override fun isEnabled(position: Int): Boolean {
			return true
		}

		fun setMaxYear(maxYear: Int) {
			this.maxYear = maxYear
			count = this.maxYear - minYear + 1
			notifyDataSetInvalidated()
		}

		fun setMinYear(minYear: Int) {
			this.minYear = minYear
			count = maxYear - this.minYear + 1
			notifyDataSetInvalidated()
		}

		fun setActivatedYear(activatedYear: Int) {
			if (activatedYear in minYear..maxYear) {
				this.activatedYear = activatedYear
				setYear(activatedYear)
			} else {
				throw IllegalArgumentException("activated date is not in range")
			}
		}

	}

	/**
	 * The callback used to indicate the user changed the year.
	 */
	interface OnYearSelectedListener {
		/**
		 * Called upon a year change.
		 *
		 * @param view The view associated with this listener.
		 * @param year The year that was set.
		 */
		fun onYearChanged(view: YearPickerView, year: Int)
	}

	fun setMinYear(minYear: Int) {
		_adapter?.setMinYear(minYear)
	}

	fun setMaxYear(maxYear: Int) {
		_adapter?.setMaxYear(maxYear)
	}

	fun setActivatedYear(activatedYear: Int) {
		_adapter?.setActivatedYear(activatedYear)
	}

}

