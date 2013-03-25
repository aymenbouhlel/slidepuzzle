package jp.suwashimizu.th.slidepuzzle;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class GalleryActivity extends Activity implements HorizontalScrollCustomView.ScrollListener{

	ImageView iv;
	LinearLayout linear;
	int[] resIds;
	TextView indexNum;
	HorizontalScrollCustomView horizontal;
	int thumbnailSize;
	int dispWidth;
	LimitCache<Bitmap> cache;
	Bitmap emptyImg;
	Rect r;
	Handler handler;
	ExecutorService service;

	static final int CACHE_SIZE=100;//キャッシュのサイズAVDでも稼動確認
	static final int FULLIMG_INDEX=11;

	public interface ScrollViewListener{
		public void onScollCahnge();
	}

	class ImgInfo{
		boolean isClear;
		int index;
		public ImgInfo(int index,boolean isClear) {
			this.isClear = isClear;
			this.index = index;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gallery);

		handler = new Handler();
		service = Executors.newFixedThreadPool(1);
		cache = new LimitCache<Bitmap>(CACHE_SIZE);

		horizontal = (HorizontalScrollCustomView)findViewById(R.id.horizontalScrollView1);
		horizontal.setOnScrollListener(this);

		float density = getResources().getDisplayMetrics().density;
		int imgSize = (int)(100*density);

		iv = (ImageView)findViewById(R.id.imageView1);
		iv.setOnClickListener(imageOnClick);
		iv.setTag(null);

		indexNum = (TextView)findViewById(R.id.textView1);

		linear = (LinearLayout)findViewById(R.id.linearLayout);
		String[] ids = getResources().getStringArray(R.array.imgIds);
		resIds = new int[ids.length];		

		r = new Rect(0, 0, imgSize,imgSize);
		emptyImg = Bitmap.createBitmap(imgSize,imgSize, Config.ARGB_8888);

		for(int i=0;i<ids.length;i++){
			resIds[i] = getResources().getIdentifier(ids[i], "drawable", getPackageName());
			if(resIds[i] == 0){
				try {
					throw new FileNotFoundException();
				} catch (FileNotFoundException e) {
					Log.e("fileNotFond","index="+i);
				}
			}

			ImageView iv = new ImageView(this);
			if(getClearFlag(i)){

				iv.setTag(new ImgInfo(i, true));
				iv.setImageResource(R.drawable.loading);//読み込み中の表示
				ImageCache ic = new ImageCache(iv, i);
				service.execute(ic);
				
			}else{
				iv.setImageResource(R.drawable.secret);
				iv.setTag(new ImgInfo(i, false));//indexとクリアフラグをTagつけ				
			}
			iv.setOnClickListener(thumbnailOnClick);
			linear.addView(iv,new LayoutParams(imgSize, imgSize));
		}
	}	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK && requestCode == FULLIMG_INDEX){
			Intent intent = new Intent();
			intent.putExtra("index",data.getIntExtra("index", -1));
			setResult(RESULT_OK,intent);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private OnClickListener thumbnailOnClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			ImgInfo info = (ImgInfo)v.getTag();
			if(info.isClear){
				iv.setImageResource(resIds[info.index]);
				indexNum.setVisibility(View.INVISIBLE);
			}
			else{
				iv.setImageResource(R.drawable.secretbig);
				indexNum.setText(String.format(getString(R.string.index_num),info.index+1,resIds.length));
				indexNum.setVisibility(View.VISIBLE);
			}
			iv.setTag(info);
		}
	};

	private OnClickListener imageOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(v.getTag() != null){
				ImgInfo info = (ImgInfo)v.getTag();
				if(info.isClear){
					Intent intent = new Intent(getBaseContext(),ImageActivty.class);
					intent.putExtra("index", info.index);
					startActivityForResult(intent,FULLIMG_INDEX);
				}else{
					Intent intent = new Intent();
					intent.putExtra("index", info.index);
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		}
	};

	private boolean getClearFlag(int index){
		boolean flg;
		String id = getResources().getStringArray(R.array.author)[index].split(",")[0];
		SharedPreferences pref = getSharedPreferences(MainActivity.PREF, MODE_PRIVATE);
		if(pref.getLong(id, -1) != -1)
			flg = true;
		else
			flg = false;
		return flg;
	}	

	@Override
	public void onScrollChanged(HorizontalScrollCustomView scrollView, int x,
			int y, int oldx, int oldy) {
		/*
		if(dispWidth != linear.getWidth()){
			thumbnailSize = linear.getChildAt(0).getWidth();
			Log.d("aaa",""+thumbnailSize);
			dispWidth = linear.getWidth();
		}
		if(x/thumbnailSize != oldx/thumbnailSize){
			int index = x/thumbnailSize;

			for(int i=0;i<CACHE_SIZE;i++){
				if(x/thumbnailSize+i > resIds.length-1)
					return;
				ImageView v = (ImageView) linear.getChildAt(x/thumbnailSize+i);
				ImgInfo info = (ImgInfo) v.getTag();
				if(info.isClear){

					Bitmap b = cache.getCache(index+i);
					if(b != null){

						v = (ImageView)linear.getChildAt(index+i);
						v.setImageBitmap(b);
					}else{
						v.setImageResource(R.drawable.secret);//読み込み中の画像を用意
					}
				}
			}

		}*/
	}

	@Override
	public void finish(){
		dispWidth = -1;
		service.shutdown();
		service.shutdownNow();
		super.finish();
	}

	class ImageCache implements Runnable{

		ImageView iv;
		int index;

		public ImageCache(ImageView view,int index) {
			handler = new Handler();
			iv = view;
			this.index = index;
		}

		@Override
		public synchronized void run(){
			//BMP生成
			if(index > resIds.length-1)
				if(cache.isCached(index))
					return;
			if(dispWidth == -1)
				return;
			final Bitmap bmp = Bitmap.createBitmap(emptyImg);
			Canvas canvas = new Canvas(bmp);
			Drawable d = getResources().getDrawable(resIds[index]);
			d.setBounds(r);
			d.draw(canvas);
			cache.cache(index, bmp);
			handler.post(new Runnable() {

				@Override
				public void run() {
					iv.setImageBitmap(bmp);
				}
			});
		}
	}
}
