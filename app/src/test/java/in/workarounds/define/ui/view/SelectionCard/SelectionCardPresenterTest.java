package in.workarounds.define.ui.view.SelectionCard;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import in.workarounds.define.helper.ContextHelper;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * Created by manidesto on 30/11/15.
 */
public class SelectionCardPresenterTest {
    @Mock SelectionCardController controller;
    @Mock SelectionCardView selectionCardView;
    @Mock ContextHelper contextHelper;
    SelectionCardPresenter presenter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        presenter = new SelectionCardPresenter(controller, contextHelper);
    }

    @Test
    public void testOnWordSelected() throws Exception {
        String word = "something";
        presenter.onWordSelected(word);
        verify(controller).onWordSelected(word);
    }

    @Test
    public void testOnClipTextChanged() throws Exception {
        presenter.addView(selectionCardView);

        String oneWord = "one";
        String twoWords = "one two";
        String twoWordsWithSpace = "one two ";
        String text = "one two three four";

        //ONE WORD
        presenter.onClipTextChanged(oneWord);
        //verify setTextForSelection
        verify(selectionCardView).setTextForSelection(oneWord);
        //verify selectAll
        verify(selectionCardView).selectAll();

        reset(selectionCardView);

        //TWO WORDS
        presenter.onClipTextChanged(twoWords);
        verify(selectionCardView).setTextForSelection(twoWords);
        verify(selectionCardView).selectAll();

        reset(selectionCardView);

        //TWO WORDS With Space
        presenter.onClipTextChanged(twoWordsWithSpace);
        verify(selectionCardView).setTextForSelection(twoWords);
        verify(selectionCardView).selectAll();

        reset(selectionCardView);

        //FOUR WORDS
        presenter.onClipTextChanged(text);
        verify(selectionCardView).setTextForSelection(text);
        verify(selectionCardView, never()).selectAll();
    }

    @Test
    public void testAddViewFirst() throws Exception {
        String clipText = "The whole cliptext";
        presenter.addView(selectionCardView);
        verify(selectionCardView, never()).setTextForSelection(anyString());
        presenter.onClipTextChanged(clipText);
        verify(selectionCardView).setTextForSelection(clipText);
    }

    @Test
    public void testAddViewLater() throws Exception {
        String clipText = "The whole cliptext";
        presenter.onClipTextChanged(clipText);
        presenter.addView(selectionCardView);
        verify(selectionCardView).setTextForSelection(clipText);
    }

    @Test
    public void testRemoveView() throws Exception {
        //First add the view and verify added
        String clipText = "The whole cliptext";
        presenter.addView(selectionCardView);
        presenter.onClipTextChanged(clipText);
        verify(selectionCardView).setTextForSelection(clipText);

        reset(selectionCardView);

        //Verify removal
        presenter.removeView();
        presenter.onClipTextChanged(clipText);
        verify(selectionCardView, never()).setTextForSelection(clipText);
    }

    @Test
    public void testOnDefineClicked() throws Exception {
        presenter.onDefineClicked();
        verify(contextHelper).openDefineApp();
        verify(controller).onButtonClicked();
    }

    @Test
    public void testOnSearchClicked() throws Exception {
        String clipText = "The whole cliptext";
        String selected = "cliptext";

        presenter.onClipTextChanged(clipText);
        presenter.onSearchClicked();
        verifySearchClicked(clipText);

        resetAllMocks();

        presenter.onWordSelected(selected);
        presenter.onSearchClicked();
        verifySearchClicked(selected);

        resetAllMocks();

        presenter.onClipTextChanged(clipText);
        presenter.onSearchClicked();
        verifySearchClicked(clipText);
    }

    @Test
    public void testOnWikiClicked() throws Exception {
        String clipText = "The whole cliptext";
        String selected = "cliptext";

        presenter.onClipTextChanged(clipText);
        presenter.onWikiClicked();
        verifyWikiClicked(clipText);

        resetAllMocks();

        presenter.onWordSelected(selected);
        presenter.onWikiClicked();
        verifyWikiClicked(selected);

        resetAllMocks();

        presenter.onClipTextChanged(clipText);
        presenter.onWikiClicked();
        verifyWikiClicked(clipText);
    }

    @Test
    public void testOnShareClicked() throws Exception {
        String clipText = "The whole cliptext";
        String selected = "cliptext";

        presenter.onClipTextChanged(clipText);
        presenter.onShareClicked();
        verifyShareClicked(clipText);

        resetAllMocks();

        presenter.onWordSelected(selected);
        presenter.onShareClicked();
        verifyShareClicked(selected);

        resetAllMocks();

        presenter.onClipTextChanged(clipText);
        presenter.onShareClicked();
        verifyShareClicked(clipText);
    }

    @Test
    public void testOnCopyClicked() throws Exception {
        String clipText = "The whole cliptext";
        String selected = "cliptext";

        presenter.onClipTextChanged(clipText);
        presenter.onCopyClicked();
        verifyNotCopied(clipText);

        resetAllMocks();

        presenter.onWordSelected(selected);
        presenter.onCopyClicked();
        verifyCopied(selected);

        resetAllMocks();

        presenter.onClipTextChanged(clipText);
        presenter.onCopyClicked();
        verifyNotCopied(clipText);
    }

    @Test
    public void testOnSettingsClicked() throws Exception {
        presenter.onSettingsClicked();
        verify(contextHelper).openSettings();
        verify(controller).onButtonClicked();
    }

    private void resetAllMocks() {
        reset(contextHelper);
        reset(controller);
        reset(selectionCardView);
    }

    private void verifySearchClicked(String text) {
        verify(contextHelper).searchWeb(text);
        verify(controller).onButtonClicked();
    }

    private void verifyWikiClicked(String text) {
        verify(contextHelper).searchWiki(text);
        verify(controller).onButtonClicked();
    }

    private void verifyCopied(String text) {
        verify(contextHelper).copyToClipboard(text);
        verify(controller).onButtonClicked();
    }

    private void verifyNotCopied(String text) {
        verify(contextHelper, never()).copyToClipboard(text);
        verify(controller).onButtonClicked();
    }

    private void verifyShareClicked(String text) {
        verify(contextHelper).sharePlainText(text);
        verify(controller).onButtonClicked();
    }
}