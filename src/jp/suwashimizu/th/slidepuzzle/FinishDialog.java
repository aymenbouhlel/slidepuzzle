package jp.suwashimizu.th.slidepuzzle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class FinishDialog extends DialogFragment{

	private DialogListener listener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.app_name).setMessage(R.string.app_finish)
		.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onPositiveClick(DialogListener.FINISH_DIALOG);
			}
		}).setNegativeButton(R.string.no, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onNegativeClick(DialogListener.FINISH_DIALOG);
			}
		});
		return builder.create();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		listener.dialogCancel(DialogListener.FINISH_DIALOG);
	}

	public void setDialogListener(DialogListener listener){
		this.listener = listener;
	}
	public void removeDialogListener(){
		listener = null;
	}

}
