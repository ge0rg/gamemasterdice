package de.duenndns.gmdice;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

// Work around android NumberPicker freaking out on negative values
// https://stackoverflow.com/a/49499143/539443
public class SignedNumberPicker extends NumberPicker {
	int nToAdd=0,mMaxValue=0,mMinValue=0;
	EditText tv;
	private void init() {
		super.setFormatter(new Formatter() {
			@Override
			public String format(int i) {
				int r=i-nToAdd;
				return String.valueOf(r);
			}
		});
		tv.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		tv.setFilters(new InputFilter[] {new InputTextFilter()});
		tv.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				if (b) {
					tv.selectAll();
				} else {
					setValueFromEditText(tv);
				}
				if (mOnTextFocusChangedListener!=null) {
					mOnTextFocusChangedListener.onTextFocusChanged(b);
				}
			}
		});
	}
	OnTextFocusChangedListener mOnTextFocusChangedListener =null;
	public interface OnTextFocusChangedListener {
		void onTextFocusChanged(boolean textViewHasFocus);
	}
	public void setOnTextFocusChangedListener(OnTextFocusChangedListener l) {
		mOnTextFocusChangedListener =l;
	}
	public SignedNumberPicker(Context context) {
		super(context);
		init();
	}
	public SignedNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SignedNumberPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	public void addView(View child) {
		super.addView(child);
		init(child);
	}
	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		init(child);
	}
	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, params);
		init(child);
	}
	private void init(View view) {
		if(view instanceof EditText){
			tv=((EditText) view);
		}
	}

	@Override
	public void setMinValue(int minValue) {
		if (minValue<0) {
			nToAdd=-minValue;
			super.setMaxValue(super.getMaxValue()+nToAdd);
		}
		mMinValue=minValue;
		super.setMinValue(minValue+nToAdd);
	}

	@Override
	public void setMaxValue(int maxValue) {
		if (maxValue+nToAdd<0) {
			nToAdd=-maxValue;
		}
		mMaxValue=maxValue;
		super.setMaxValue(maxValue+nToAdd);
	}

	@Override
	public void setValue(int value) {
		super.setValue(value+nToAdd);
	}
	private void setValueFromEditText(EditText tv) {
		String str = String.valueOf(tv.getText());
		if (TextUtils.isEmpty(str.replace("-",""))) {
			tv.setText(String.valueOf(SignedNumberPicker.super.getValue()-nToAdd));
		} else {
			try {
				SignedNumberPicker.super.setValue(Integer.valueOf(tv.getText().toString())+nToAdd);
			} catch (Exception e) {}
		}
	}
	public int getRealValue() {
		if (tv.hasFocus())
			setValueFromEditText(tv);
		return super.getValue()-nToAdd;
	}
	private NumberPicker.OnValueChangeListener mOnValueChangedListener=null;
	@Override
	public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
		mOnValueChangedListener=onValueChangedListener;
		super.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				mOnValueChangedListener.onValueChange(picker,oldVal-nToAdd,newVal-nToAdd);
			}
		});
	}

	//copied from NumberPicker source & modified
	//https://github.com/aosp-mirror/platform_frameworks_base/blob/master/core/java/android/widget/NumberPicker.java
	class InputTextFilter extends NumberKeyListener {

		// XXX This doesn't allow for range limits when controlled by a
		// soft input method!
		public int getInputType() {
			return InputType.TYPE_CLASS_TEXT;
		}

		@Override
		protected char[] getAcceptedChars() {
			return DIGIT_CHARACTERS;
		}

		@Override
		public CharSequence filter(
				CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			// We don't know what the output will be, so always cancel any
			// pending set selection command.
			/*if (mSetSelectionCommand != null) {
			  mSetSelectionCommand.cancel();
			  }*/
			//and we will ignore this

			CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
			if (filtered == null) {
				filtered = source.subSequence(start, end);
			}

			String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
				+ dest.subSequence(dend, dest.length());

			if ("".equals(result)) {
				return result;
			}
			int val = getSelectedPos(result);

			/*
			 * Ensure the user can't type in a value greater than the max
			 * allowed. We have to allow less than min as the user might
			 * want to delete some numbers and then type a new number.
			 * And prevent multiple-"0" that exceeds the length of upper
			 * bound number.
			 */
			if (val > mMaxValue || (result.length() > String.valueOf(mMaxValue).length() && result
						.length()>String.valueOf(mMinValue).length()) || (val<0 && val<mMinValue)) {
				return "";
			} else {
				return filtered;
			}
				}
		private int getSelectedPos(String value) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// Ignore as if it's not a number we don't care
			}
			return mMinValue;
		}
		private final char[] DIGIT_CHARACTERS = new char[] {
			//THE MINUS SIGN
			'-',
				// Latin digits are the common case
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				// Arabic-Indic
				'\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665', '\u0666', '\u0667', '\u0668'
					, '\u0669',
				// Extended Arabic-Indic
				'\u06f0', '\u06f1', '\u06f2', '\u06f3', '\u06f4', '\u06f5', '\u06f6', '\u06f7', '\u06f8'
					, '\u06f9',
				// Hindi and Marathi (Devanagari script)
				'\u0966', '\u0967', '\u0968', '\u0969', '\u096a', '\u096b', '\u096c', '\u096d', '\u096e'
					, '\u096f',
				// Bengali
				'\u09e6', '\u09e7', '\u09e8', '\u09e9', '\u09ea', '\u09eb', '\u09ec', '\u09ed', '\u09ee'
					, '\u09ef',
				// Kannada
				'\u0ce6', '\u0ce7', '\u0ce8', '\u0ce9', '\u0cea', '\u0ceb', '\u0cec', '\u0ced', '\u0cee'
					, '\u0cef'
		};
	}
}
