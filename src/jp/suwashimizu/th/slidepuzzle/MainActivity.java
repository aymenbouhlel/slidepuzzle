package jp.suwashimizu.th.slidepuzzle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;



import net.nend.android.NendAdView;

import android.os.Bundle;
import android.os.Handler;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements SlidePuzzleCall,DialogListener{

	static public final String PREF = "PAZZLE";
	SlideView sv;

	int dispW,dispH;

	long startTime;
	//long clearTime;
	long spentTime;
	SimpleDateFormat sdf;
	TextView playTime;
	TextView indexText;

	Handler timeCount;
	public static final int REQUEST_GALLERY=1;
	boolean slideMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		getDispSize();

		timeCount = new Handler();
		playTime = (TextView)findViewById(R.id.textView2);
		sdf = new SimpleDateFormat("mm:ss");
		indexText = (TextView)findViewById(R.id.textView3);

		slideMode = readTouchModePref();

		LayoutParams lp0 = new LayoutParams(dispW,dispH);
		//lp0.weight = 1;
		sv = new SlideView(this);
		sv.createPazzle();
		sv.slideMode = slideMode;
		sv.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if(event.getAction() == MotionEvent.ACTION_UP)
					if(spentTime != -1 && sv.isClear){//clearTime
						try{
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_SEND);
							//intent.setType("text/plain");
							intent.setClassName("com.twitter.android","com.twitter.android.PostActivity");
							String twwet = String.format(getString(R.string.twwet), sv.getTitle(),playTime.getText().toString());

							intent.putExtra(Intent.EXTRA_TEXT,twwet);
							startActivity(intent);
							//						startActivity(Intent.createChooser(intent, null));

						}catch(ActivityNotFoundException e){
							e.printStackTrace();
							Toast.makeText(getBaseContext(), R.string.twitterNotFound, Toast.LENGTH_SHORT).show();
						}
					}

				return sv.onTouchEvent(event);
			}
		});
		LinearLayout layout = ((LinearLayout)findViewById(R.id.LinearLayout));
		layout.addView(sv,0,lp0);

		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		pref.registerOnSharedPreferenceChangeListener(sv);

		/*
		ADMobの設定
		AdRequest adRequest = new AdRequest();
		adRequest.addTestDevice("AdRequest.TEST_EMULATOR");//テストモード
		adRequest.addTestDevice("B20A48E63368CFB4D4E7F89FF8D2FA59");
		AdView adView = new AdView(this, AdSize.BANNER,"a1512f4c9b3d71e");//a1512f4c9b3d71e
		adView.loadAd(adRequest);
		layout.addView(adView);*/


		//nendの設定
		NendAdView nendAdView = new NendAdView(getApplicationContext(),38255,"e263d431f4533a30046d66dbf2acf12e20631f13");
		layout.addView(nendAdView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		nendAdView.setPadding(0, 5, 0, 0);
		//layout.addView(nendAdView);

		Button btn = (Button)findViewById(R.id.button1);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sv.changeAssist();
			}
		});
		btn = (Button)findViewById(R.id.button4);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sv.moveImage(-1);
			}
		});
		btn = (Button)findViewById(R.id.button5);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sv.moveImage(1);
				setClearTime();
			}
		});
		//		btn = (Button)findViewById(R.id.button3);
		//		btn.setOnClickListener(new OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				sv.createPazzle();
		//				setClearTime();
		//			}
		//		});

		btn = (Button)findViewById(R.id.button2);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sv.setRandomImage();
			}
		});

		((TextView)findViewById(R.id.textView1)).setMovementMethod(LinkMovementMethod.getInstance());
		//indexText.setText(String.format(getString(R.string.index_num), sv.imgIndex+1,sv.imgIds.length));
		puzzleReset();		
		
		Log.d("oncre",getResources().getConfiguration().locale.getLanguage());
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(sv.isTouch()){
			timeCount.post(playTimer);
			//startTime = System.currentTimeMillis();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		MenuItem menu1 = (MenuItem)menu.findItem(R.id.menu_mode);
		if(slideMode)
			menu1.setTitle(R.string.menu_mode_slide);
		else
			menu1.setTitle(R.string.menu_mode_touch);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getItemId() == R.id.menu_settings){
			Intent intent = new Intent(this,GalleryActivity.class);
			startActivityForResult(intent, REQUEST_GALLERY);
		}
		if(item.getItemId() == R.id.menu_mode){
			modeChange();
		}
		return super.onOptionsItemSelected(item);
	}

	private void modeChange(){
		slideMode = !slideMode;
		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putBoolean("mode",slideMode);
		editor.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_GALLERY && resultCode == RESULT_OK){
			int index = data.getIntExtra("index", -1);
			sv.setIndexImage(index);
		}
		super.onActivityResult(requestCode,resultCode,data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		puzzleStop();
	}

	@Override
	public void finish()
	{
		FinishDialog dialog = new FinishDialog();
		dialog.setDialogListener(this);
		dialog.show(getSupportFragmentManager(),"finish");
	}

	private void writePref(){
		String id = sv.getCreditId();
		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putLong(id, spentTime);//clearTime
		editor.commit();
		Log.d("newrecord",""+spentTime);//clearTime
	}

	private long readPref(){
		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		String id = sv.getCreditId();
		return pref.getLong(id, -1);
	}
	private boolean readTouchModePref(){
		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		return pref.getBoolean("mode", false);
	}

	private void setClearTime(){
		long clearTime = readPref();
		if(clearTime!=-1){
			long millsec = clearTime;
			millsec /= 100;
			playTime.setText(sdf.format(clearTime)+"."+millsec%10);
		}else
			playTime.setText(R.string.no_record);
	}
	private void creditText(){
		String credit = sv.getCredit();
		((TextView)findViewById(R.id.textView1)).setText(Html.fromHtml(credit));
	}

	private void getDispSize(){
		Display disp = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		try {
			// test for new method to trigger exception
			Class<?> pointClass = Class.forName("android.graphics.Point");
			Method newGetSize = Display.class.getMethod("getSize", new Class[]{ pointClass });

			// no exception, so new method is available, just use it
			newGetSize.invoke(disp, size);
			dispW = size.x;
			dispH = size.x;
		} catch(NoSuchMethodException ex) {
			// new method is not available, use the old ones
			dispW = disp.getWidth();
			dispH = disp.getWidth();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void puzzleStart() {
		spentTime = 0;
		//startTime = System.currentTimeMillis();
		timeCount.post(playTimer);
	}

	@Override
	public void puzzleClear() {
		//clearTime = System.currentTimeMillis()-startTime;
		long oldClearTime = readPref();
		if(oldClearTime == -1){
			playTime.setBackgroundResource(R.drawable.newrec);
			writePref();
		}else if(spentTime < oldClearTime){//clearTime
			playTime.setBackgroundResource(R.drawable.newrec);
			writePref();
		}
		puzzleStop();
		creditText();
	}

	public void puzzleStop() {
		timeCount.removeCallbacks(playTimer);
	}

	@Override
	public void puzzleReset() {
		((TextView)findViewById(R.id.textView1)).setText("");
		timeCount.removeCallbacks(playTimer);
		playTime.setBackgroundResource(0);
		indexText.setText(String.format(getString(R.string.index_num), sv.imgIndex+1,sv.imgIds.length));
		setClearTime();
		spentTime = -1;//clearTime
	}

	private Runnable playTimer = new Runnable() {

		@Override
		public void run() {
			//spentTime = System.currentTimeMillis()-startTime;
			spentTime +=100;
			
			long millsec = spentTime;
			millsec /= 100;
			playTime.setText(sdf.format(spentTime)+"."+millsec%10);
			timeCount.postDelayed(playTimer, 100);
		}
	};

	@Override
	public void onPositiveClick(int code, Object... objects) {
		if(code == DialogListener.FINISH_DIALOG){
			super.finish();
		}
	}

	@Override
	public void onNegativeClick(int code) {
	}

	@Override
	public void dialogCancel(int code) {
		if(code == DialogListener.FINISH_DIALOG){
			super.finish();
		}		
	}
}
