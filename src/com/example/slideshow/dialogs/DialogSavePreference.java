package com.example.slideshow.dialogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.example.slideshow.R;
import com.example.slideshow.app.Constants;

@SuppressLint("ValidFragment")
public class DialogSavePreference extends DialogFragment {
	String contentsOfFile;

	EditText edtNameFile;

	public DialogSavePreference() {
		// TODO Auto-generated constructor stub
	}

	public DialogSavePreference(String content) {
		contentsOfFile = content;
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final FragmentActivity activity = getActivity();
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_save_preference);

		edtNameFile = (EditText) dialog.findViewById(R.id.edt_file_name);
		// edtNameFile.setText(contentsOfFile);

		dialog.findViewById(R.id.but_pref_save_dialog_ok).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Editable text = edtNameFile.getText();
						if (TextUtils.isEmpty(text)) {
							Toast.makeText(activity, "File name is empty",
									Toast.LENGTH_SHORT).show();
							return;
						}
						writeToFile(contentsOfFile, text.toString() + ".ert",
								Constants.PATH_FOR_FILES);

						dismiss();

					}
				});

		dialog.findViewById(R.id.but_save_dialog_cansel).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						dismiss();

					}
				});

		return dialog;
	}

	private void writeToFile(String data, String fileName, String path) {

		File pFile = new File(path);
		if (!pFile.exists()) {
			pFile.mkdir();
		}

		try {
			File myFile = new File(path, fileName);
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(data);
			myOutWriter.close();
			fOut.close();
			Toast.makeText(getActivity().getBaseContext(), "Done writing SD ",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

	}

}
