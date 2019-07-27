package com.tanovai.revolut.view.rates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.tanovai.revolut.BR
import com.tanovai.revolut.model.data.RateListItem
import kotlinx.android.synthetic.main.list_item_rate.view.*


class RatesListAdapter(private val repoListViewModel: RatesViewModel) : RecyclerView.Adapter<RepoListViewHolder>() {

    var repoList: List<RateListItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val dataBinding = com.tanovai.revolut.databinding.ListItemRateBinding.inflate(inflater, parent, false)
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

//class RatesListAdapter(private val ratesViewModel: RatesViewModel) : RecyclerView.Adapter<RepoListViewHolder>() {
//
//    var ratesList: List<RateListItem> = emptyList()
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoListViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val dataBinding = com.tanovai.revolut.databinding.ActivityRatesBinding.inflate(inflater, parent, false)
//        return RepoListViewHolder(dataBinding, ratesViewModel)
//    }
//
//    override fun getItemCount() = ratesList.size
//
//    override fun onBindViewHolder(holder: RepoListViewHolder, position: Int) {
//        holder.setup(ratesList[position])
//    }
//
//    fun updateRatesList(repoList: List<RateListItem>) {
//        this.ratesList = repoList
//        notifyDataSetChanged()
//    }
//}
//
//class RepoListViewHolder constructor(private val dataBinding: ViewDataBinding, private val ratesViewModel: RatesViewModel)
//    : RecyclerView.ViewHolder(dataBinding.root) {
//
//    fun setup(itemData: RateListItem) {
//        dataBinding.setVariable(BR.itemData, itemData)
//        dataBinding.executePendingBindings()
//
////        abb.text = itemData.abb
////        description.text = itemData.name
////        rate.setText(itemData.rate.toString())
//
//        //            view.findViewById<TextView>(R.id.list_item_rate_abb).text = item.abb
////            view.findViewById<TextView>(R.id.list_item_rate_currency_description).text = item.name
////            view.findViewById<TextInputEditText>(R.id.list_item_rate_input_edit).setText(item.rate.toString())
//
////        Picasso.get().load(itemData.owner.avatar_url).into(avatarImage);
////
////        itemView.setOnClickListener() {
////            val bundle = bundleOf("url" to itemData.html_url)
////            itemView.findNavController().navigate(R.id.action_repoListFragment_to_repoDetailFragment, bundle)
////        }
//    }
//}
