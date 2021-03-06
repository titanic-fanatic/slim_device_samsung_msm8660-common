/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.cyanogenmod.settings.device;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.util.Log;

public class InputBoostTimeout extends DialogPreference implements android.widget.Button.OnClickListener {

    private static final String TAG = "GalaxyS2Parts_InputBoostTimeout";
    private static final String FILE_INPUT_BOOST_MS = "/sys/module/cpu_input_boost/parameters/input_boost_ms";
    
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 3000;
    private static final int DEFAULT_VALUE = 1500;
    
    private NumberPicker mPickerTimeout;
    private String mValueTimeout;
    private Context mContext;
    
    public InputBoostTimeout(Context context) {
        this(context, null);
    }

    public InputBoostTimeout(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setPersistent(false);
        mContext = context;
        
        // Set Layout
        setDialogLayoutResource(R.layout.input_boost_timeout_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }
    
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        String timeout = Utils.readValue(FILE_INPUT_BOOST_MS).replace("\n", "");
        setValue(timeout);
    }
    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        
        TextView dialogMessageText = (TextView) view.findViewById(R.id.input_boost_timeout_dialog_message);
        dialogMessageText.setText(getDialogMessage());
        
        Button resetButton = (Button) view.findViewById(R.id.input_boost_timeout_dialog_reset_button);
        resetButton.setOnClickListener(this);

        mPickerTimeout = (NumberPicker) view.findViewById(R.id.NumberPickerTimeout);
        mPickerTimeout.setMinValue(DEFAULT_MIN_VALUE);
        mPickerTimeout.setMaxValue(DEFAULT_MAX_VALUE);
        mPickerTimeout.setValue(Integer.parseInt(mValueTimeout));
        
    }
    
    public String getValue() {
        return mValueTimeout;
    }
    
    public void setValue(String value) {
        if (!value.equals(mValueTimeout)){
            mValueTimeout = value;
            notifyChanged();
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            mPickerTimeout.clearFocus();
            
            int valueTimeout = mPickerTimeout.getValue();
            String sTimeout = valueTimeout + "";
            
            if (callChangeListener(sTimeout)) {
                SharedPreferences sharedPrefs = mContext.getSharedPreferences(DisplaySettings.SHARED_PREFERENCES_BASENAME + "_preferences", 0);
                SharedPreferences.Editor editor = sharedPrefs.edit();
            
                setValue(sTimeout);
                editor.putString(DisplaySettings.KEY_INPUT_BOOST_TIMEOUT, mValueTimeout);
                editor.commit();
                Utils.writeValue(FILE_INPUT_BOOST_MS, mValueTimeout);
                
                Utils.showToast(mContext, mContext.getString(R.string.input_boost_timeout_dialog_success_toast));
            }
        }
    }
    
    @Override
    protected Parcelable onSaveInstanceState() {
        // save the instance state so that it will survive screen orientation changes and other events that may temporarily destroy it
        final Parcelable superState = super.onSaveInstanceState();
 
        // set the state's value with the class member that holds current setting value
        final SavedState myState = new SavedState(superState);
        myState.value = getValue();
 
        return myState;
    }
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // check whether we saved the state in onSaveInstanceState()
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }
 
        // restore the state
        SavedState myState = (SavedState) state;
        setValue(myState.value);
 
        super.onRestoreInstanceState(myState.getSuperState());
    }

    @Override
    public void onClick(View v) {
        setValue(DEFAULT_VALUE + "");
        mPickerTimeout.setValue(DEFAULT_VALUE);
        Utils.showToast(mContext, mContext.getString(R.string.input_boost_timeout_dialog_reset_toast));
    }
    
    private static class SavedState extends BaseSavedState {
        String value;
 
        public SavedState(Parcelable superState)
        {
            super(superState);
        }
 
        public SavedState(Parcel source)
        {
            super(source);
 
            value = source.readString();
        }
 
        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);
 
            dest.writeString(value);
        }
 
        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>()
        {
            @Override
            public SavedState createFromParcel(Parcel in)
            {
                return new SavedState(in);
            }
 
            @Override
            public SavedState[] newArray(int size)
            {
                return new SavedState[size];
            }
        };
    }
}
