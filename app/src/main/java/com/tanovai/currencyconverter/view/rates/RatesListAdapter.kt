package com.tanovai.currencyconverter.view.rates

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.tanovai.currencyconverter.BR
import com.tanovai.currencyconverter.R
import com.tanovai.currencyconverter.model.data.RateListItem
import kotlinx.android.synthetic.main.list_item_rate.view.*


class RatesListAdapter(private val repoListViewModel: RatesViewModel) : RecyclerView.Adapter<RepoListViewHolder>() {

    var repoList: List<RateListItem> = emptyList()
    val textWatcher = object : TextWatcher {

        override fun afterTextChanged(editable: Editable) {
            repoListViewModel.onSelectedFieldInputStateChanged(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val dataBinding = com.tanovai.currencyconverter.databinding.ListItemRateBinding.inflate(inflater, parent, false)
        return RepoListViewHolder(dataBinding, repoListViewModel, textWatcher)
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

class RepoListViewHolder constructor(private val dataBinding: ViewDataBinding, private val repoListViewModel: RatesViewModel, private val textWatcher: TextWatcher)
    : RecyclerView.ViewHolder(dataBinding.root) {

    val editText = itemView.findViewById<TextInputEditText>(R.id.list_item_rate_input_edit)

    fun setup(itemData: RateListItem) {
        dataBinding.setVariable(BR.itemData, itemData)
        dataBinding.executePendingBindings()

        if(itemData.isSelected) {
            editText.addTextChangedListener(textWatcher)
            editText.isEnabled = true
        }else {
            editText.removeTextChangedListener(textWatcher)
            editText.isEnabled = false
        }

        itemView.setOnClickListener() {
            repoListViewModel.onItemClick(itemData)
        }
    }
}