package com.tanovai.revolut.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class ItemAdapter<T> : RecyclerView.Adapter<ItemAdapter<T>.ViewHolder>() {
    
        private var itemsList = mutableListOf<T>()
        private val diffCallback = ItemsDifCallback()
        @Suppress("UNCHECKED_CAST")
        protected val onClickListener: View.OnClickListener = View.OnClickListener { v -> onItemClickedInternal(v, item = v?.tag as T) }
    
        protected var isClickable: Boolean = true
    
        abstract fun getItemLayoutId(): Int
    
        abstract fun initItemView(view: View, item: T)
    
        //this method is returning the view for each item in the list
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter<T>.ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(getItemLayoutId(), parent, false)
            return ViewHolder(v)
        }
    
        //this method is binding the data on the list
        override fun onBindViewHolder(holder: ItemAdapter<T>.ViewHolder, position: Int) {
            holder.bindItems(itemsList[position])
        }
    
        fun setIsClickable(isClickable: Boolean) {
            this.isClickable = isClickable
        }
    
        //this method is giving the size of the list
        override fun getItemCount(): Int {
            return itemsList.size
        }
    
        abstract fun onItemClicked(item: T)
    
    
        fun onItemClickedInternal(view: View, item: T){
            view.isActivated = true
            onItemClicked(item)
        }
    
        //the class is hodling the list view
        @Suppress("UNCHECKED_CAST")
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    
            fun bindItems(item: T) {
                initItemView(itemView, item)
                itemView.tag = item
                if (isClickable) {
                    itemView.setOnClickListener(onClickListener)
                }
            }
        }
    
    
        fun setItems(itemsList: List<T>) {
            this.itemsList.clear()
            this.itemsList.addAll(itemsList)
            notifyDataSetChanged()
        }
    
        fun swapItems(newItems: List<T>) {
            // compute diffs
            diffCallback.oldItems = itemsList
            diffCallback.newItems = newItems
    
            val diffResult = DiffUtil.calculateDiff(diffCallback)
    
            // clear contacts and add
            itemsList.clear()
            itemsList.addAll(newItems)
    
            diffResult.dispatchUpdatesTo(this) // calls adapter's notify methods after diff is computed
        }
    
        inner class ItemsDifCallback : DiffUtil.Callback() {
    
            var oldItems: List<T> = listOf()
            var newItems: List<T> = listOf()
    
            override fun getOldListSize(): Int {
                return oldItems.size
            }
    
            override fun getNewListSize(): Int {
                return newItems.size
            }
    
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                // add a unique ID property on Item and expose a getId() method
                return areItemsTheSameById(oldItems[oldItemPosition], newItems[newItemPosition])
            }
    
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldItems[oldItemPosition]
                val newItem = newItems[newItemPosition]
    
                return areItemsContentSame(oldItem, newItem)
            }
        }
    
        abstract fun areItemsContentSame(oldItem: T, newItem: T): Boolean
    
        abstract fun areItemsTheSameById(oldItem: T, newItem: T): Boolean
    
    
    }