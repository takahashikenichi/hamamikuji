/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved.
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

package jp.codedesign.android.jagy_omikuji.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import jp.codedesign.android.jagy_omikuji.MainActivity;
import jp.codedesign.android.jagy_omikuji.R;

/**
 * A simple fragment that shows a button to reset the counter
 */
public class SettingsFragment extends Fragment {
    private TextView mCounterText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_layout, container, false);
        mCounterText = (TextView) view.findViewById(R.id.counter);
        Button button = (Button) view.findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).resetCounter();
            }
        });
        return view;
    }

    public void setCounter(String text) {
        mCounterText.setText(text);
    }

    public void setCounter(int i) {
        setCounter((i < 0 ? "0" : String.valueOf(i)) + getString(R.string.count_text));
    }
}
