package com.charm.phr.mphr.ui.cardonfile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemCardOnFileBinding
import com.charm.phr.mphr.model.PaymentGatewayToken

class CardOnFileListAdapter : RecyclerView.Adapter<CardOnFileListAdapter.ViewHolder>() {

    private val cardList: ArrayList<PaymentGatewayToken> = arrayListOf()
    private var listener: PracticeCardOnFileListener? = null

    interface PracticeCardOnFileListener {
        fun onDeleteCard(paymentGatewayToken: PaymentGatewayToken, position: Int)
        fun onEditCard(paymentGatewayToken: PaymentGatewayToken, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardOnFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paymentGatewayToken = cardList[position]
        holder.onBind(paymentGatewayToken)

        holder.binding.cofEditBtn.setOnClickListener {
            listener?.onEditCard(paymentGatewayToken, position)
        }

        holder.binding.cofDeleteBtn.setOnClickListener {
            listener?.onDeleteCard(paymentGatewayToken, position)
        }

    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    fun setDataList(cardList: ArrayList<PaymentGatewayToken>) {
        this.cardList.clear()
        this.cardList.addAll(cardList)
        notifyDataSetChanged()
    }

    fun removeCard(position: Int): Int {
        cardList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, cardList.size)

        return cardList.size
    }

    fun updateCard(position: Int, paymentGatewayToken: PaymentGatewayToken): Int {
        cardList[position] = paymentGatewayToken
        notifyDataSetChanged()
        return cardList.size
    }

    fun setPracticeCardOnFileListener(listener: PracticeCardOnFileListener) {
        this.listener = listener
    }

    class ViewHolder(val binding: ItemCardOnFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(paymentGatewayToken: PaymentGatewayToken) {
            binding.cofCardTypeTv.text = paymentGatewayToken.cardBrand
            binding.cofCardNumberTv.text = binding.root.resources.getString(
                R.string.card_display_format,
                paymentGatewayToken.cardDigit
            )
            if (paymentGatewayToken.cardNickName.isNullOrBlank()) {
                binding.cofCardNickNameValueTv.text = paymentGatewayToken.nameOnCard
                binding.cofCardNickNameLableTv.text = binding.root.resources.getString(R.string.payment_card_name_hint)
            } else {
                binding.cofCardNickNameValueTv.text = paymentGatewayToken.cardNickName
                binding.cofCardNickNameLableTv.text = binding.root.resources.getString(R.string.card_nick_name)
            }
            binding.cofExpiresValueTv.text = paymentGatewayToken.expiry
            if (paymentGatewayToken.hasCardExpired) {
                binding.cofExpiresValueTv.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.allergyListSeveritySevereTxtColor
                    )
                )
            } else {
                binding.cofExpiresValueTv.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.white
                    )
                )
            }
            if (!paymentGatewayToken.allowPracticeCardDeletion && !paymentGatewayToken.isPHRCard) {
                binding.cofDeleteBtn.visibility = View.GONE
            } else {
                binding.cofDeleteBtn.visibility = View.VISIBLE
            }
        }
    }

}



