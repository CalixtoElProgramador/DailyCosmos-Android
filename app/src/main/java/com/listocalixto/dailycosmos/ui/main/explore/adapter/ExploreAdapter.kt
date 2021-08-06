package com.listocalixto.dailycosmos.ui.main.explorer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
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

    private val firebaseAuth by lazy {FirebaseAuth.getInstance()}

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
        override fun bind(item: APOD) {

            if (item.hdurl.isEmpty()) {
                Glide.with(context).load(item.url).into(binding.imageItemAPOD)
            } else {
                Glide.with(context).load(item.hdurl).into(binding.imageItemAPOD)
            }

            binding.favoritesItemAPOD.setOnClickListener{
                if (firebaseAuth.currentUser?.isAnonymous == true) {
                    Toast.makeText(context, "Función solo para registrados", Toast.LENGTH_SHORT).show()
                }
            }

            binding.apply {
                titleItemAPOD.text = item.title
                dateItemAPOD.text = item.date
            }
        }
    }

}
