package com.dream.highlighteditor.view;

import android.annotation.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.dream.highlighteditor.*;

/*
 符号栏类
 */
public class SymbolView {
    private final int TILE_WIDTH = 60;
    private PopupWindow popupWindow;
    private View rootView;
    private OnSymbolViewClick onSymbolViewClick;
    private boolean visible = false;
    private InputMethodManager inputMethodManager;
    private boolean isFirst = true;
    private int maxLayoutHeight = 0;//布局总长
    private int currentLayoutHeight = 0;//当前布局高

    @SuppressLint("ClickableViewAccessibility")
    public SymbolView(final Context context, final View rootView) {
        this.rootView = rootView;
        popupWindow = new PopupWindow(context);
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = LayoutInflater.from(context).inflate(R.layout.symbol_view, null);
        LinearLayout linearLayout = view.findViewById(R.id.linear_container);
        final float[] tempPoint = new float[2];
        String symbol = "→ { } ( ) ; , . = \" | ' & ! [ ] < > + - \\/ * ? : _";
        for (int i = 0; i < symbol.length(); i++) {
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            textView.setText(String.valueOf(symbol.charAt(i)));
            textView.setClickable(true);
            textView.setTextSize(25);
            textView.setWidth(TILE_WIDTH);
            textView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int color = v.getDrawingCacheBackgroundColor();
                    int motionEvent = event.getAction();
                    TextView tv = (TextView) v;

                    if (motionEvent == MotionEvent.ACTION_DOWN) {
                        tempPoint[0] = event.getX();
                        tempPoint[1] = event.getY();
                        tv.setBackgroundColor(0xffcecfd1);

                    } else if (motionEvent == MotionEvent.ACTION_UP || motionEvent == MotionEvent.ACTION_CANCEL) {
                        tv.setBackgroundColor(0xffe4e7e9);
                        if (Math.abs(event.getX() - tempPoint[0]) < TILE_WIDTH) {
                            if (onSymbolViewClick != null)
                                onSymbolViewClick.onClick(tv, tv.getText().toString());
                        }
                    }
                    return true;
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.addView(textView, layoutParams);

        }
        popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        //popupWindow.setHeight(EditCodeActivity.height);
        popupWindow.getBackground().setAlpha(0);//窗口完全透明
        view.setBackgroundColor(0xffe4e7e9);//视图不完全透明

        popupWindow.setContentView(view);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        rootView.getWindowVisibleDisplayFrame(r);
                        if (isFirst) {
                            maxLayoutHeight = r.bottom;//初始化时为布局的最高高度
                            currentLayoutHeight = r.bottom;//当前弹出的布局高
                            isFirst = false;
                        } else {
                            currentLayoutHeight = r.bottom;//当前弹出的布局高
                        }
                        if (currentLayoutHeight == maxLayoutHeight || !visible) {
                            hide();
                        } else if (currentLayoutHeight < maxLayoutHeight) {
                            show(rootView.getHeight() - r.bottom);
                        }
                    }
                });
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    private void show(int bottom) {
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, bottom);
    }

    private void hide() {
        popupWindow.dismiss();
    }

    public void setOnSymbolViewClick(OnSymbolViewClick onSymbolViewClick) {
        this.onSymbolViewClick = onSymbolViewClick;
    }


    public interface OnSymbolViewClick {
        void onClick(View view, String text);
    }
}
