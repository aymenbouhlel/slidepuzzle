package jp.suwashimizu.th.slidepuzzle;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class HorizontalScrollCustomView extends HorizontalScrollView{

	public interface ScrollListener{
		public void onScrollChanged(HorizontalScrollCustomView scrollView,int x, int y, int oldx, int oldy);
	}
	
	private ScrollListener listener;
	
	public HorizontalScrollCustomView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		
	}

	public HorizontalScrollCustomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	public HorizontalScrollCustomView(Context context) {
		super(context);
		
	}
	
	public void setOnScrollListener(ScrollListener listenre){
		this.listener = listenre;
	}

	@Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (listener != null) {
            listener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }
}
