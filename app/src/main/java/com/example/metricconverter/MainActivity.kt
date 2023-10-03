package com.example.metricconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.pow

data class MetricInfo(
    val units: MutableList<String>,
    val unitFactor: Int = 10,
)

class MainActivity : AppCompatActivity() {
    private val metricInfo: HashMap<String, MetricInfo> = hashMapOf(
        "panjang" to MetricInfo (
            units = mutableListOf(
                "km",
                "hm",
                "dam",
                "m",
                "dm",
                "cm",
                "m"
            )
        ),
        "massa" to MetricInfo (
            units = mutableListOf(
                "kg",
                "hg",
                "dag",
                "g",
                "dg",
                "cg",
                "mg"
            )
        ),
        "waktu" to MetricInfo (
            units = mutableListOf(
                "jam",
                "menit",
                "detik"
            ),
            unitFactor = 60
        ),
        "kuat arus" to MetricInfo(
            units = mutableListOf(
                "kA",
                "hA",
                "dA",
                "A",
                "dA",
                "cA",
                "mA",
            )
        )
    )
    private var metricSelected: String = "panjang"
    private var selectedmetricInfo = metricInfo[metricSelected]
    private var fromUnitIndex: Int = 0
    private var toUnitIndex: Int = 0
    private var unitFactor: Int = 10
    private var userRawInput: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setter
        val setMetricSelected: (String) -> Unit = {
                value ->
            metricSelected = value
        }

        val setFromUnitIndex: (Int) -> Unit = {
                value -> fromUnitIndex = value
        }

        val setToUnitIndex: (Int) -> Unit = {
                value -> toUnitIndex = value
        }

        val setUnitFactor: (Int) -> Unit = {
                value -> unitFactor = value
        }

        val setUserRawInput: (String) -> Unit = {
                value -> userRawInput = value
        }

        val setSelectedMetricInfo: (String) -> Unit = {
                value -> selectedmetricInfo = metricInfo[value]
        }

        val resultNum: TextView = findViewById(R.id.result_number)

        fun calculateResult(
            numInput: Double, fromUnitIndex: Int, toUnitIndex: Int,
            unitFactor: Int): Double {
            return ((1.0 * unitFactor).pow(toUnitIndex - fromUnitIndex) * numInput)
        }

        fun updateResult(){
            try {
                val userNumberInput = userRawInput.toDouble()
                val resultNumberCalculation = calculateResult(
                    numInput = userNumberInput,
                    fromUnitIndex=fromUnitIndex,
                    toUnitIndex=toUnitIndex,
                    unitFactor=unitFactor
                )
                resultNum.text = resultNumberCalculation.toString()
            }
            catch (e: NumberFormatException){
                resultNum.text = ""
            }
        }

        // metric dropdown
        val metricSelectedDropDown: AutoCompleteTextView = findViewById(R.id.metric_selection_dropdown)

        // get data from metricInfo
        ArrayAdapter<String>(this, R.layout.metric_selection_item)
            .also {
                    adapter ->
                adapter.addAll(metricInfo.keys)
                metricSelectedDropDown.setAdapter(adapter)
            }
        val toUnitDropdown: AutoCompleteTextView = findViewById(R.id.to_unit_dropdown)

        toUnitDropdown.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                setToUnitIndex(position)
                updateResult()
            }

        // from unit dropdown
        val fromUnitDropdown: AutoCompleteTextView = findViewById(R.id.from_unit_dropdown)

        fromUnitDropdown.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                setFromUnitIndex(position)
                updateResult()
            }

        ArrayAdapter<String>(this, R.layout.metric_selection_item)
            .also {
                    adapter ->
                metricInfo[metricSelected]?.units?.let {
                    adapter.addAll(it)
                }

                // add "to unit" and "from unit" dropdown
                // toUnitDropdown.setAdapter(adapter)
                // fromUnitDropdown.setAdapter(adapter)
                adapter.notifyDataSetChanged()
            }

        // input field
        val inputUserField: EditText = findViewById(R.id.input_number_field)
        inputUserField.isEnabled = false

        // set those variables as some kind of state

        metricSelectedDropDown.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                // Log to check if the callback is being triggered
                Log.i("metricSelected", "onItemSelected: selected metric position: $position")
                Log.i("metricSelected", "onItemSelected: selected metric: ${parent?.getItemAtPosition(position)}")

                // Update metricSelected and selectedmetricInfo
                metricSelected = parent?.getItemAtPosition(position).toString()
                if (metricSelected != "-- Pilih metrik") {
                    // enable when the value is not place holder
                    toUnitDropdown.isEnabled = true
                    fromUnitDropdown.isEnabled = true
                    inputUserField.isEnabled = true

                    // remove the placeholder
                    val adaptorInside = parent?.adapter as ArrayAdapter<String>
                    adaptorInside.remove("-- Pilih metrik")

                    setUnitFactor(metricInfo[metricSelected]?.unitFactor ?: 10)
                    setSelectedMetricInfo(metricSelected)

                    // Log to check the updated values
                    Log.i(
                        "metricSelected",
                        "onItemSelected: updated metricSelected: $metricSelected"
                    )
                    Log.i(
                        "metricSelected",
                        "onItemSelected: updated selectedmetricInfo: $selectedmetricInfo"
                    )

                    // Update the adapters of toUnitDropdown and fromUnitDropdown
                    val adapter = ArrayAdapter<String>(
                        this@MainActivity,
                        R.layout.metric_selection_item
                    )
                    adapter.addAll(selectedmetricInfo?.units ?: emptyList())

                    toUnitDropdown.setAdapter(adapter)
                    fromUnitDropdown.setAdapter(adapter)

                    toUnitDropdown.text = null
                    fromUnitDropdown.text = null
                    inputUserField.text = null
                    resultNum.text = null

                    Log.i("metricSelected", "onItemSelected: adapters updated")
                } else {
                    // disable when the value is placeholder
                    toUnitDropdown.isEnabled = false
                    fromUnitDropdown.isEnabled = false
                    inputUserField.isEnabled = false
                }
            }

        inputUserField.addTextChangedListener(object:
            TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val userInputText = inputUserField.text.toString()
                try {
                    setUserRawInput(userInputText)
                    updateResult()
                }
                catch (e: NumberFormatException){
                    Log.e(
                        "InputUserField",
                        "afterTextChanged: got NumberFormatExcpetion"
                    )
                    val resultNum: TextView = findViewById(R.id.result_number)
                    resultNum.text = ""

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        }
        )
    }
}