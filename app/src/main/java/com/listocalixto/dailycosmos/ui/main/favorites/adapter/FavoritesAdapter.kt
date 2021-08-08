package com.listocalixto.dailycosmos.ui.main.favorites.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.listocalixto.dailycosmos.core.BaseViewHolder
import com.listocalixto.dailycosmos.data.model.APODFavoriteEntity
import com.listocalixto.dailycosmos.databinding.ItemApodFavoriteBinding

class FavoritesAdapter(
    private val favoriteList: List<APODFavoriteEntity>,
    private val itemClickListener: OnFavoriteClickListener
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    interface OnFavoriteClickListener {
        fun onFavoriteClick(favorite: APODFavoriteEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val itemBinding =
            ItemApodFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = FavoriteViewHolder(itemBinding, parent.context)
        itemBinding.root.setOnClickListener {
            val position =
                holder.bindingAdapterPosition.takeIf { it != DiffUtil.DiffResult.NO_POSITION }
                    ?: return@setOnClickListener
            itemClickListener.onFavoriteClick(favoriteList[position])
        }
        return holder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is FavoriteViewHolder -> {
                holder.bind(favoriteList[position])
            }
        }
    }

    override fun getItemCount(): Int = favoriteList.size

    private inner class FavoriteViewHolder(
        val binding: ItemApodFavoriteBinding,
        val context: Context
    ) :
        BaseViewHolder<APODFavoriteEntity>(binding.root) {
        override fun bind(item: APODFavoriteEntity) {
            binding.titleApodFavorite.text = item.title
            binding.dateApodFavorite.text = item.date
            if (item.hdurl.isEmpty()) {
                Glide.with(context).load(item.url).into(binding.imgApodFavorite)
            } else {
                Glide.with(context).load(item.hdurl).into(binding.imgApodFavorite)
            }
        }
    }

}