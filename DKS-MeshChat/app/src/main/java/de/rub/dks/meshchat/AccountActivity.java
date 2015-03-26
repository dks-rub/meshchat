package de.rub.dks.meshchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Wrapping activity for account management. 
 */
public class AccountActivity extends Activity{
	
	private Button account_btn;
	private EditText nickname;
	private String n;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account);
		
		account_btn = (Button) findViewById(R.id.creat_account_btn);
		
		nickname = (EditText) findViewById(R.id.nickname_textedit);
		nickname.clearFocus();
		nickname.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				account_btn.setEnabled(s.length()>0);
			}
		});
		
		account_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish(nickname.getText().toString());
			}
		});
	}
	
	public void finish(String name){
		//sending back result to calling activity
		Intent data = new Intent();
		if(name != null){
			data.putExtra("nickname",name);
			setResult(Globals.INIT_SUCCESS,data);
		} else {
			setResult(Globals.INIT_FAIL,data);
		}
		super.finish();
	}
}
