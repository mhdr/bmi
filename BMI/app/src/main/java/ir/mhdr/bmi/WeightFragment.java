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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ir.mhdr.bmi.lib.FirebaseUtils;
import ir.mhdr.bmi.lib.Statics;

public class WeightFragment extends DialogFragment {

    NumberPicker numberPickerWeight1;
    NumberPicker numberPickerWeight2;
    AppCompatButton buttonSaveWeight;
    AppCompatButton buttonCancelWeight;

    OnSaveListener onSaveListener;
    double initialWeightValue = -1;
    View.OnClickListener buttonSaveWeight_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int num1 = numberPickerWeight1.getValue();
            int num2 = numberPickerWeight2.getValue();

            double temp1 = num2 / 10f;
            double temp2 = Double.parseDouble(String.format(Locale.US, "%.1f", temp1));
            double value = num1 + temp2;

            if (onSaveListener != null) {
                onSaveListener.onSave(value);
            }

            dismiss();
        }
    };
    private FirebaseAnalytics mFirebaseAnalytics;


    public WeightFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weight, container, false);

        if (FirebaseUtils.checkPlayServices(getContext())) {

            // Obtain the FirebaseAnalytics instance.
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
            mFirebaseAnalytics.setCurrentScreen(this.getActivity(), "WeightFragment", this.getClass().getSimpleName());
            mFirebaseAnalytics.setUserProperty(FirebaseUtils.UserProperty.InstallSource, Statics.InstallSource);
        }

        numberPickerWeight1 = (NumberPicker) view.findViewById(R.id.numberPickerWeight1);
        numberPickerWeight2 = (NumberPicker) view.findViewById(R.id.numberPickerWeight2);
        buttonSaveWeight = (AppCompatButton) view.findViewById(R.id.buttonSaveWeight);
        buttonCancelWeight = (AppCompatButton) view.findViewById(R.id.buttonCancelWeight);


        Map<Integer, Integer> extractedValue = extractWeigtValue();
        Integer num1 = extractedValue.get(1);
        Integer num2 = extractedValue.get(2);

        numberPickerWeight1.setMaxValue(999);
        numberPickerWeight1.setMinValue(0);
        if (initialWeightValue == -1f) {
            numberPickerWeight1.setValue(70);
        } else {
            numberPickerWeight1.setValue(num1);
        }
        numberPickerWeight1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        numberPickerWeight2.setMaxValue(9);
        numberPickerWeight2.setMinValue(0);
        if (initialWeightValue == -1f) {
            numberPickerWeight2.setValue(0);
        } else {
            numberPickerWeight2.setValue(num2);
        }
        numberPickerWeight2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        buttonSaveWeight.setOnClickListener(buttonSaveWeight_OnClickListener);

        buttonCancelWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    public void setWeightValue(Double value) {
        this.initialWeightValue = value;
    }

    private Map<Integer, Integer> extractWeigtValue() {
        Map<Integer, Integer> result = new HashMap<>();

        Integer num1;
        Integer num2;

        int temp1 = (int) initialWeightValue;
        num1 = temp1;

        int temp2 = (int) ((initialWeightValue - temp1) * 10);
        num2 = temp2;

        result.put(1, num1);
        result.put(2, num2);

        return result;
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
        void onSave(double value);
    }
}
