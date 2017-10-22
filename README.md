# Android_UI_Waves

# 设计思路
  * 贝塞尔曲线绘制水波纹（二阶贝塞尔曲线）`path.quadTo()`
  * 采用属性动画沿X轴模拟水波纹运动 `ValueAnimator`
  * 矩形切割计算出交点(利用微积分无限接近思想) `region.setPath(path,clip)`
  * 绘制圆角图片 `paint.setXfermode`

# 效果展示
![image](https://github.com/Jony-Li/Android_UI_Waves/blob/master/waves.gif)
