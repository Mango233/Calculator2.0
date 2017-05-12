/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.jikexueyuan.jikecalculator;

import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

class EventListener implements View.OnKeyListener,
                               View.OnClickListener,
                               View.OnLongClickListener {
    Logic mHandler;
    ViewPager mPager;
    //使用一个全局变量SoundManager类型的mSoundManager.
    SoundManager mSoundManager;

    void setHandler(Logic handler, ViewPager pager) {
        mHandler = handler;
        mPager = pager;
    }

    @Override
    public void onClick(View view) {
        //在onClick方法中，可以获取View的文本，我们需要播放文本的音频。
        //单个按键的文本直接播放，所以最开始调用playSound（）方法。
        mSoundManager = SoundManager.getInstance();
        //Play sound
        mSoundManager.playSound(((Button)view).getText().toString());
        int id = view.getId();
        switch (id) {
        case R.id.del:
            mHandler.onDelete();
            break;

        case R.id.clear:
            mHandler.onClear();
            break;

        case R.id.equal:
            mHandler.onEnter();
            //等于号点击之后，我们需要获取结果显示框的文本。然后调用playSeqSound()方法，逐一播放。
            //这里Logic类的getText()是private的，我们将它修改成public的，这样我们才可以在这里访问。
            String result = mHandler.getText();
            String[] soundsToPlay = new String[result.length()];
            for(int i = 0 ; i < soundsToPlay.length ; i ++ ){
                soundsToPlay[i] = String.valueOf(result.charAt(i));
            }
            mSoundManager.playSeqSounds(soundsToPlay);
            break;

        default:
            if (view instanceof Button) {
                String text = ((Button) view).getText().toString();
                if (text.length() >= 2) {
                    // add paren after sin, cos, ln, etc. from buttons
                    text += '(';
                }
                mHandler.insert(text);
                if (mPager != null && mPager.getCurrentItem() == Calculator.ADVANCED_PANEL) {
                    mPager.setCurrentItem(Calculator.BASIC_PANEL);
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        if (id == R.id.del) {
            mHandler.onClear();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        int action = keyEvent.getAction();

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
            keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            boolean eat = mHandler.eatHorizontalMove(keyCode == KeyEvent.KEYCODE_DPAD_LEFT);
            return eat;
        }

        //Work-around for spurious key event from IME, bug #1639445
        if (action == KeyEvent.ACTION_MULTIPLE && keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            return true; // eat it
        }

        //Calculator.log("KEY " + keyCode + "; " + action);

        if (keyEvent.getUnicodeChar() == '=') {
            if (action == KeyEvent.ACTION_UP) {
                mHandler.onEnter();
            }
            return true;
        }

        if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER &&
            keyCode != KeyEvent.KEYCODE_DPAD_UP &&
            keyCode != KeyEvent.KEYCODE_DPAD_DOWN &&
            keyCode != KeyEvent.KEYCODE_ENTER) {
            if (keyEvent.isPrintingKey() && action == KeyEvent.ACTION_UP) {
                // Tell the handler that text was updated.
                mHandler.onTextChanged();
            }
            return false;
        }

        /*
           We should act on KeyEvent.ACTION_DOWN, but strangely
           sometimes the DOWN event isn't received, only the UP.
           So the workaround is to act on UP...
           http://b/issue?id=1022478
         */

        if (action == KeyEvent.ACTION_UP) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mHandler.onEnter();
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                mHandler.onUp();
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                mHandler.onDown();
                break;
            }
        }
        return true;
    }
}
