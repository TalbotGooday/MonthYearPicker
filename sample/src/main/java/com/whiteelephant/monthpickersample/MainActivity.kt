package com.whiteelephant.monthpickersample

import android.app.DatePickerDialog
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.whiteelephant.monthpicker.MonthPickerDialog
import java.util.*

class MainActivity : AppCompatActivity() {
	companion object {
		private const val TAG = "MainActivity"
	}

	internal var chosenYear = 2017

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		applyLocale()

		setNormalPicker()

		// goto styles.xml and change the monthPickerStyles for below three layouts

		//setBottleView()
		//chooseMonthOnly()
		//chooseYearOnly()
	}


	private fun applyLocale() {
		val locale = Locale("ru")
		Locale.setDefault(locale)

		val res = resources
		val config = Configuration(res.configuration)
		config.locale = locale
		baseContext.resources.updateConfiguration(config,
				baseContext.resources.displayMetrics)
	}

	private fun setNormalPicker() {
		setContentView(R.layout.activity_main)
		val today = Calendar.getInstance()
		findViewById<View>(R.id.month_picker).setOnClickListener {
			val builder = MonthPickerDialog.Builder(this@MainActivity, object : MonthPickerDialog.OnDateSetListener {
				override fun onDateSet(selectedMonth: Int, selectedYear: Int) {
					Log.d(TAG, "selectedMonth : $selectedMonth selectedYear : $selectedYear")
					Toast.makeText(this@MainActivity, "Date set with month$selectedMonth year $selectedYear", Toast.LENGTH_SHORT).show()
				}
			}, today.get(Calendar.YEAR), today.get(Calendar.MONTH))

			builder.setActivatedMonth(Calendar.JULY)
					.setMinYear(1990)
					.setActivatedYear(2017)
					.setMaxYear(2030)
					.setMinMonth(Calendar.FEBRUARY)
					.setTitle(null)
					.setMonthRange(Calendar.FEBRUARY, Calendar.NOVEMBER)
					// .setMaxMonth(Calendar.OCTOBER)
					// .setYearRange(1890, 1890)
					// .setMonthAndYearRange(Calendar.FEBRUARY, Calendar.OCTOBER, 1890, 1890)
					//.showMonthOnly()
					// .showYearOnly()
					.setOnMonthChangedListener(object : MonthPickerDialog.OnMonthChangedListener {
						override fun onMonthChanged(selectedMonth: Int) {
							Log.d(TAG, "Selected month : $selectedMonth")
							// Toast.makeText(MainActivity.this, " Selected month : " + selectedMonth, Toast.LENGTH_SHORT).show();
						}
					})
					.setOnYearChangedListener(object : MonthPickerDialog.OnYearChangedListener {
						override fun onYearChanged(selectedYear: Int) {
							Log.d(TAG, "Selected year : $selectedYear")
							// Toast.makeText(MainActivity.this, " Selected year : " + selectedYear, Toast.LENGTH_SHORT).show();
						}
					})
					.build()
					.show()
		}

		findViewById<View>(R.id.date_picker).setOnClickListener {
			val cal = Calendar.getInstance()
			val dialog = DatePickerDialog(this@MainActivity, null, 2017,
					cal.get(Calendar.MONTH), cal.get(Calendar.DATE))
			dialog.show()
		}
	}

	private fun chooseMonthOnly() {
		setContentView(R.layout.activity_choose_month)

		findViewById<View>(R.id.choose_month).setOnClickListener {
			val builder = MonthPickerDialog.Builder(this@MainActivity, object : MonthPickerDialog.OnDateSetListener {
				override fun onDateSet(selectedMonth: Int, selectedYear: Int) {

				}
			}, /* activated number in year */ 3, 5)

			builder.showMonthOnly()
					.build()
					.show()
		}
	}

	private fun chooseYearOnly() {
		setContentView(R.layout.activity_choose_year)

		val year = findViewById<View>(R.id.year) as TextView
		findViewById<View>(R.id.choose_year).setOnClickListener {
			val builder = MonthPickerDialog.Builder(this@MainActivity, object : MonthPickerDialog.OnDateSetListener {
				override fun onDateSet(selectedMonth: Int, selectedYear: Int) {
					year.text = selectedYear.toString()
					chosenYear = selectedYear
				}
			}, chosenYear, 0)

			builder.showYearOnly()
					.setYearRange(1990, 2030)
					.build()
					.show()
		}
	}

	private fun setBottleView() {

		setContentView(R.layout.activity_bottle)

		val chooseQty = findViewById<View>(R.id.select_quantity) as LinearLayout
		val qty = findViewById<View>(R.id.qty) as TextView


		chooseQty.setOnClickListener {
			val builder = MonthPickerDialog.Builder(this@MainActivity, object : MonthPickerDialog.OnDateSetListener {
				override fun onDateSet(selectedMonth: Int, selectedYear: Int) {
					qty.text = selectedYear.toString()
				}
			}, /* activated number in year */ 3, 0)

			builder.setActivatedMonth(Calendar.JULY)
					// .setMaxMonth(Calendar.OCTOBER)
					//.setMinYear(1990)
					//.setActivatedYear(3)
					//.setMinMonth(Calendar.FEBRUARY)
					//.setMaxYear(2030)
					.setTitle("Select Quantity")
					//.setMonthRange(Calendar.FEBRUARY, Calendar.NOVEMBER)
					.setYearRange(1, 15)
					// .setMonthAndYearRange(Calendar.FEBRUARY, Calendar.OCTOBER, 1890, 1890)
					//.showMonthOnly()
					.showYearOnly()
					.build()
					.show()
		}

	}

}
