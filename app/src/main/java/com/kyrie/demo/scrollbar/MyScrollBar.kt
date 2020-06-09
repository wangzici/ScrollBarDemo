package com.kyrie.demo.scrollbar

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView

/**
 * Created by Kyrie
 * Date: 2020/6/9
 *
 */
class MyScrollBar(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    var mVerticalThumbHeight: Int = 0//滑块高度
    var mVerticalThumbWidth: Int = 0//滑块宽度
    var mVerticalThumbTop: Int = 0//滑块当前起点位置
    var mThumbDrawable: Drawable? = null//滑块drawable
    var mTrackDrawable: Drawable? = null//滑道drawable
    private val mHandler = Handler(Looper.getMainLooper())

    init {
        mThumbDrawable = ContextCompat.getDrawable(getContext(), R.color.colorAccent)
        mTrackDrawable = ContextCompat.getDrawable(getContext(), R.color.colorPrimary)
        mVerticalThumbWidth = 5
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) {
            return
        }

        //滑块的top
        val top = mVerticalThumbTop
        //滑块的bottom
        val bottom = mVerticalThumbTop + mVerticalThumbHeight

        //先绘制滑道
        mTrackDrawable?.setBounds(0, 0, mVerticalThumbWidth, measuredHeight)
        mTrackDrawable?.draw(canvas)

        //再绘制滑块
        mThumbDrawable?.setBounds(0, top, mVerticalThumbWidth, bottom)
        mThumbDrawable?.draw(canvas)
    }

    /**
     * @param nestedScrollView 绑定的ScrollView
     */
    fun attachScrollView(nestedScrollView: NestedScrollView) {
        nestedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            calculate(nestedScrollView)
            showNow()
        }
        val child = nestedScrollView.getChildAt(0)
        if (child is TextView) {
            child.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    calculate(nestedScrollView)
                }
            })
        }
        //调用太早无法获取测量高度
        post {
            calculate(nestedScrollView)
            showNow()
        }
    }

    private fun calculate(nestedScrollView: NestedScrollView) {
        val visibleHeight = nestedScrollView.measuredHeight
        val contentHeight = nestedScrollView.getChildAt(0)?.height ?: 0
        if (contentHeight <= visibleHeight) {
            visibility = INVISIBLE
            return
        } else {
            visibility = VISIBLE
        }
        val scrollY = nestedScrollView.scrollY
        mVerticalThumbHeight = measuredHeight * visibleHeight / contentHeight//计算出滑块的高度
        mVerticalThumbTop =
            (measuredHeight - mVerticalThumbHeight) * scrollY / (contentHeight - visibleHeight)//滑块的top值范围是从0到{滑道高度-滑块高度}
        invalidate()
    }

    private val dismissRunnable = Runnable {
        ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).setDuration(1000).start()
    }

    private fun showNow() {
        alpha = 1f
        postDelayDismissRunnable()
    }

    private fun postDelayDismissRunnable() {
        mHandler.removeCallbacks(dismissRunnable)
        mHandler.postDelayed(dismissRunnable, 2000)
    }
}