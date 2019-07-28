package com.tanovai.currencyconverter.view.rates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.tanovai.currencyconverter.BR
import com.tanovai.currencyconverter.model.data.RateListItem


class RatesListAdapter(private val repoListViewModel: RatesViewModel) : RecyclerView.Adapter<RepoListViewHolder>() {

    var repoList: List<RateListItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val dataBinding = com.tanovai.currencyconverter.databinding.ListItemRateBinding.inflate(inflater, parent, false)
        return RepoListViewHolder(dataBinding, repoListViewModel)
    }

    override fun getItemCount() = repoList.size

    override fun onBindViewHolder(holder: RepoListViewHolder, position: Int) {
        holder.setup(repoList[position])
    }

    fun updateRatesList(repoList: List<RateListItem>) {
        this.repoList = repoList
        notifyDataSetChanged()
    }
}

class RepoListViewHolder constructor(private val dataBinding: ViewDataBinding, private val repoListViewModel: RatesViewModel)
    : RecyclerView.ViewHolder(dataBinding.root) {

    fun setup(itemData: RateListItem) {
        dataBinding.setVariable(BR.itemData, itemData)
        dataBinding.executePendingBindings()

        itemView.setOnClickListener() {
            repoListViewModel.onItemClick(itemData)
        }
    }
}