package com.rsschool.cats.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.api.load
import com.rsschool.cats.utils.CatsListAdapter.CatViewHolder
import com.rsschool.cats.data.Cat
import com.rsschool.cats.databinding.CatItemBinding
import com.rsschool.cats.databinding.CatItemErrorBinding
import com.rsschool.cats.databinding.CatItemProgressBinding


class CatsListAdapter(private val listener: CatListener)
    : ListAdapter<Cat, CatViewHolder>(CATS_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {

        var viewHolder:CatViewHolder? = null
        val layoutInflater = LayoutInflater.from(parent.context)
        when (viewType){
            VIEW_TYPE_LOADING -> {
                val binding = CatItemProgressBinding.inflate(layoutInflater, parent, false)
                viewHolder = CatLoadingViewHolder(binding)
            }
            VIEW_TYPE_ERROR -> {
                val binding = CatItemErrorBinding.inflate(layoutInflater, parent, false)
                viewHolder = CatErrorViewHolder(binding)
            }
            VIEW_TYPE_ITEM -> {
                val binding = CatItemBinding.inflate(layoutInflater, parent, false)
                viewHolder = CatItemViewHolder(binding, listener)
            }
        }

        return viewHolder!!
    }

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            currentList[position].number < 0 -> VIEW_TYPE_LOADING
            currentList[position].number == 0 -> VIEW_TYPE_ERROR
            else -> VIEW_TYPE_ITEM
        }
    }

    class CatItemViewHolder(private val binding: CatItemBinding,
                           private val listener: CatListener
    ) : CatViewHolder(binding) {

        override fun bind(cat: Cat?) {
            if (cat != null) {

                val number = cat.number.toString()
                binding.catTextView.text = String.format("Cat #$number")
                binding.catImageView.load(cat.imageUrl)

                /*
                //For sharedElement transitions
                binding.catTextView.transitionName = cat.number.toString();
                binding.catImageView.transitionName = cat.imageUrl;
                 */

                binding.root.setOnClickListener {
                    listener.openCat(cat, binding.catTextView, binding.catImageView)
                }
            }

        }
    }

    class CatLoadingViewHolder(private val binding: CatItemProgressBinding
    ) : CatViewHolder(binding) {

        override fun bind(cat: Cat?) {
            binding.progressbar.visibility = View.VISIBLE
        }
    }

    class CatErrorViewHolder(
        binding: CatItemErrorBinding
    ) : CatViewHolder(binding) {

        override fun bind(cat: Cat?) {}
    }

    abstract class CatViewHolder(binding: ViewBinding)
        :RecyclerView.ViewHolder(binding.root) {

        abstract fun bind(cat: Cat?)
    }

    companion object {
        private const val VIEW_TYPE_ERROR = 2
        private const val VIEW_TYPE_LOADING = 1
        private const val VIEW_TYPE_ITEM = 0

        private val CATS_COMPARATOR = object : DiffUtil.ItemCallback<Cat>() {

            override fun areItemsTheSame(oldItem: Cat, newItem: Cat): Boolean {
                return oldItem.number == newItem.number
            }

            override fun areContentsTheSame(oldItem: Cat, newItem: Cat): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.imageUrl == newItem.imageUrl
            }
        }
    }
}
