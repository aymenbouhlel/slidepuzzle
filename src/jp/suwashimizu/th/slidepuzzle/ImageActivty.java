package jp.suwashimizu.th.slidepuzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageActivty extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fullsizeimage);
		final int index = getIntent().getIntExtra("index", -1);
		
		String idStr = getResources().getStringArray(R.array.imgIds)[index];
		int resId = getResources().getIdentifier(idStr, "drawable", getPackageName());
		
		String credit = getResources().getStringArray(R.array.author)[index].split(",")[1];
		
		final TextView tv = (TextView)findViewById(R.id.textView1);
		tv.setVisibility(View.INVISIBLE);
		tv.setText(Html.fromHtml(credit));
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		
		final Button btn = (Button)findViewById(R.id.button1);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		btn.setVisibility(View.INVISIBLE);
		
		final Button btnPuzzle = (Button)findViewById(R.id.button2);
		btnPuzzle.setVisibility(View.INVISIBLE);
		btnPuzzle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("index", index);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		ImageView iv = (ImageView)findViewById(R.id.imageView1);
		iv.setImageResource(resId);
		iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(btn.getVisibility() == View.INVISIBLE){
					btn.setVisibility(View.VISIBLE);
					tv.setVisibility(View.VISIBLE);
					btnPuzzle.setVisibility(View.VISIBLE);
				}else{
					btn.setVisibility(View.INVISIBLE);
					tv.setVisibility(View.INVISIBLE);
					btnPuzzle.setVisibility(View.INVISIBLE);
				}
			}
		});			
	}
}
