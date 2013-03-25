package jp.suwashimizu.th.slidepuzzle;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;



import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

public class SlideView extends View implements OnSharedPreferenceChangeListener{

	public int[] imgIds;//ココの順がモロリストに反映されるので注意
	public static final String MODE_KEY = "mode";

	Context context;
	Bitmap[] imgs;
	Puzzle puzzle;
	int[] gridState;
	int imgIndex;

	Bitmap empty;
	boolean assist;
	boolean useAssist;
	Paint hintPaint;
	Paint hintPaintEdge;

	int grid_w,grid_h;
	int w,h;
	Drawable d;

	SlidePuzzleCall call;
	boolean touch;

	Point[] ps;
	Paint hiLight;
	Paint shadow;
	Path hiLightPath;
	Path shadowPath;

	boolean slideMode;
	boolean isClear;
	
	public SlideView(Context context) {
		super(context);
		
		String[] ids = getResources().getStringArray(R.array.imgIds);
		imgIds = new int[ids.length];

		for(int i=0;i<imgIds.length;i++){
			imgIds[i] = getResources().getIdentifier(ids[i], "drawable", context.getPackageName());
			if(imgIds[i] == 0){
				try {
					throw new FileNotFoundException();
				} catch (FileNotFoundException e) {
					Log.e("fileNotFond","index="+i);
				}
			}
		}

		imgIndex = (int)(Math.random()*imgIds.length);

		this.context = context;
		call = (SlidePuzzleCall)context;

		float fontSize = getResources().getDimension(R.dimen.hint_text);

		hintPaint = new Paint();
		hintPaint.setColor(Color.argb(150, 0,0,0));
		hintPaint.setStrokeWidth(3);
		hintPaint.setStyle(Paint.Style.STROKE);
		hintPaint.setTextSize(fontSize);
		hintPaint.setTextAlign(Paint.Align.RIGHT);

		hintPaintEdge = new Paint();
		hintPaintEdge.setColor(Color.argb(255, 255, 255, 255));
		hintPaintEdge.setStrokeWidth(6);
		hintPaintEdge.setStyle(Paint.Style.STROKE);
		hintPaintEdge.setTextSize(fontSize);
		hintPaintEdge.setTextAlign(Paint.Align.RIGHT);

		getDispSize();
		createPath();
		this.gridState = new int[16];
		setImage();
	}

	public void moveImage(int index){
		imgIndex += index;
		if(imgIndex > imgIds.length-1)
			imgIndex = 0;
		if(imgIndex < 0)
			imgIndex = imgIds.length-1;
		setImage();
		createPazzle();
		setState();
	}

	public void setRandomImage(){
		
		imgIndex = (int)(Math.random()*imgIds.length);
		setImage();
		createPazzle();
		setState();
	}

	public void setIndexImage(int index){
		imgIndex = index;
		setImage();
		createPazzle();
		setState();
	}
	
	private void setState(){
		touch = false;
		isClear = false;
		assist = false;
		useAssist = false;
	}

	//なお使って無い模様
	public void resetImage(){
		setImage();
		setState();
	}

	public boolean isTouch(){
		return touch;
	}

	public void changeAssist(){
		assist = !assist;
		useAssist = true;
		redrawPaner();
	}

	public boolean useAssist(){
		return useAssist;
	}

	public String getCredit(){
		return getResources().getStringArray(R.array.author)[imgIndex].split(",")[1];
	}
	public String getCreditId(){
		return getResources().getStringArray(R.array.author)[imgIndex].split(",")[0];
	}
	public String getTitle(){
		return getResources().getStringArray(R.array.author)[imgIndex].split(",")[2];
	}

	//パスとかの作成
	private void createPath(){
		ps = new Point[8];
		for(int i=0;i<ps.length;i++){
			ps[i] = new Point();
		}
		ps[0].set(0, 0);
		ps[1].set(0, grid_h);
		ps[2].set(grid_w, grid_h);
		ps[3].set(grid_w, 0);
		ps[4].set((int)(grid_w*0.05), (int)(grid_h*0.05));
		ps[5].set((int)(grid_w*0.05), (int)(grid_h*0.95));
		ps[6].set((int)(grid_w*0.95), (int)(grid_h*0.95));
		ps[7].set((int)(grid_w*0.95),(int)(grid_h*0.05));

		hiLight = new Paint();
		hiLight.setAntiAlias(true);
		hiLight.setColor(Color.argb(180,255,255,255));

		shadow = new Paint();
		shadow.setColor(Color.argb(224,92,92,92));
		hiLight.setAntiAlias(true);

		hiLightPath = new Path();
		hiLightPath.moveTo(0, 0);
		hiLightPath.lineTo(ps[0].x, ps[0].y);
		hiLightPath.lineTo(ps[1].x, ps[1].y);
		hiLightPath.lineTo(ps[5].x, ps[5].y);
		hiLightPath.lineTo(ps[4].x, ps[4].y);

		hiLightPath.moveTo(0, 0);
		hiLightPath.lineTo(ps[0].x, ps[0].y);
		hiLightPath.lineTo(ps[3].x, ps[3].y);
		hiLightPath.lineTo(ps[7].x, ps[7].y);
		hiLightPath.lineTo(ps[4].x, ps[4].y);

		shadowPath = new Path();
		shadowPath.moveTo(grid_w, 0);
		shadowPath.lineTo(ps[2].x, ps[2].y);
		shadowPath.lineTo(ps[6].x, ps[6].y);
		shadowPath.lineTo(ps[7].x, ps[7].y);
		shadowPath.lineTo(ps[3].x, ps[3].y);

		shadowPath.moveTo(grid_w, grid_h);
		shadowPath.lineTo(ps[1].x, ps[1].y);
		shadowPath.lineTo(ps[5].x, ps[5].y);
		shadowPath.lineTo(ps[6].x, ps[6].y);
		shadowPath.lineTo(ps[2].x, ps[2].y);
	}

	//パネルの作成
	private void setImage(){

		imgs = new Bitmap[16];
		Bitmap b = Bitmap.createBitmap(w,h, Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Bitmap img =  BitmapFactory.decodeResource(context.getResources(), imgIds[imgIndex]);
		c.drawBitmap(img,new Rect(0, 0, img.getWidth(), img.getHeight()), new Rect(0, 0, c.getWidth(), c.getHeight()),null);

		b = Bitmap.createScaledBitmap(b, w,h, false);

		for(int i=0;i<4;i++)
			for(int j=0;j<4;j++){

				int start_X = grid_w*j;
				int start_Y = grid_h*i;

				imgs[i*4+j] = Bitmap.createBitmap(b,start_X,start_Y,grid_w,grid_h);
				c.setBitmap(imgs[i*4+j]);//分割イメージの描画
				//ハイライト描画
				c.drawPath(hiLightPath, hiLight);
				//シャドウの描画
				c.drawPath(shadowPath,shadow);

				c.drawRect(0, 0, grid_w, grid_h, hintPaint);//境界線の描画
				if(assist){
					c.drawText(""+(i*4+j+1), this.grid_w*0.9f, this.grid_h*0.9f, hintPaintEdge);
					c.drawText(""+(i*4+j+1), this.grid_w*0.9f, this.grid_h*0.9f, hintPaint);
				}
			}

		empty = imgs[15];

		c.setBitmap(imgs[15]);
		c.drawColor(Color.GRAY);

		b.recycle();
		img.recycle();
		setOriginalImg();

	}

	public void createPazzle(){
		puzzle = new Puzzle();
		int i=0;
		for(int[] box:puzzle.getGridState())
			for(int j:box){
				if(j == -1)
					j=15;
				gridState[i] =j;
				i++;
			}
		invalidate();
		call.puzzleReset();
	}

	private void redrawPaner(){
		for(Bitmap b:imgs){
			b.recycle();
		}

		Bitmap b = Bitmap.createBitmap(w,h, Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Bitmap img =  BitmapFactory.decodeResource(context.getResources(), imgIds[imgIndex]);
		c.drawBitmap(img,new Rect(0, 0, img.getWidth(), img.getHeight()), new Rect(0, 0, c.getWidth(), c.getHeight()),null);

		b = Bitmap.createScaledBitmap(b, w,h, false);
		for(int i=0;i<4;i++)
			for(int j=0;j<4;j++){
				imgs[i*4+j] = Bitmap.createBitmap(b,j*this.grid_w,i*this.grid_h,this.grid_w,this.grid_h);
				c.setBitmap(imgs[i*4+j]);
				//ハイライト描画
				c.drawPath(hiLightPath, hiLight);
				//シャドウの描画
				c.drawPath(shadowPath,shadow);
				c.drawRect(0, 0, this.grid_w, this.grid_h, hintPaint);

				if(assist){
					c.drawText(""+(i*4+j+1), this.grid_w*0.9f, this.grid_h*0.9f, hintPaintEdge);
					c.drawText(""+(i*4+j+1), this.grid_w*0.9f, this.grid_h*0.9f, hintPaint);
				}
			}

		empty = imgs[15];
		c.setBitmap(imgs[15]);
		c.drawColor(Color.GRAY);

		b.recycle();
		img.recycle();
		invalidate();
	}

	private void setOriginalImg(){
		d = context.getResources().getDrawable(imgIds[imgIndex]);
		d.setBounds(new Rect(0,0,w,w));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		if(oldw != 0){
			this.w = w;
			this.h = w;
			grid_w = w/4;
			grid_h = w/4;
			d = context.getResources().getDrawable(imgIds[imgIndex]);
			d.setBounds(new Rect(0,0,w,w));

			redrawPaner();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//canvas.drawColor(Color.WHITE);
		if(puzzle.getClearState()){
			d.draw(canvas);
		}else{
			for(int i=0;i<4;i++)
				for(int j=0;j<4;j++)
					canvas.drawBitmap(imgs[gridState[i*4+j]],grid_w*j,i*grid_h,null);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!isClear && !puzzle.getClearState()){

			if(event.getAction() == MotionEvent.ACTION_UP || slideMode){
				if(!touch){
					touch = true;
					call.puzzleStart();
				}
				if(puzzle.moveSlide((int)event.getX()/grid_w,(int)event.getY()/grid_h)){
					int i=0;
					for(int[] box:puzzle.getGridState())
						for(int j:box){
							if(j == -1)
								j=15;
							gridState[i] =j;
							i++;
						}
					invalidate();
				}
			}
			
			if(event.getAction() == MotionEvent.ACTION_UP || slideMode){
				if(puzzle.getClearState()){
						invalidate();
						//isClear = true;
						touch = false;
						call.puzzleClear();
				}
			}
//		}else if(event.getAction() == MotionEvent.ACTION_UP){
//			Intent intent = new Intent();
//			intent.setAction(Intent.ACTION_SEND);
//			intent.setType("text/plain");
//			//intent.setClassName("com.twitter.android","com.twitter.android.PostActivity");
//			intent.putExtra(Intent.EXTRA_TEXT,"てすとなう");
//			context.startActivity(Intent.createChooser(intent, null));
		}else if(!isClear && puzzle.getClearState() && event.getAction()  == MotionEvent.ACTION_UP){
			isClear = true;			
		}
		return true;
	}

	private void getDispSize(){
		Display disp = ((Activity)context).getWindowManager().getDefaultDisplay();
		Point size = new Point();
		try {
			// test for new method to trigger exception
			Class<?> pointClass = Class.forName("android.graphics.Point");
			Method newGetSize = Display.class.getMethod("getSize", new Class[]{ pointClass });

			// no exception, so new method is available, just use it
			newGetSize.invoke(disp, size);
			w = size.x;
			h = size.y;
			h = w;
		} catch(NoSuchMethodException ex) {
			// new method is not available, use the old ones
			w = disp.getWidth();
			h = disp.getHeight();
			h = w;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		grid_w = w/4;
		grid_h = h/4;

		d = context.getResources().getDrawable(imgIds[imgIndex]);
		d.setBounds(new Rect(0,0,w,w));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("mode"))
			slideMode = sharedPreferences.getBoolean(key, false);
	}

}
