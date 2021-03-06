package ir.mhdr.bmi;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.NumberPicker;

import com.google.firebase.analytics.FirebaseAnalytics;

import ir.mhdr.bmi.lib.FirebaseUtils;
import ir.mhdr.bmi.lib.Statics;

public class HeightFragment extends DialogFragment {

    NumberPicker numberPickerHeight;
    AppCompatButton buttonSaveHeight;
    AppCompatButton buttonCancelHeight;

    OnSaveListener onSaveListener;
    int initialHeightValue = -1;
    View.OnClickListener buttonSaveHeight_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int value = numberPickerHeight.getValue();

            if (onSaveListener != null) {
                onSaveListener.onSave(value);
            }

            dismiss();
        }
    };
    private FirebaseAnalytics mFirebaseAnalytics;


    public HeightFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_height, container, false);

        if (FirebaseUtils.checkPlayServices(getContext())) {

            // Obtain the FirebaseAnalytics instance.
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
            mFirebaseAnalytics.setCurrentScreen(this.getActivity(), "HeightFragment", this.getClass().getSimpleName());
            mFirebaseAnalytics.setUserProperty(FirebaseUtils.UserProperty.InstallSource, Statics.InstallSource);
        }

        numberPickerHeight = (NumberPicker) view.findViewById(R.id.numberPickerHeight);
        buttonSaveHeight = (AppCompatButton) view.findViewById(R.id.buttonSaveHeight);
        buttonCancelHeight= (AppCompatButton) view.findViewById(R.id.buttonCancelHeight);

        numberPickerHeight.setMaxValue(999);
        numberPickerHeight.setMinValue(0);

        if (initialHeightValue == -1) {
            numberPickerHeight.setValue(170);
        } else {
            numberPickerHeight.setValue(initialHeightValue);
        }

        numberPickerHeight.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        buttonSaveHeight.setOnClickListener(buttonSaveHeight_OnClickListener);

        buttonCancelHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    public void setHeightValue(int value) {
        this.initialHeightValue = value;
    }

    @Override
    public void onResume() {
        super.onResume();

        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void setOnSaveListener(OnSaveListener mListener) {
        this.onSaveListener = mListener;
    }

    public interface OnSaveListener {
        void onSave(int value);
    }
}
