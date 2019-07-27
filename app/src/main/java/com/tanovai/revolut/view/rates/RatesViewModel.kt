package com.tanovai.revolut.view.rates

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.tanovai.mvvm.model.RepoRepository
import com.tanovai.revolut.architecture.BaseViewModel
import com.tanovai.revolut.model.Rate
import com.tanovai.revolut.model.RatesResponse
import com.tanovai.revolut.model.data.RateListItem
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class RatesViewModel : BaseViewModel() {

    val ratesListLive = MutableLiveData<List<RateListItem>>()
    val compositeDisposable = CompositeDisposable()
    var timerDisposable: Disposable? = null
    var isPused = true

    val baseEUR = "EUR"
    val TAG = RatesViewModel::class.java.name

    fun fetchRepoList() {
        compositeDisposable.add(RepoRepository.getInstance().getRates(baseEUR)
            .map { mapRatesToRateListItems(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ whenRatesFetched(it) } , { Log.e(TAG, it.message, it)}))

    }

    private fun whenRatesFetched(it: List<RateListItem>?) {
        ratesListLive.value = it
        dataLoading.value = false

        startFetchTimer()
    }

    private fun startFetchTimer() {
        timerDisposable?.dispose()
        if (!isPused) {
            timerDisposable = Observable.just(true).delay(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({whenTimerFinised()})
        }
    }

    private fun whenTimerFinised() {
       if(!isPused){
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
        compositeDisposable.clear()
        timerDisposable?.dispose()
        super.onCleared()
    }

    fun onCreate() {
        dataLoading.value = true
    }

    fun onResume() {
        isPused = false
        fetchRepoList()
    }

    fun onPause() {
        isPused = true
        timerDisposable?.dispose()
    }
}