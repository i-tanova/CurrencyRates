package com.tanovai.currencyconverter.view.rates

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.tanovai.currencyconverter.R
import com.tanovai.currencyconverter.databinding.ActivityRatesBinding
import kotlinx.android.synthetic.main.activity_rates.*

class RatesActivity : AppCompatActivity() {

    private lateinit var adapter: RatesListAdapter
    private lateinit var binding: ActivityRatesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProviders.of(this).get(RatesViewModel::class.java)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_rates)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        setupAdapter(binding.viewmodel)
        setupObservers(binding.viewmodel, binding.lifecycleOwner)
        val itemAnimator = rates_rv.getItemAnimator()
        itemAnimator?.changeDuration = 0

        binding.viewmodel?.onCreate()
    }

    override fun onResume() {
        super.onResume()
        binding.viewmodel?.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.viewmodel?.onPause()
    }

    private fun setupAdapter(viewModel: RatesViewModel?) {
        if (viewModel != null) {
            adapter = RatesListAdapter(viewModel)
            rates_rv.layoutManager = LinearLayoutManager(this)
            rates_rv.adapter = adapter
        }
    }


    private fun setupObservers(viewmodel: RatesViewModel?, lifecycleOwner: LifecycleOwner?) {
        if (lifecycleOwner != null) {
            viewmodel?.ratesListLive?.observe(lifecycleOwner, Observer {
                adapter.updateRatesListDontChangeFirst(it)
            })

            viewmodel?.changeFirstItem?.observe(lifecycleOwner, Observer {
                val listItems = viewmodel.ratesListLive.value
                if (listItems != null) {
                    adapter.updateRatesListAll(listItems)
                    rates_rv.scrollToPosition(0)
                }
            })
        }
    }
}
