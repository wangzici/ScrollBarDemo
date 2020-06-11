package com.kyrie.demo.scrollbar

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
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
    private var mVerticalThumbHeight: Int = 0//滑块高度
    private var mVerticalThumbWidth: Int = 0//滑块宽度
    private var mVerticalThumbTop: Int = 0//滑块当前起点位置
    private var mThumbDrawable: Drawable? = null//滑块drawable
    private var mTrackDrawable: Drawable? = null//滑道drawable

    init {
        mThumbDrawable = ContextCompat.getDrawable(getContext(), R.color.colorAccent)
        mTrackDrawable = ContextCompat.getDrawable(getContext(), R.color.colorPrimary)
        mVerticalThumbWidth = 10
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
     * 与ScrollView绑定
     * @param nestedScrollView 绑定的ScrollView,由于默认的ScrollView不自带滑动监听,所以此处用的是NestedScrollView
     */
    fun attachScrollView(nestedScrollView: NestedScrollView) {
        nestedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            calculate(nestedScrollView)
        }
        val child = nestedScrollView.getChildAt(0)
        //由于一般ScrollView的子View都是TextView，所以我们需要在TextView的内容变换之后重新测量
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
        }
    }

    private fun calculate(nestedScrollView: NestedScrollView) {
        //ScrollView的高度
        val visibleHeight = nestedScrollView.measuredHeight
        //ScrollView内部的内容高度
        val contentHeight = nestedScrollView.getChildAt(0)?.height ?: 0
        //若不需要滚动，则直接隐藏
        if (contentHeight <= visibleHeight) {
            visibility = INVISIBLE
            return
        } else {
            visibility = VISIBLE
        }
        //当前ScrollView内容滚动的距离
        val scrollY = nestedScrollView.scrollY
        //计算出滑块的高度
        mVerticalThumbHeight = measuredHeight * visibleHeight / contentHeight
        //滑块的top值范围是从0到{滑道高度-滑块高度}
        mVerticalThumbTop =
            (measuredHeight - mVerticalThumbHeight) * scrollY / (contentHeight - visibleHeight)
        showNow()
        invalidate()
    }

    private val dismissRunnable = Runnable {
        if (isShown) {
            ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).setDuration(500).start()
        }
    }

    /**
     * 立刻显示并延迟消失
     */
    private fun showNow() {
        alpha = 1f
        postDelayDismissRunnable()
    }

    private fun postDelayDismissRunnable() {
        removeCallbacks(dismissRunnable)
        postDelayed(dismissRunnable, 1000)
    }
}