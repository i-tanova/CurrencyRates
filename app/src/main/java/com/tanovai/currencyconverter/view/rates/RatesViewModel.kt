package com.tanovai.currencyconverter.view.rates

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.tanovai.currencyconverter.architecture.BaseViewModel
import com.tanovai.currencyconverter.model.RatesResponse
import com.tanovai.currencyconverter.model.data.RateListItem
import com.tanovai.currencyconverter.util.CURRENCIES
import com.tanovai.currencyconverter.util.Constants
import com.tanovai.mvvm.model.RepoRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class RatesViewModel : BaseViewModel() {

    val ratesListLive = MutableLiveData<List<RateListItem>>()
    var fetchRatesDisposable: Disposable? = null
    var fetchRatesDisposableOnItemClick: Disposable? = null
    var timerDisposable: Disposable? = null
    var rateChangedDisposable: Disposable? = null
    var isPused = AtomicBoolean(true)
    var isInInputMode = AtomicBoolean(false)
    var selectedBase = Constants.EURO_BASE
    var quantityWanted = 1.0

    private val rateEnteredPublishSubject = PublishSubject.create<String>()

    init {
        configureRateChangedListener()
    }


    val TAG = RatesViewModel::class.java.name

    fun fetchRatesListFromTimer(base: String) {
        fetchRatesDisposable?.dispose()
        fetchRatesDisposable = RepoRepository.getInstance().getRates(base)
            .map { mapRatesToRateListItems(it, quantityWanted) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ whenRatesFetched(it) }, { Log.e(TAG, it.message, it) })

    }

    private fun whenRatesFetched(newRates: List<RateListItem>) {
        val items = ratesListLive.value
        val newRateItems = mutableListOf<RateListItem>()
        newRateItems.addAll(newRates)

        if(items.isNullOrEmpty() || items[0].abb != selectedBase){
            val findDataAboutSelected = CURRENCIES.find { it.abb == selectedBase }
            if(findDataAboutSelected != null) {
                val firstRate = RateListItem(findDataAboutSelected.abb, findDataAboutSelected.description, 1.0, quantityWanted, findDataAboutSelected.drawableRId, true)
                 newRateItems.add(0, firstRate)
            }
        }else{
            newRateItems.add(0, items[0])
        }

        ratesListLive.value = newRateItems
        dataLoading.value = false
    }

    private fun startFetchTimer() {
        timerDisposable?.dispose()
        if (!isPused.get()) {
            timerDisposable =
                Observable.interval(1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .takeWhile(object : Predicate<Any> {
                        override fun test(o: Any): Boolean {
                            return !isPused.get() && !isInInputMode.get()
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ atIntervalTick() })
        }
    }

    private fun atIntervalTick() {
        if (!isPused.get() && !isInInputMode.get()) {
            fetchRatesListFromTimer(selectedBase)
        }
    }

    private fun mapRatesToRateListItems(response: RatesResponse, quantityWanted: Double): List<RateListItem> {
        val ratesListItems = mutableListOf<RateListItem>()

        CURRENCIES.forEach {
            val rateStr = getPropertyValue(response.rates, it.abb)

            try {
                if (rateStr.isNotEmpty()) {
                    val rateDouble = rateStr.toDouble()
                    val quantity = BigDecimal(rateDouble).multiply(BigDecimal(quantityWanted))
                    val quantityFormatted = quantity.setScale(4, RoundingMode.HALF_UP)
                    ratesListItems.add(RateListItem(it.abb, it.description, rateDouble, quantityFormatted.toDouble(), it.drawableRId))
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }
        }
        return ratesListItems
    }


    fun getPropertyValue(obj: Any, propertyName: String): String {
        var result = ""
        try {
            val field = obj.javaClass.getDeclaredField(propertyName)
            field.isAccessible = true
            result = field.get(obj)?.toString() ?: ""
        } finally {
            return result
        }
    }

    override fun onCleared() {
        fetchRatesDisposable?.dispose()
        fetchRatesDisposableOnItemClick?.dispose()
        timerDisposable?.dispose()
        super.onCleared()
    }

    fun onCreate() {
        dataLoading.value = true
    }

    fun onResume() {
        isPused.set(false)
        if (!isInInputMode.get()) {
            startFetchTimer()
        }
    }

    fun onPause() {
        isPused.set(true)
        fetchRatesDisposable?.dispose()
        timerDisposable?.dispose()
    }

    fun onItemClick(selectedRate: RateListItem) {
        isInInputMode.set(true)
        fetchRatesDisposableOnItemClick?.dispose()
        fetchRatesDisposableOnItemClick = RepoRepository.getInstance().getRates(selectedBase)
            .map { mapRatesToRateListItems(it, selectedRate.rate) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ whenRatesFetchedChangeBase(it, selectedRate) }, { Log.e(TAG, it.message, it) })
    }

    private fun whenRatesFetchedChangeBase(
        ratesListItems: List<RateListItem>?,
        selectedRate: RateListItem
    ) {

        if(ratesListItems != null) {

            selectedBase = selectedRate.abb
            quantityWanted = selectedRate.rate
            val newItems = mutableListOf<RateListItem>()
            newItems.add(
                RateListItem(
                    selectedRate.abb,
                    selectedRate.description,
                    selectedRate.rate,
                    selectedRate.quantityRate,
                    selectedRate.drawableRId,
                    true
                )
            )

            newItems.addAll(1, ratesListItems)
            ratesListLive.value = newItems
        }
    }


    fun onSelectedFieldInputStateChanged(text: String) {
        rateEnteredPublishSubject.onNext(text.trim())
    }

    private fun configureRateChangedListener() {
        rateChangedDisposable = rateEnteredPublishSubject
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ value ->
                onNewValueEntered(value)
            }, { t: Throwable? -> Log.e(TAG, "Failed to get search results") })
    }


    private fun onNewValueEntered(value: String): String {
        Log.d(TAG, value)
        try {
            val sumToEvaluate = value.toDouble()
            //quantityWanted = sumToEvaluate
//            val rates = ratesListLive.value
//            val newRates = mutableListOf<RateListItem>()
//            if (rates != null) {
//                newRates.addAll(rates.subList(1, rates.lastIndex).map {
//                    RateListItem(
//                        it.abb,
//                        it.description,
//                        it.rate,
//                        it.drawableRId
//                    )
//                })
//            }
//
//            ratesListLive.value = newRates
        } catch (e: Exception) {

        }

        return ""
    }
}