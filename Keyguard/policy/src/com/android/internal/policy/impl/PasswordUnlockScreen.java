/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.policy.impl;

import java.util.List;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.security.KeyStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.internal.R;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.PasswordEntryKeyboardHelper;
import com.android.internal.widget.PasswordEntryKeyboardView;

/**
 * Displays a dialer-like interface or alphanumeric (latin-1) key entry for the user to enter
 * an unlock password
 */
public class PasswordUnlockScreen extends LinearLayout implements KeyguardScreen,
OnEditorActionListener {
	private View visibleScreen;
	private static final String TAG = "PasswordUnlockScreen";
	private final KeyguardUpdateMonitor mUpdateMonitor;
	private final KeyguardScreenCallback mCallback;

	private final boolean mIsAlpha;

	private final EditText mPasswordEntry;
	private final LockPatternUtils mLockPatternUtils;
	private final PasswordEntryKeyboardView mKeyboardView;
	private final PasswordEntryKeyboardHelper mKeyboardHelper;

	private final int mCreationOrientation;
	private final int mCreationHardKeyboardHidden;

	private final KeyguardStatusViewManager mStatusViewManager;
	private final boolean mUseSystemIME = true; // TODO: Make configurable
	private boolean mResuming; // used to prevent poking the wakelock during onResume()

	// To avoid accidental lockout due to events while the device in in the pocket, ignore
	// any passwords with length less than or equal to this length.
	private static final int MINIMUM_PASSWORD_LENGTH_BEFORE_REPORT = 3;
	private EditText addrEt;
	private EditText timeEt;
	private Button b;
	private TextView info;
	private LinearLayout editLayout;
	public PasswordUnlockScreen(Context context, Configuration configuration,
			LockPatternUtils lockPatternUtils, KeyguardUpdateMonitor updateMonitor,
			KeyguardScreenCallback callback) {
		super(context);

		mCreationHardKeyboardHidden = configuration.hardKeyboardHidden;
		mCreationOrientation = configuration.orientation;
		mUpdateMonitor = updateMonitor;
		mCallback = callback;
		mLockPatternUtils = lockPatternUtils;

		LayoutInflater layoutInflater = LayoutInflater.from(context);
		if (mCreationOrientation != Configuration.ORIENTATION_LANDSCAPE) {
			visibleScreen = layoutInflater.inflate(R.layout.keyguard_screen_password_portrait, this, true);
		} else {
			layoutInflater.inflate(R.layout.keyguard_screen_password_landscape, this, true);
		}

		LinearLayout layout = (LinearLayout)findViewById(R.id.inputAddr);
		editLayout = (LinearLayout)findViewById(R.id.editLayout);
		addrEt = new EditText(mContext);
		timeEt = new EditText(mContext);
		b = new Button(mContext);

		mPasswordEntry = new EditText(mContext);
		mPasswordEntry.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		mPasswordEntry.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		mPasswordEntry.setEms(10);
		if(KeyguardViewMediator.SECUREMODE != 3){
			//    LinearLayout layout = (LinearLayout)findViewById(R.id.infoPasswordLayout);
			info = (TextView)findViewById(R.id.Password_info);
			info.setVisibility(View.VISIBLE);
			if (KeyguardViewMediator.SECUREMODE == 0) {
				info.setText("Please attach your USB token");
				info.setEnabled(true);
			} else if (KeyguardViewMediator.SECUREMODE == 1) {
				info.setText("Decrypting now...");
				info.setEnabled(false);
			} else if (KeyguardViewMediator.SECUREMODE == 2) {
				info.setText("Try Again");
				info.setEnabled(true);
			}
			info.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AddressDialog dialog= new AddressDialog(mContext);

					dialog.show();

					dialog.addrEt.addTextChangedListener(new TextWatcher() {

						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
							// TODO Auto-generated method stub

						}

						@Override
						public void beforeTextChanged(CharSequence s, int start, int count,
								int after) {
							// TODO Auto-generated method stub

						}

						@Override
						public void afterTextChanged(Editable s) {
							// TODO Auto-generated method stub
							if (!mResuming) {
								mCallback.pokeWakelock();
							}
						}
					});

					dialog.timeEt.addTextChangedListener(new TextWatcher() {

						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
							// TODO Auto-generated method stub

						}

						@Override
						public void beforeTextChanged(CharSequence s, int start, int count,
								int after) {
							// TODO Auto-generated method stub

						}

						@Override
						public void afterTextChanged(Editable s) {
							// TODO Auto-generated method stub
							if (!mResuming) {
								mCallback.pokeWakelock();
							}
						}
					});
				}
			});
		}
			mKeyboardView = (PasswordEntryKeyboardView) findViewById(R.id.keyboard);
			mKeyboardHelper = new PasswordEntryKeyboardHelper(context, mKeyboardView, this, false);
			mStatusViewManager = new KeyguardStatusViewManager(this, mUpdateMonitor, mLockPatternUtils,
					mCallback, true);

			final int quality = lockPatternUtils.getKeyguardStoredPasswordQuality();
			mIsAlpha = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC == quality
					|| DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC == quality
					|| DevicePolicyManager.PASSWORD_QUALITY_COMPLEX == quality;

			mPasswordEntry.setOnEditorActionListener(this);


			mKeyboardHelper.setEnableHaptics(mLockPatternUtils.isTactileFeedbackEnabled());
			boolean imeOrDeleteButtonVisible = false;
			if (mIsAlpha) {
				// We always use the system IME for alpha keyboard, so hide lockscreen's soft keyboard
				mKeyboardHelper.setKeyboardMode(PasswordEntryKeyboardHelper.KEYBOARD_MODE_ALPHA);
				mKeyboardView.setVisibility(View.GONE);
			} else {
				// Use lockscreen's numeric keyboard if the physical keyboard isn't showing
				mKeyboardHelper.setKeyboardMode(PasswordEntryKeyboardHelper.KEYBOARD_MODE_NUMERIC);
				mKeyboardView.setVisibility(mCreationHardKeyboardHidden
						== Configuration.HARDKEYBOARDHIDDEN_NO ? View.INVISIBLE : View.VISIBLE);

				// The delete button is of the PIN keyboard itself in some (e.g. tablet) layouts,
				// not a separate view
				View pinDelete = findViewById(R.id.pinDel);
				if (pinDelete != null) {
					pinDelete.setVisibility(View.VISIBLE);
					imeOrDeleteButtonVisible = true;
					pinDelete.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							mKeyboardHelper.handleBackspace();
						}
					});
				}
			}

			mPasswordEntry.requestFocus();

			// This allows keyboards with overlapping qwerty/numeric keys to choose just numeric keys.
			if (mIsAlpha) {
				mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
				mPasswordEntry.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
				//mStatusViewManager.setHelpMessage(R.string.keyguard_password_enter_password_code,
				//KeyguardStatusViewManager.LOCK_ICON);
			} else {
				mPasswordEntry.setKeyListener(DigitsKeyListener.getInstance());
				mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER
						| InputType.TYPE_NUMBER_VARIATION_PASSWORD);
				//mStatusViewManager.setHelpMessage(R.string.keyguard_password_enter_pin_code,
				//KeyguardStatusViewManager.LOCK_ICON);
			}

			// Poke the wakelock any time the text is selected or modified
			mPasswordEntry.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mCallback.pokeWakelock();
				}
			});
			mPasswordEntry.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				public void afterTextChanged(Editable s) {
					if (!mResuming) {
						mCallback.pokeWakelock();
					}
				}
			});

			// If there's more than one IME, enable the IME switcher button
			View switchImeButton = findViewById(R.id.switch_ime_button);
			final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
					Context.INPUT_METHOD_SERVICE);
			if (mIsAlpha && switchImeButton != null && hasMultipleEnabledIMEsOrSubtypes(imm, false)) {
				switchImeButton.setVisibility(View.VISIBLE);
				imeOrDeleteButtonVisible = true;
				switchImeButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						mCallback.pokeWakelock(); // Leave the screen on a bit longer
						imm.showInputMethodPicker();
					}
				});
			}

			// If no icon is visible, reset the left margin on the password field so the text is
			// still centered.
			if (!imeOrDeleteButtonVisible) {
				android.view.ViewGroup.LayoutParams params = mPasswordEntry.getLayoutParams();
				if (params instanceof MarginLayoutParams) {
					((MarginLayoutParams)params).leftMargin = 0;
					mPasswordEntry.setLayoutParams(params);
				}
			}
		
	}

	/**
	 * Method adapted from com.android.inputmethod.latin.Utils
	 *
	 * @param imm The input method manager
	 * @param shouldIncludeAuxiliarySubtypes
	 * @return true if we have multiple IMEs to choose from
	 */
	private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager imm,
			final boolean shouldIncludeAuxiliarySubtypes) {
		final List<InputMethodInfo> enabledImis = imm.getEnabledInputMethodList();

		// Number of the filtered IMEs
		int filteredImisCount = 0;

		for (InputMethodInfo imi : enabledImis) {
			// We can return true immediately after we find two or more filtered IMEs.
			if (filteredImisCount > 1) return true;
			final List<InputMethodSubtype> subtypes =
					imm.getEnabledInputMethodSubtypeList(imi, true);
			// IMEs that have no subtypes should be counted.
			if (subtypes.isEmpty()) {
				++filteredImisCount;
				continue;
			}

			int auxCount = 0;
			for (InputMethodSubtype subtype : subtypes) {
				if (subtype.isAuxiliary()) {
					++auxCount;
				}
			}
			final int nonAuxCount = subtypes.size() - auxCount;

			// IMEs that have one or more non-auxiliary subtypes should be counted.
			// If shouldIncludeAuxiliarySubtypes is true, IMEs that have two or more auxiliary
			// subtypes should be counted as well.
			if (nonAuxCount > 0 || (shouldIncludeAuxiliarySubtypes && auxCount > 1)) {
				++filteredImisCount;
				continue;
			}
		}

		return filteredImisCount > 1
				// imm.getEnabledInputMethodSubtypeList(null, false) will return the current IME's enabled
				// input method subtype (The current IME should be LatinIME.)
				|| imm.getEnabledInputMethodSubtypeList(null, false).size() > 1;
	}

	@Override
	protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
		// send focus to the password field
		return mPasswordEntry.requestFocus(direction, previouslyFocusedRect);
	}

	/** {@inheritDoc} */
	public boolean needsInput() {
		return mUseSystemIME && mIsAlpha;
	}

	/** {@inheritDoc} */
	public void onPause() {
		mStatusViewManager.onPause();
	}

	/** {@inheritDoc} */
	public void onResume() {
		mResuming = true;
		// reset status
		mStatusViewManager.onResume();

		// start fresh
		if(false){
			mPasswordEntry.setText("");
			mPasswordEntry.requestFocus();
		}
		// if the user is currently locked out, enforce it.
		long deadline = mLockPatternUtils.getLockoutAttemptDeadline();
		if (deadline != 0) {
			handleAttemptLockout(deadline);
		}

		if(KeyguardViewMediator.SECUREMODE == 3){

			/*if(mIsAlpha){
        		if(editLayout.getChildCount() == 1){
        			editLayout.addView(mPasswordEntry);
        		}
        	}else{
        		if(editLayout.getChildCount() == 2){
        			editLayout.addView(mPasswordEntry);
        		}
        	}*/
			if(editLayout.getChildCount() == 2){
				editLayout.addView(mPasswordEntry);
			}
			//editLayout.addView(mPasswordEntry);
			findViewById(R.id.Password_info).setVisibility(INVISIBLE);

		} else {
			info.setVisibility(VISIBLE);
			if(editLayout.getChildCount() == 3){
				editLayout.removeView(mPasswordEntry);
			}
			if (KeyguardViewMediator.SECUREMODE == 0) {
				info.setText("Please attach your USB token");
				info.setEnabled(true);
			} else if (KeyguardViewMediator.SECUREMODE == 1) {
				info.setText("Decrypting now...");
				info.setEnabled(false);
			} else if (KeyguardViewMediator.SECUREMODE == 2) {
				info.setText("Try Again");
				info.setEnabled(true);
			}
		}
		//        info.setVisibility(View.INVISIBLE);
		//   //     b.setVisibility(View.INVISIBLE);
		//        if(!LockPatternKeyguardView.SecureMode){
		//        	info.setVisibility(View.VISIBLE);
		//   //     	b.setVisibility(View.VISIBLE);
		//        	 mPasswordEntry.setVisibility(View.INVISIBLE);
		//        	 mKeyboardView.setVisibility(View.GONE);
		//        	 View pinDelete = findViewById(R.id.pinDel);
		//             if (pinDelete != null) {
		//                 pinDelete.setVisibility(View.INVISIBLE);
		//             }
		//             visibleScreen.findViewById(R.id.lockPattern).setVisibility(INVISIBLE);
		//    		 	
		//    	        
		//    	         
		//        	
		//        }else{
		//        	mPasswordEntry.setVisibility(View.VISIBLE);
		//        	if(mIsAlpha){
		//        		
		//        	}else{
		//        		mKeyboardView.setVisibility(mCreationHardKeyboardHidden
		//                        == Configuration.HARDKEYBOARDHIDDEN_NO ? View.INVISIBLE : View.VISIBLE);
		//        		View pinDelete = findViewById(R.id.pinDel);
		//        		if (pinDelete != null) {
		//                    pinDelete.setVisibility(View.VISIBLE);
		//                }
		//        	}
		//        }
		mResuming = false;
	}

	/** {@inheritDoc} */
	public void cleanUp() {
		mUpdateMonitor.removeCallback(this);
	}

	private void verifyPasswordAndUnlock() {
		String entry = mPasswordEntry.getText().toString();
		if (mLockPatternUtils.checkPassword(entry)) {
			mCallback.keyguardDone(true);
			mCallback.reportSuccessfulUnlockAttempt();
			mStatusViewManager.setInstructionText(null);
			KeyStore.getInstance().password(entry);
		} else if (entry.length() > MINIMUM_PASSWORD_LENGTH_BEFORE_REPORT ) {
			// to avoid accidental lockout, only count attempts that are long enough to be a
			// real password. This may require some tweaking.
			mCallback.reportFailedUnlockAttempt();
			if (0 == (mUpdateMonitor.getFailedAttempts()
					% LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT)) {
				long deadline = mLockPatternUtils.setLockoutAttemptDeadline();
				handleAttemptLockout(deadline);
			}
			mStatusViewManager.setInstructionText(
					mContext.getString(R.string.lockscreen_password_wrong));
		} else if (entry.length() > 0) {
			mStatusViewManager.setInstructionText(
					mContext.getString(R.string.lockscreen_password_wrong));
		}
		mPasswordEntry.setText("");
	}

	// Prevent user from using the PIN/Password entry until scheduled deadline.
	private void handleAttemptLockout(long elapsedRealtimeDeadline) {
		mPasswordEntry.setEnabled(false);
		mKeyboardView.setEnabled(false);
		long elapsedRealtime = SystemClock.elapsedRealtime();
		new CountDownTimer(elapsedRealtimeDeadline - elapsedRealtime, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				int secondsRemaining = (int) (millisUntilFinished / 1000);
				String instructions = getContext().getString(
						R.string.lockscreen_too_many_failed_attempts_countdown,
						secondsRemaining);
				mStatusViewManager.setInstructionText(instructions);
			}

			@Override
			public void onFinish() {
				mPasswordEntry.setEnabled(true);
				mKeyboardView.setEnabled(true);
				mStatusViewManager.resetStatusInfo();
			}
		}.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		mCallback.pokeWakelock();
		return false;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Configuration config = getResources().getConfiguration();
		if (config.orientation != mCreationOrientation
				|| config.hardKeyboardHidden != mCreationHardKeyboardHidden) {
			mCallback.recreateMe(config);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation != mCreationOrientation
				|| newConfig.hardKeyboardHidden != mCreationHardKeyboardHidden) {
			mCallback.recreateMe(newConfig);
		}
	}

	public void onKeyboardChange(boolean isKeyboardOpen) {
		// Don't show the soft keyboard when the real keyboard is open
		mKeyboardView.setVisibility(isKeyboardOpen ? View.INVISIBLE : View.VISIBLE);
	}

	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		// Check if this was the result of hitting the enter key
		if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE
				|| actionId == EditorInfo.IME_ACTION_NEXT) {
			verifyPasswordAndUnlock();
			return true;
		}
		return false;
	}
}
