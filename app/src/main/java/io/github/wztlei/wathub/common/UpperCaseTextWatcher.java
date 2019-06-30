package io.github.wztlei.wathub.common;

import android.text.Editable;
import android.widget.EditText;

import io.github.wztlei.wathub.utils.SimpleTextWatcher;

public class UpperCaseTextWatcher extends SimpleTextWatcher {

    private final EditText mEditText;

    public UpperCaseTextWatcher(final EditText editText) {
        mEditText = editText;
    }

    @Override
    public void afterTextChanged(final Editable s) {
        for (int i = 0; i < s.length(); ++i) {
            if (Character.isLowerCase(s.charAt(i))) {
                mEditText.setText(s.toString().toUpperCase());
                mEditText.setSelection(s.length());
                break;
            }
        }
    }
}
