package com.example.metricconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
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

        fun calculateResult(
            numInput: Double, fromUnitIndex: Int, toUnitIndex: Int,
            unitFactor: Int): Double {
            return ((1.0 * unitFactor).pow(toUnitIndex - fromUnitIndex) * numInput)
        }

        fun updateResult(){
            val resultNum: TextView = findViewById(R.id.result_number)
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
        val metricSelectedDropDown: Spinner = findViewById(R.id.metric_dropdown)

        // get data from metricInfo
        ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
            .also {
                    adapter ->
                adapter.add("-- Pilih metrik")
                adapter.addAll(metricInfo.keys)
                metricSelectedDropDown.adapter = adapter
            }
        val toUnitDropdown: Spinner = findViewById(R.id.to_unit_dropdown)

        toUnitDropdown.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                setToUnitIndex(position)
                updateResult()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // from unit dropdown
        val fromUnitDropdown: Spinner = findViewById(R.id.from_unit_dropdown)

        fromUnitDropdown.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                setFromUnitIndex(position)
                updateResult()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
            .also {
                    adapter ->
                adapter.add("-- Pilih satuan")
                metricInfo[metricSelected]?.units?.let {
                    adapter.addAll(it)
                }

                // add "to unit" and "from unit" dropdown
                toUnitDropdown.adapter = adapter
                fromUnitDropdown.adapter = adapter
                adapter.notifyDataSetChanged()
            }

        // input field
        val inputUserField: EditText = findViewById(R.id.input_number_field)

        // set those variables as some kind of state

        metricSelectedDropDown.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
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
                        android.R.layout.simple_spinner_item
                    )
                    adapter.addAll(selectedmetricInfo?.units ?: emptyList())
                    toUnitDropdown.adapter = adapter
                    fromUnitDropdown.adapter = adapter

                    Log.i("metricSelected", "onItemSelected: adapters updated")
                }
                else {
                    // disable when the value is placeholder
                    toUnitDropdown.isEnabled = false
                    fromUnitDropdown.isEnabled = false
                    inputUserField.isEnabled = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
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