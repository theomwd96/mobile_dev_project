package com.studenthousing.app.ui.properties

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.studenthousing.app.BuildConfig
import com.studenthousing.app.R
import com.studenthousing.app.data.local.PropertyEntity
import com.studenthousing.app.databinding.ItemPropertyBinding
import com.studenthousing.app.util.LocationHelper

class PropertyAdapter(
    private val onClick: (PropertyEntity) -> Unit
) : ListAdapter<PropertyEntity, PropertyAdapter.PropertyViewHolder>(PropertyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PropertyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class PropertyViewHolder(private val binding: ItemPropertyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PropertyEntity, onClick: (PropertyEntity) -> Unit) {
            binding.titleText.text = item.title
            binding.addressText.text = item.address
            binding.priceText.text = "$${item.price}/mo"

            if (!item.imageUrl.isNullOrBlank()) {
                binding.propertyImage.load(item.imageUrl) {
                    placeholder(R.drawable.placeholder_property)
                    error(R.drawable.placeholder_property)
                    crossfade(true)
                }
            } else {
                binding.propertyImage.setImageResource(R.drawable.placeholder_property)
            }

            if (!item.type.isNullOrBlank()) {
                binding.typeChip.text = item.type
                binding.typeChip.visibility = View.VISIBLE
            } else {
                binding.typeChip.visibility = View.GONE
            }

            if (item.latitude != null && item.longitude != null) {
                val km = LocationHelper.distanceKm(
                    item.latitude,
                    item.longitude,
                    BuildConfig.CAMPUS_LAT,
                    BuildConfig.CAMPUS_LNG
                )
                binding.distanceText.text = String.format("%.1f km from campus", km)
            } else {
                binding.distanceText.text = binding.root.context.getString(R.string.distance_unavailable)
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    private class PropertyDiffCallback : DiffUtil.ItemCallback<PropertyEntity>() {
        override fun areItemsTheSame(oldItem: PropertyEntity, newItem: PropertyEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PropertyEntity, newItem: PropertyEntity): Boolean {
            return oldItem == newItem
        }
    }
}
