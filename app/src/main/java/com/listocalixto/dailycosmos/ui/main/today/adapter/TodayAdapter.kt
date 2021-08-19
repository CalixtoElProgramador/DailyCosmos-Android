package com.listocalixto.dailycosmos.ui.main.today.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.core.BaseViewHolder
import com.listocalixto.dailycosmos.data.model.APOD
import com.listocalixto.dailycosmos.databinding.ItemApodDailyBinding
import com.listocalixto.dailycosmos.core.BaseDiffUtil

@Suppress("CAST_NEVER_SUCCEEDS")
class TodayAdapter(
    private var apodList: List<APOD>,
    private val imageClickListener: OnImageAPODClickListener,
    private val fabClickListener: OnFabClickListener,
    private val buttonClickListener: OnButtonClickListener,
    private val iconClickListener: OnIconClickListener
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    interface OnImageAPODClickListener {
        fun onImageClick(apod: APOD, itemBinding: ItemApodDailyBinding)
    }

    interface OnFabClickListener {
        fun onFabClick(apod: APOD, itemBinding: ItemApodDailyBinding, position: Int, apodList: List<APOD>)
    }

    interface OnButtonClickListener {
        fun onButtonClick(apod: APOD, itemBinding: ItemApodDailyBinding)
    }

    interface OnIconClickListener {
        fun onIconClick(apod: APOD)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val itemBinding =
            ItemApodDailyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewPagerViewHolder(itemBinding, parent.context)

        itemBinding.imgApodPicture.setOnClickListener {
            val position =
                holder.bindingAdapterPosition.takeIf { it != DiffUtil.DiffResult.NO_POSITION }
                    ?: return@setOnClickListener
            imageClickListener.onImageClick(apodList[position], itemBinding)
        }

        itemBinding.fabAddAPODFavorites.setOnClickListener {
            val position =
                holder.bindingAdapterPosition.takeIf { it != DiffUtil.DiffResult.NO_POSITION }
                    ?: return@setOnClickListener
            fabClickListener.onFabClick(apodList[position], itemBinding, position, apodList)
        }

        itemBinding.btnTranslate.setOnClickListener {
            val position =
                holder.bindingAdapterPosition.takeIf { it != DiffUtil.DiffResult.NO_POSITION }
                    ?: return@setOnClickListener
            buttonClickListener.onButtonClick(apodList[position], itemBinding)
        }

        itemBinding.iconCopyLink.setOnClickListener {
            val position =
                holder.bindingAdapterPosition.takeIf { it != DiffUtil.DiffResult.NO_POSITION }
                    ?: return@setOnClickListener
            iconClickListener.onIconClick(apodList[position])
        }

        return holder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is TodayAdapter.ViewPagerViewHolder -> {
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

    private inner class ViewPagerViewHolder(
        val binding: ItemApodDailyBinding,
        val context: Context
    ) :
        BaseViewHolder<APOD>(binding.root) {

        @SuppressLint("SetTextI18n")
        override fun bind(item: APOD) {

            when (item.media_type) {
                "image" -> {
                    binding.textVideoMessage.visibility = View.GONE
                    binding.iconCopyLink.visibility = View.GONE
                    if (item.hdurl.isEmpty()) {
                        Glide.with(context).load(item.url).into(binding.imgApodPicture)
                    } else {
                        Glide.with(context).load(item.hdurl).into(binding.imgApodPicture)
                    }
                }
                "video" -> {
                    binding.imgApodPicture.setImageResource(R.drawable.photo_cover)
                    binding.textVideoMessage.visibility = View.VISIBLE
                    binding.iconCopyLink.visibility = View.VISIBLE
                }
            }

            binding.textApodTitle.text = item.title
            binding.textApodDate.text = item.date

            if (item.explanation.isEmpty()) {
                binding.textApodExplanation.text = context.getString(R.string.no_explanation)
                binding.btnTranslate.visibility = View.GONE
            } else {
                binding.textApodExplanation.text = item.explanation
                binding.btnTranslate.visibility = View.VISIBLE
            }

            if (item.copyright.isEmpty()) {
                binding.textApodCopyright.text = context.getString(R.string.no_copyright)
            } else {
                binding.textApodCopyright.text = "Copyright: ${item.copyright}"
            }
            if (item.is_favorite == 0) {
                binding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite_border)
            } else {
                binding.fabAddAPODFavorites.setImageResource(R.drawable.ic_favorite)
            }
        }
    }
}
