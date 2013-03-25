package jp.suwashimizu.th.slidepuzzle;

import java.util.EventListener;

public interface DialogListener extends EventListener{
	
	public static final int FINISH_DIALOG = 1;
	public static final int PASSWORD_DIALOG = 2;
	public static final int PASSCHECK_DIALOG = 3;
	
	public void onPositiveClick(int code,Object... objects);
	public void onNegativeClick(int code);
	public void dialogCancel(int code);
}
