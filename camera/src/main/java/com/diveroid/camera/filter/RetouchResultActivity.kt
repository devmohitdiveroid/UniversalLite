package com.diveroid.camera.filter

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.diveroid.camera.R
import com.diveroid.camera.data.MediaData
import com.diveroid.camera.databinding.ActivityRetouchResultBinding
import com.diveroid.camera.databinding.LayoutVideoItemBinding
import com.diveroid.camera.mediaCtrl.ImageFilter
import com.diveroid.camera.mediaCtrl.MediaFileControl
import com.diveroid.camera.underfilter.FileUtils2
import com.diveroid.camera.underfilter.GL2JNILib
import com.diveroid.camera.underfilter.GL2JNIVideoView
import com.diveroid.camera.utils.BitmapUtils
import com.diveroid.camera.utils.ImageSaver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.io.File

abstract class RetouchResultActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    abstract fun saveCorrectedVideo(
        mediaId: Long,
        filePath: String,
        thumbPath: String,
        filtered: Boolean,
    )

    abstract fun saveCorrectedImage(mediaId: Long, filtered: Boolean)

    private val TAG = this::class.java.simpleName
    private lateinit var binding: ActivityRetouchResultBinding
    private val mediaUris: List<MediaData>? by lazy {
        val token = object : TypeToken<List<MediaData>>() {}.type
        Gson().fromJson(intent.getStringExtra(IMAGE_OR_VIDEO_URIS), token)
    }

    private val adapter: ViewPagerAdapter = ViewPagerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityRetouchResultBinding?>(
            this,
            R.layout.activity_retouch_result
        ).apply {
            lifecycleOwner = this@RetouchResultActivity
            filterSeekbar.setOnSeekBarChangeListener(this@RetouchResultActivity)
            btnClose.setOnClickListener {
                onBackPressed()
            }

            btnSave.setOnClickListener {
                saveAction()
            }
        }
        loadLibrary()

        if (mediaUris.isNullOrEmpty()) {
            // 이전 화면에서 전달받은 미디어(이미지, 비디오) URI가 없는 경우 이전 화면으로 전환
            Toast.makeText(
                this,
                getString(R.string.media_filter_item_empty),
                Toast.LENGTH_SHORT
            ).show()
            onBackPressed()
        }

        binding.pager.apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 1
            clipChildren = false
            clipToPadding = false

            val density = resources.displayMetrics.density
            val padding = 12 * density
            if (mediaUris!!.size > 1) {
                setPadding(padding.toInt(), 0, padding.toInt(), 0)
            }
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State,
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
//                    val space  = 12 * density// px
                    if (parent.adapter!!.itemCount > 1) {
                        outRect.set(12, 0, 12, 0)
                    }
                }
            })

            val transform = CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(8))
            }
            setPageTransformer(transform)
            registerOnPageChangeCallback(viewPageCallback)
            adapter = this@RetouchResultActivity.adapter
        }

        binding.btnCancel.setOnClickListener {
            isCancel = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // unregister ViewPager Callback
        binding.pager.unregisterOnPageChangeCallback(viewPageCallback)
        GL2JNILib.onDestroy()
    }

    fun onBack() {
        onBackPressed()
    }

    private var filteredIndex: Int = 0
    private var isCancel: Boolean = false

    private fun saveAction() {
        filterProcess()
    }

    private fun filterProcess() {
        if (isCancel) {
            isCancel = false
            binding.layoutLoading.visibility = View.GONE
            setResult(RESULT_CANCELED)
            return
        }

        val size = mediaUris?.size ?: 0
        if (filteredIndex >= size) { // 완료
            runOnUiThread {
                binding.layoutLoading.visibility = View.GONE

                val resultIntent = Intent().apply {
                    putExtra(
                        CALLBACK_NAME,
                        this@RetouchResultActivity.intent.getStringExtra(
                            CALLBACK_NAME
                        )
                    )
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            return
        }

        runOnUiThread {
            binding.layoutLoading.visibility = View.VISIBLE
            binding.pager.setCurrentItem(filteredIndex, true)
            binding.txtProcessContent.text = getString(
                R.string.media_filter_calibrating_format,
                filteredIndex + 1,
                mediaUris!!.size
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val mediaData = mediaUris?.get(filteredIndex)!!
            if (mediaData.isImage) { // 이미지
                val filePath = FileUtils2.getFileName(this, Uri.parse(mediaData.fileName))
                val thumbPath = FileUtils2.getFileName(this, Uri.parse(mediaData.thumbName))
                ImageSaver(
                    mediaData.after ?: mediaData.before!!,
                    filePath!!,
                    thumbPath!!,
                    object : ImageSaver.OnImageSaverListener {
                        override fun onImageSaveCompleted(filePath: String, thumbPath: String) {
                            saveCorrectedImage(mediaData.mediaId, filterLevel > 0)
                            runOnUiThread {
                                filteredIndex += 1
                                filterProcess()
                            }
                        }

                        override fun onImageSaveFailed(msg: String) {

                        }
                    }
                ).execute()
            } else {
                GL2JNILib.stopVideo()
                MediaFileControl.init(applicationContext)
                val originPath = MediaFileControl.getLocalMediaPath(
                    Uri.parse(mediaData.fileName).lastPathSegment,
                    false
                )
                val filteredPath = MediaFileControl.getLocalMediaPath(
                    MediaFileControl.createVideoFileName(),
                    false
                )
                GL2JNILib.setScale(filterLevel.toFloat())
                GL2JNILib.startVideo(originPath, filteredPath)
                GL2JNILib.startAnimation()
            }
        }, 1000)
    }

    private fun loadLibrary() {
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    System.loadLibrary("ImageFilter")
                    // 이미지 필터 기본값 1.0으로 되어 있음. filterScaleSeek값도 그에 해당하는 값으로 맞추기.
                    binding.filterSeekbar.progress = 50
                }

                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    private var filterLevel = 0.0

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (seekBar != null && seekBar.id == R.id.filter_seekbar) {
            binding.markerOrigin.visibility = if (progress == 0) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

            val tmp = progress / 100.0

            filterLevel = if (tmp <= 0.5) {
                tmp * 2.0
            } else {
                (tmp - 0.5) + 1.0
            }

            binding.seekbarValue.apply {
                val lp = layoutParams as ConstraintLayout.LayoutParams
                lp.horizontalBias = tmp.toFloat()
                layoutParams = lp
                text = "${(filterLevel * 100f).toInt()}%"
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        binding.pager.adapter?.notifyItemRangeChanged(0, mediaUris?.size ?: 0)
    }

    companion object {
        const val REQUEST_CODE_RETOUCH = 10000

        // extra keys
        const val IMAGE_OR_VIDEO_URIS = "IMAGE_OR_VIDEO_URIS"
        const val CALLBACK_NAME = "CALLBACK_NAME"

        const val ITEM_VIEW_TYPE_IMAGE: Int = 1
        const val ITEM_VIEW_TYPE_VIDEO = 2
    }

    inner class ViewPagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == ITEM_VIEW_TYPE_IMAGE) {
                val imageView = ImageView(applicationContext).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                ViewHolderPage(imageView)
            } else {
                VideoHolderItem(
                    LayoutVideoItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is ViewHolderPage -> {
                    holder.onBind(mediaUris!![position])
                }

                is VideoHolderItem -> {
                    holder.onBind(mediaUris!![position])
                }
            }
        }

        override fun getItemCount(): Int {
            return mediaUris?.count() ?: 0
        }

        override fun getItemViewType(position: Int): Int {
            return if (mediaUris!![position].isImage) {
                ITEM_VIEW_TYPE_IMAGE
            } else {
                ITEM_VIEW_TYPE_VIDEO
            }
        }
    }

    inner class ViewHolderPage(val imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
        lateinit var data: MediaData

        fun onBind(data: MediaData) {
            this.data = data

            if (data.before == null) {
                loadImage()
            } else {
                doFiltering()
            }
        }

        private fun loadImage() {
            Glide.with(this@RetouchResultActivity).asBitmap()
                .load(data.fileName)
                .addListener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        data.before = resource
                        doFiltering()
                        return false
                    }
                })
                .into(imageView)
        }

        private fun doFiltering() {
            if (data.before == null) {
                loadImage()
                return
            }

            ImageFilter(
                data.before!!.copy(data.before!!.config, true),
                filterLevel,
                object : ImageFilter.Callback {
                    override fun onResult(success: Boolean, result: Bitmap?) {
                        if (success && result != null && !result.isRecycled) {
                            runOnUiThread {
                                imageView.setImageBitmap(result)
                            }
                            data.after?.let {
                                if (it.isRecycled) it.recycle()
                            }
                            data.after = result
                        } else {
                            if (result != null && !result.isRecycled) result.recycle()
                        }
                    }
                }
            ).execute()
        }
    }

    inner class VideoHolderItem(private val layoutBinding: LayoutVideoItemBinding) :
        RecyclerView.ViewHolder(layoutBinding.root) {
        private lateinit var data: MediaData
        private var videoView: GL2JNIVideoView? = null

        fun onBind(data: MediaData) {
            this.data = data
            changeVideoViewRootLayout()
            loadThumbnail()
        }

        fun loadVideoView() {
            addVideoView()
        }

        fun unloadVideoView() {
            removeVideoView()
        }

        private fun changeVideoViewRootLayout() {
            (layoutBinding.videoArea.layoutParams as ConstraintLayout.LayoutParams).apply {
                val fileUri = Uri.parse(data.fileName)
                MediaFileControl.init(this@RetouchResultActivity)
                val filePath = MediaFileControl.getLocalMediaPath(fileUri.lastPathSegment, false)!!
                val videoSize = FileUtils2.getVideoSize(filePath)
                val ratio = videoSize.width.toFloat() / videoSize.height.toFloat()
                dimensionRatio = "$ratio"
            }
            layoutBinding.videoArea.requestLayout()
        }

        /**
         * add GL2JNIView in ViewHolder
         *
         * GL2JNIView 포함된 viewholder 여러 개인 경우에 아래와 같은 현상이 발생하여
         * 뷰를 추가/삭제하는 방식으로 처리
         *
         * 1. 깜박이는 현상
         * 2. 랜더링 이미지가 혼합되는 현상
         *
         * @see removeVideoView
         */
        private fun addVideoView() {
            if (videoView != null) return

            layoutBinding.imgThumbnail.visibility = View.GONE
            videoView = GL2JNIVideoView(this@RetouchResultActivity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setGL2JNILibCallback(object : GL2JNILib.Callback() {
                    override fun GL2JNIVideoPlayStart(incode: Boolean, savedFile: String?) {
                        super.GL2JNIVideoPlayStart(incode, savedFile)
                    }

                    override fun GL2JNIVideoPlaying(
                        incode: Boolean,
                        currentMilisecond: Long,
                        totalMiliseecond: Long,
                    ) {
                        super.GL2JNIVideoPlaying(incode, currentMilisecond, totalMiliseecond)

                        val currentPlayTimeMin = currentMilisecond / 1000 / 60
                        val currentPlayTimeSec = currentMilisecond / 1000 % 60
                        val totalPlayTimeMin = totalMiliseecond / 1000 / 60
                        val totalPlayTimeSec = totalMiliseecond / 1000 % 60

                        val progress =
                            ((currentMilisecond / totalMiliseecond.toFloat()) * 100).toInt()

                        runOnUiThread {
                            layoutBinding.imgThumbnail.visibility = View.GONE
                            layoutBinding.btnPlay.visibility = View.GONE
                            layoutBinding.txtPlayTime.text =
                                String.format("%02d:%02d", currentPlayTimeMin, currentPlayTimeSec)
                            layoutBinding.txtTotalTime.text =
                                String.format("%02d:%02d", totalPlayTimeMin, totalPlayTimeSec)


                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                layoutBinding.playerBar.setProgress(progress, true)
                            } else {
                                layoutBinding.playerBar.progress = progress
                            }
                        }
                    }

                    override fun GL2JNIVideoPlayFinished(incode: Boolean, savedFile: String?) {
                        super.GL2JNIVideoPlayFinished(incode, savedFile)
                        if (incode) {
                            val newThumbnailPath = saveVideoThumbNail(savedFile)
                            saveCorrectedVideo(
                                data.mediaId,
                                savedFile!!,
                                newThumbnailPath,
                                filterLevel > 0
                            )

                            filteredIndex += 1
                            filterProcess()
                        }

                        runOnUiThread {
                            loadThumbnail()
                            layoutBinding.btnPlay.visibility = View.VISIBLE
                        }
                        GL2JNILib.stopVideo()
                    }

                    override fun GL2JNIVideoPlayInterrupted(incode: Boolean, savedFile: String?) {
                        super.GL2JNIVideoPlayInterrupted(incode, savedFile)
                    }

                    override fun GL2JNIVideoPlayStopted(incode: Boolean, savedFile: String?) {
                        super.GL2JNIVideoPlayStopted(incode, savedFile)
                    }

                    override fun GL2JNIImageConvertFinished(bitmap: Bitmap?, savedFile: String?) {
                        super.GL2JNIImageConvertFinished(bitmap, savedFile)
                    }
                })
            }
            layoutBinding.videoArea.addView(videoView, 0)
            loadThumbnail()
        }

        /**
         * remove GL2JNIView in parent view
         *
         * GL2JNIView 포함된 viewholder 여러 개인 경우에 아래와 같은 현상이 발생하여
         * 뷰를 추가/삭제하는 방식으로 처리하였습니다.
         *
         * 1. 깜박이는 현상
         * 2. 랜더링 이미지가 혼합되는 현상
         *
         * @see addVideoView
         */
        private fun removeVideoView() {
            layoutBinding.imgThumbnail.visibility = View.VISIBLE

            videoView?.let {
                layoutBinding.videoArea.removeView(videoView)
                videoView = null
            }
        }

        // TODO:
        // FIXME:
        //  페이지 전환 시 이미지 깜빡거림 관련 수정 작업 필요

        private fun loadThumbnail() {
            Glide.with(this@VideoHolderItem.itemView).asBitmap()
                .load(data.thumbName)
                .addListener(object : RequestListener<Bitmap> {


                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        Log.e(TAG, "onLoadFailed: ")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        resource.let {
                            data.before = resource
                            if (filterLevel > 0) {
                                GL2JNILib.setScale(filterLevel.toFloat())
                                videoView?.onFilterThread()
                            } else {
                                videoView?.offFilterThread()
                            }
                            GL2JNILib.startImage(it, it.width, it.height, "")

                            if (layoutBinding.imgThumbnail.visibility == View.VISIBLE) {
                                layoutBinding.imgThumbnail.setImageBitmap(resource)
                                doFilter()
                            }
                        }
                        return false
                    }
                }).submit()
        }

        fun doFilter() {
            if (filterLevel > 0.0) {
                if (layoutBinding.imgThumbnail.isVisible) {
                    if (data.after != null) {
                        layoutBinding.imgThumbnail.setImageBitmap(data.after)
                    } else {
                        ImageFilter(
                            data.before!!.copy(data.before!!.config, true),
                            filterLevel,
                            object : ImageFilter.Callback {
                                override fun onResult(success: Boolean, result: Bitmap?) {
                                    if (success && result != null && !result.isRecycled) {
                                        runOnUiThread {
                                            layoutBinding.imgThumbnail.setImageBitmap(result)
                                        }
                                        data.after?.let {
                                            if (it.isRecycled) it.recycle()
                                        }
                                        data.after = result
                                    } else {
                                        if (result != null && !result.isRecycled) result.recycle()
                                    }
                                }
                            }
                        ).execute()
                    }
                }
            } else {
                if (layoutBinding.imgThumbnail.isVisible) {
                    layoutBinding.imgThumbnail.setImageBitmap(data.before)
                }
            }

            videoView?.let {
                it.onFilterThread()
                GL2JNILib.setScale(filterLevel.toFloat())
            }
        }

        init {
            layoutBinding.btnPlay.setOnClickListener {
                layoutBinding.imgThumbnail.visibility = View.GONE
                videoView?.let {
                    GL2JNILib.stopVideo()
                    MediaFileControl.init(applicationContext)
                    val filePath = MediaFileControl.getLocalMediaPath(
                        Uri.parse(data.fileName).lastPathSegment,
                        false
                    )
                    GL2JNILib.startVideo(filePath, "")
                }
            }

            layoutBinding.playerBar.setOnTouchListener { view, _ ->
                view.performClick()
                return@setOnTouchListener true
            }
        }
    }

    private val viewPageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            Log.d(TAG, "onPageScrolled: position = $position")

            mediaUris?.forEachIndexed { index, mediaData ->
                if (mediaData.isVideo) {
                    val viewHolder =
                        (binding.pager[0] as RecyclerView).findViewHolderForAdapterPosition(index)
                    if (viewHolder is VideoHolderItem) {
                        if (position == viewHolder.adapterPosition) {
                            viewHolder.loadVideoView()
                        } else {
                            viewHolder.unloadVideoView()
                        }
                    }
                }
            }
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Log.d(TAG, "onPageSelected: position = $position")
        }
    }

    private fun saveVideoThumbNail(savedFile: String?): String {
        if (savedFile.isNullOrEmpty()) return ""

        val file = File(savedFile)
        val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val size = FileUtils2.getVideoSize(savedFile)
            ThumbnailUtils.createVideoThumbnail(file, size, null)
        } else {
            ThumbnailUtils.createVideoThumbnail(savedFile, MediaStore.Video.Thumbnails.MINI_KIND)
        }

        return if (thumbnail != null) {
            val thumbnailPath = MediaFileControl.getLocalMediaPath(file.name, true)!!
            val resizedBitmap =
                BitmapUtils.getResizedBitmap(thumbnail, MediaFileControl.SIZE_THUMBNAIL_X)
            BitmapUtils.saveBitmap(
                thumbnailPath,
                resizedBitmap
            )
            thumbnail.recycle()
            resizedBitmap.recycle()
            thumbnailPath
        } else {
            ""
        }
    }
}
