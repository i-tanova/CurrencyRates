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
    val changeFirstItem = MutableLiveData<Boolean>()
    var fetchRatesDisposable: Disposable? = null
    var timerDisposable: Disposable? = null

    var quantityChangedDisposable: Disposable? = null
    private val quantityEnteredPublishSubject = PublishSubject.create<String>()

    var isPused = AtomicBoolean(true)
    var isInInputMode = AtomicBoolean(false)

    var selectedBase = Constants.EURO_BASE
    var quantityWanted = 1.0


    val TAG = RatesViewModel::class.java.name

    init {
        configureRateChangedListener()
    }

    fun fetchRatesListFromTimer(base: String) {
        if(!isInInputMode.get() && !isPused.get()) {
            fetchRatesDisposable?.dispose()
            fetchRatesDisposable = RepoRepository.getInstance().getRates(base)
                .map { mapRatesToRateListItems(it, quantityWanted) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ whenRatesFetched(it) }, { Log.e(TAG, it.message, it) })
        }

    }

    private fun whenRatesFetched(newRates: List<RateListItem>) {
        if(isInInputMode.get() || isPused.get()){
            return
        }
        val items = ratesListLive.value
        val newRateItems = mutableListOf<RateListItem>()
        newRateItems.addAll(newRates)

        //Add selection if it is not already added
        if (items.isNullOrEmpty() || items[0].abb != selectedBase) {
            val findDataAboutSelected = CURRENCIES.find { it.abb == selectedBase }
            if (findDataAboutSelected != null) {
                val firstRate = RateListItem(
                    findDataAboutSelected.abb,
                    findDataAboutSelected.description,
                    1.0,
                    quantityWanted,
                    findDataAboutSelected.drawableRId,
                    true
                )
                newRateItems.add(0, firstRate)
            }
        } else {
            newRateItems.add(0, items[0])
        }

        if(!isInInputMode.get() || !isPused.get()){
            ratesListLive.value = newRateItems
            dataLoading.value = false
        }
    }

    private fun startFetchTimer() {
        timerDisposable?.dispose()

        if (!isPused.get() && !isInInputMode.get()) {
            timerDisposable =
                Observable.interval(1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .takeWhile(object : Predicate<Any> {
                        override fun test(o: Any): Boolean {
                            return !isPused.get() && !isInInputMode.get()
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ atTimerTick() })
        }
    }

    private fun atTimerTick() {
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
                    if (rateDouble > 0) {
                        val quantity = BigDecimal(rateDouble).multiply(BigDecimal(quantityWanted))
                        val quantityFormatted = quantity.setScale(4, RoundingMode.HALF_UP)
                        val item = RateListItem(
                            it.abb,
                            it.description,
                            rateDouble,
                            quantityFormatted.toDouble(),
                            it.drawableRId
                        )

                        ratesListItems.add(item)
                    }
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
        setInputMode(true)

        selectedBase = selectedRate.abb
        quantityWanted = selectedRate.quantity

        val newItems = mutableListOf<RateListItem>()
        val items = ratesListLive.value
        if (!items.isNullOrEmpty()) {
            newItems.addAll(items)
            newItems.remove(selectedRate)
        }

        newItems.add(
            0,
            RateListItem(
                selectedRate.abb,
                selectedRate.description,
                selectedRate.rate,
                selectedRate.quantity,
                selectedRate.drawableRId,
                true
            )
        )


        ratesListLive.value = newItems
        changeFirstItem.value = true

        setInputMode(false)
        startFetchTimer()
    }

    fun setInputMode(value: Boolean) {
        isInInputMode.set(value)
    }

    fun onSelectedFieldInputStateChanged(text: String) {
        quantityEnteredPublishSubject.onNext(text.trim())
    }

    private fun configureRateChangedListener() {
        quantityChangedDisposable = quantityEnteredPublishSubject
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ value ->
                onNewValueEntered(value)
            }, { t: Throwable? -> Log.e(TAG, t?.message) })
    }

    private fun onNewValueEntered(value: String): String {
        Log.d(TAG, value)
        try {
            val sumToEvaluate = value.toDouble()
            quantityWanted = sumToEvaluate
        } catch (e: Exception) {
            quantityWanted = 0.0
        }

        return ""
    }
}