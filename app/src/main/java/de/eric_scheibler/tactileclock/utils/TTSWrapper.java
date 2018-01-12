package de.eric_scheibler.tactileclock.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.accessibility.AccessibilityManager;

import com.google.common.base.Splitter;

public class TTSWrapper extends UtteranceProgressListener {

    private static final int MAX_TEXT_LENGTH = 500;
    private static final String DELIMITER = ". ";
    private static final String UTTERANCE_ID_SPEAK = "utteranceidspeak";

    private static TTSWrapper ttsWrapperInstance;
    private Context mContext;
    private AccessibilityManager accessibilityManager;
    private TextToSpeech tts;

    public static TTSWrapper getInstance(Context context) {
        if(ttsWrapperInstance == null){
            ttsWrapperInstance = new TTSWrapper(context.getApplicationContext());
        }
        return ttsWrapperInstance;
    }

    private TTSWrapper(Context context) {
        mContext = context;
        accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        tts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.getDefault());
                    tts.setOnUtteranceProgressListener(TTSWrapper.this);
                } else {
                    tts = null;
                }
            }
        });
    }

    public boolean isInitialized() {
        if (tts != null) {
            return true;
        }
        return false;
    }

    public boolean isSpeaking() {
        if (tts != null) {
            return tts.isSpeaking();
        }
        return false;
    }

    public void speak(String message, boolean talkbackOnly, boolean interrupt) {
        if (interrupt) {
            stop();
        }
        // set language
        if (tts != null) {
            if (! talkbackOnly
                    || (accessibilityManager.isEnabled() && accessibilityManager.isTouchExplorationEnabled())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ttsGreater21(message);
                } else {
                    ttsUnder20(message);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID_SPEAK);
        for (String part : splitText(text)) {
            tts.speak(part, TextToSpeech.QUEUE_ADD, map);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        for (String part : splitText(text)) {
            tts.speak(part, TextToSpeech.QUEUE_ADD, null, UTTERANCE_ID_SPEAK);
        }
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
        }
    }

    /**
     * split text which is longer than MAX_TEXT_LENGTH by DELIMITER
     * if the text String doesn't contain the DELIMITER, split by MAX_TEXT_LENGTH directly
     * return list of substrings
     */

    private List<String> splitText(String text) {
        List<String> chunks;
        try {
            chunks = new ArrayList<String>();
            int startIndex = 0;
            int endIndex = text.lastIndexOf(DELIMITER, MAX_TEXT_LENGTH);

            while (text.substring(startIndex).length() > MAX_TEXT_LENGTH) {
                chunks.add(text.substring(startIndex, endIndex + 1));
                startIndex = endIndex + DELIMITER.length();
                endIndex = text.lastIndexOf(DELIMITER, MAX_TEXT_LENGTH + startIndex);
            }

            // add the last part
            chunks.add(text.substring(startIndex));
        } catch (IndexOutOfBoundsException e) {
            chunks = Splitter.fixedLength(MAX_TEXT_LENGTH).splitToList(text);
        }
        return chunks;
    }


    /**
     * UtteranceProgressListener interface inplementation
     */

    @Override public void onStart(String utteranceId) {
    }

    @Override public void onError(String utteranceId) {
        tts.setLanguage(Locale.getDefault());
    }

    @Override public void onDone(String utteranceId) {
    }

}
