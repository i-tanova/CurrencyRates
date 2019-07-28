package com.tanovai.currencyconverter.view.rates

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.tanovai.mvvm.model.RepoRepository
import com.tanovai.currencyconverter.architecture.BaseViewModel
import com.tanovai.currencyconverter.model.Rate
import com.tanovai.currencyconverter.model.RatesResponse
import com.tanovai.currencyconverter.model.data.RateListItem
import com.tanovai.currencyconverter.util.CURRENCIES
import com.tanovai.currencyconverter.util.Constants
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class RatesViewModel : BaseViewModel() {

    val ratesListLive = MutableLiveData<List<RateListItem>>()
    var fetchRatesDisposable: Disposable? = null
    var timerDisposable: Disposable? = null
    var isPused = AtomicBoolean(true)
    var isInInputMode = AtomicBoolean(false)


    val TAG = RatesViewModel::class.java.name

    fun fetchRepoList() {
        fetchRatesDisposable?.dispose()
        fetchRatesDisposable = RepoRepository.getInstance().getRates(Constants.baseEUR)
            .map { mapRatesToRateListItems(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ whenRatesFetched(it) } , { Log.e(TAG, it.message, it)})

    }

    private fun whenRatesFetched(it: List<RateListItem>?) {
        ratesListLive.value = it
        dataLoading.value = false
    }

    private fun startFetchTimer() {
        timerDisposable?.dispose()
        if (!isPused.get()) {
            timerDisposable =
                Observable.interval(1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .takeWhile(object : Predicate<Any>{
                        override fun test(o: Any): Boolean {
                            return !isPused.get() && !isInInputMode.get()
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({whenTimerFinised()})
        }
    }

    private fun whenTimerFinised() {
       if(!isPused.get() && !isInInputMode.get()){
           fetchRepoList()
       }
    }

    private fun mapRatesToRateListItems(response: RatesResponse): List<RateListItem> {
        val ratesListItems = mutableListOf<RateListItem>()

        CURRENCIES.forEach {
            val rateStr = getPropertyValue(response.rates, it.abb)

            try {
                if (rateStr.isNotEmpty()) {
                val rateDouble = rateStr.toDouble()
                    ratesListItems.add(RateListItem(it.abb, it.description, rateDouble, it.drawableRId))
                }
            }catch (e: Exception){
                Log.e(TAG, e.message, e)
            }
        }
        return ratesListItems
    }

    fun getPropertyValue(obj: Any, propertyName: String): String
    {
        var result = ""
        try {
            val field = obj.javaClass.getDeclaredField(propertyName)
            field.isAccessible = true
            result = field.get(obj)?.toString() ?:""
        }finally{
            return result
        }
    }

    override fun onCleared() {
        fetchRatesDisposable?.dispose()
        timerDisposable?.dispose()
        super.onCleared()
    }

    fun onCreate() {
        dataLoading.value = true
    }

    fun onResume() {
        isPused.set(false)
        if(!isInInputMode.get()) {
            startFetchTimer()
        }
    }

    fun onPause() {
        isPused.set(true)
        fetchRatesDisposable?.dispose()
        timerDisposable?.dispose()
    }

    fun onItemClick(itemData: RateListItem) {
        isInInputMode.set(true)
       val items = ratesListLive.value
        val newItems = mutableListOf<RateListItem>()
        if(items != null){
            newItems.addAll(items)
            newItems.remove(itemData)
            newItems.add(0, itemData)
        }
        ratesListLive.value = newItems
    }
}