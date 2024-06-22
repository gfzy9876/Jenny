package pers.zy.jenny

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pers.zy.jenny.databinding.ItemLogBinding

class LogAdapter(private val logs: List<String>) : RecyclerView.Adapter<LogAdapter.Holder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
    return Holder(ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false))
  }

  override fun getItemCount(): Int = logs.size

  override fun onBindViewHolder(holder: Holder, position: Int) {
    holder.binding.tvLog.text = logs[position]
  }

  class Holder(val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root)
}