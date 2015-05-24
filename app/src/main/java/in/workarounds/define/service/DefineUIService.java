package in.workarounds.define.service;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;

import in.workarounds.define.R;
import in.workarounds.define.model.DictResult;
import in.workarounds.define.model.Dictionary;
import in.workarounds.define.model.WordnetDictionary;
import in.workarounds.define.ui.adapter.DefineCardHandler;
import in.workarounds.define.ui.view.PopupRoot;

/**
 * Created by manidesto on 15/05/15.
 */
public class DefineUIService extends UIService implements PopupRoot.OnCloseDialogsListener, DefineCardHandler.SelectedTextChangedListener{
    public static final String INTENT_EXTRA_CLIPTEXT = "intent_clip_text";
    private DefineCardHandler mCardHandler;
    private String mClipText;
    private Dictionary mDictionary;
    private String mWordForm;
    private boolean mIgnoreIntent = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mDictionary = new WordnetDictionary(this);
        setBubbleView(R.layout.layout_test_bubble);
        setCardView(R.layout.card_define_service);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!mIgnoreIntent){
            goToState(STATE_BUBBLE);
            mClipText = intent.getStringExtra(INTENT_EXTRA_CLIPTEXT);
            View cardView = getCardView();
            initCard(cardView);
            handleClipText(mClipText);
        } else {
            mIgnoreIntent = false;
        }
        return START_NOT_STICKY;
    }

    @Override
    protected void onBubbleCreated(View bubbleView) {
        super.onBubbleCreated(bubbleView);
        bubbleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToState(STATE_CARD);
            }
        });
    }

    @NonNull
    @Override
    protected WindowManager.LayoutParams getBubbleParams() {
        WindowManager.LayoutParams params = super.getBubbleParams();
        params.windowAnimations = R.style.BubbleAnimations;
        return params;
    }

    @NonNull
    @Override
    protected WindowManager.LayoutParams getCardParams() {
        WindowManager.LayoutParams params = super.getCardParams();
        params.windowAnimations = R.style.CardAnimations;
        return params;
    }

    /**
     * sets up touch listeners for the popup and card so that touch outside the
     * card dismisses the popup and touch on the card consumes the touch
     */
    private void initCard(View root) {
        ViewGroup popup = (ViewGroup) root.findViewById(R.id.popup);

        popup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent me) {
                int action = me.getAction();
                switch (action) {
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        onCloseDialogs();
                        v.performClick();
                    default:
                        break;
                }
                return true;
            }
        });
        mCardHandler = new DefineCardHandler(this, root, this);
        View copyButton = root
                .findViewById((R.id.iv_define_icon));
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIgnoreIntent = true;
                clipText(mCardHandler.extractTextFromCard());
                onCloseDialogs();
            }
        });

        PopupRoot popupRoot = (PopupRoot) popup;
        popupRoot.registerOnCloseDialogsListener(this);
    }

    /**
     * Function that is called by the ClipboardService when a text is copied
     *
     * @param clipText
     *            text that is copied into the clipboard
     */
    public void handleClipText(String clipText) {
        String[] words = clipText.split(" ");

        if (words.length > 1) {
            mCardHandler.showBubbles(words);
        } else {
            new MeaningsTask().execute(words[0]);
        }
    }

    @Override
    public void onCloseDialogs() {
        goToState(STATE_WAITING);
        int animTime = getResources().getInteger(R.integer.default_anim_time);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        }, animTime);
    }

    @Override
    public void onSelectedTextChanged(String selectedText) {
        if(!selectedText.equals(mWordForm)) {
            new MeaningsTask().execute(selectedText);
        }
    }

    private void clipText(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("RECOPY", text);
        clipboardManager.setPrimaryClip(clip);
        // Give some feedback
    }

    private void onResultListUpdated(String wordForm, ArrayList<DictResult> results){
        mWordForm = wordForm;
        mCardHandler.showMeanings(wordForm, results);
    }

    private class MeaningsTask extends AsyncTask<String, Integer, ArrayList<DictResult>> {
        private String wordForm;
        @Override
        protected ArrayList<DictResult> doInBackground(String... params) {
            wordForm = params[0];
            return mDictionary.getMeanings(wordForm);
        }

        @Override
        protected void onPostExecute(ArrayList<DictResult> results) {
            onResultListUpdated(wordForm, results);
        }
    }
}
