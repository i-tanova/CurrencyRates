package com.tanovai.currencyconverter.view.rates

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.tanovai.mvvm.model.RepoRepository
import com.tanovai.currencyconverter.architecture.BaseViewModel
import com.tanovai.currencyconverter.model.Rate
import com.tanovai.currencyconverter.model.RatesResponse
import com.tanovai.currencyconverter.model.data.RateListItem
import com.tanovai.currencyconverter.util.Constants
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class RatesViewModel : BaseViewModel() {

    val ratesListLive = MutableLiveData<List<RateListItem>>()
    var fetchRatesDisposable: Disposable? = null
    var timerDisposable: Disposable? = null
    var isPused = AtomicBoolean(true)

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
                            return !isPused.get()
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({whenTimerFinised()})
        }
    }

    private fun whenTimerFinised() {
       if(!isPused.get()){
           fetchRepoList()
       }
    }

    private fun mapRatesToRateListItems(it: RatesResponse): List<RateListItem> {
        val rates = mutableListOf<RateListItem>()

              rates.add(createRateListItem(it.rates))

        return rates
    }

    private fun createRateListItem(rate: Rate): RateListItem {
        return RateListItem("USD", "US Dollar", rate.USD)
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
        startFetchTimer()
    }

    fun onPause() {
        isPused.set(true)
        fetchRatesDisposable?.dispose()
        timerDisposable?.dispose()
    }
}