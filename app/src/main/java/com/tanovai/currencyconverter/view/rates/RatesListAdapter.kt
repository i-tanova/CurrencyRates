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

    //val avatarImage = itemView.item_avatar

    fun setup(itemData: RateListItem) {
        dataBinding.setVariable(BR.itemData, itemData)
        dataBinding.executePendingBindings()

//        Picasso.get().load(itemData.owner.avatar_url).into(avatarImage);
//
//        itemView.setOnClickListener() {
//            val bundle = bundleOf("url" to itemData.html_url)
//            itemView.findNavController().navigate(R.id.action_repoListFragment_to_repoDetailFragment, bundle)
//        }
    }
}