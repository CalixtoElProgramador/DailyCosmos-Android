package com.listocalixto.dailycosmos.ui.main.explore.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.core.BaseDiffUtil
import com.listocalixto.dailycosmos.core.BaseViewHolder
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.databinding.ItemApodBinding

class ExploreAdapter(
    private var apodList: List<APOD>,
    private val itemClickListener: OnAPODClickListener
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    interface OnAPODClickListener {
        fun onAPODClick(apod: APOD, binding: ItemApodBinding)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val itemBinding =
            ItemApodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = APODViewHolder(itemBinding, parent.context)

        itemBinding.root.setOnClickListener {
            val position =
                holder.bindingAdapterPosition.takeIf { it != DiffUtil.DiffResult.NO_POSITION }
                    ?: return@setOnClickListener
            itemClickListener.onAPODClick(apodList[position], itemBinding)
        }
        return holder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is APODViewHolder -> {
                holder.bind(apodList[position])
            }
        }
    }

    override fun getItemCount(): Int = apodList.size

    fun setData(newAPODList: List<APOD>) {
        val diffUtil = BaseDiffUtil(apodList, newAPODList)
        val diffResults = DiffUtil.calculateDiff(diffUtil)
        apodList = newAPODList
        diffResults.dispatchUpdatesTo(this)
    }

    private inner class APODViewHolder(val binding: ItemApodBinding, val context: Context) :
        BaseViewHolder<APOD>(binding.root) {
        @SuppressLint("SetTextI18n", "ResourceAsColor")
        override fun bind(item: APOD) {

            if (item.hdurl.isEmpty()) {
                Glide.with(context).load(item.url).into(binding.imageItemAPOD)
            } else {
                Glide.with(context).load(item.hdurl).into(binding.imageItemAPOD)
            }

            binding.apply {
                titleItemAPOD.text = item.title
                dateItemAPOD.text = item.date
            }
            if (item.copyright.isEmpty()) {
                binding.textCopyright.text = "No copyright"
            } else {
                binding.textCopyright.text = item.copyright
            }

        }
    }

}
