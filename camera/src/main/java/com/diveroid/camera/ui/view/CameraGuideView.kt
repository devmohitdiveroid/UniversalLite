package com.diveroid.camera.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.diveroid.camera.R
import com.diveroid.camera.databinding.LayoutGuideViewBinding

class CameraGuideView: ConstraintLayout {
    private val TAG = CameraGuideView::class.java.simpleName

    private val guideImages = listOf(
        R.drawable.guide_001,
        R.drawable.guide_002
    )

    constructor(context: Context) : super(context) {
        initView()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private lateinit var binding: LayoutGuideViewBinding

    private val mAdapter = GuidePagerAdapter()
    private var exitListener: OnExitListener? = null

    private fun initView() {
        Log.d(TAG, "init: ")
        binding = LayoutGuideViewBinding.inflate(LayoutInflater.from(context), this, true)


        binding.pager.apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = mAdapter
        }
    }

    /**
     *  다음 페이지로 이동
     *  만약 페이지 인덱스가 guideImages.size 보다 크거나 같으면 가이드뷰는 숨김 처리
     */
    fun nextPage() {
        val currentIndex = binding.pager.currentItem
        if(currentIndex >= guideImages.size - 1) {
            visibility = GONE
            exitListener?.onExit()
        }
        binding.pager.setCurrentItem(currentIndex + 1, true)
    }

    fun setOnExitListener(l: OnExitListener) {
        exitListener = l
    }

    inner class GuidePagerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val imageView = ImageView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                scaleType = ImageView.ScaleType.FIT_XY
            }
            return GuideViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder is GuideViewHolder) {
                holder.onBindImage(guideImages[position])
            }
        }

        override fun getItemCount(): Int {
            return guideImages.size
        }
    }

    inner class GuideViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
        fun onBindImage(resId: Int) {
            imageView.setImageResource(resId)
        }
    }

    interface OnExitListener {
        fun onExit()
    }


}