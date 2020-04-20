package education.mahmoud.quranyapp.utils.testsomefeature;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import education.mahmoud.quranyapp.model.Aya;
import education.mahmoud.quranyapp.model.Sura;

public class test {

    private static final String TAG = "test";

    public static void main(String[] args) {

    }

    public void parseXML(Context contex) {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = contex.getAssets().open("quran_simple_with_num.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            processParsing(parser);

        } catch (XmlPullParserException e) {

        } catch (IOException e) {
        }
    }

    private void processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<Sura> players = new ArrayList<>();
        int eventType = parser.getEventType();
        Sura currentPlayer = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String eltName = null;

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();
                    if ("sura".equals(eltName)) {
                        currentPlayer = new Sura();
                        players.add(currentPlayer);
                    } /*else if (currentPlayer != null) {
                        if ("name".equals(eltName)) {
                            currentPlayer.name = parser.nextText();
                        } else if ("age".equals(eltName)) {
                            currentPlayer.age = parser.nextText();
                        } else if ("position".equals(eltName)) {
                            currentPlayer.position = parser.nextText();
                        }
                    }*/
                    break;
            }

            eventType = parser.next();
        }

        printPlayers(players);
    }

    private void printPlayers(ArrayList<Sura> players) {
        StringBuilder builder = new StringBuilder();

        for (Sura player : players) {
            Log.d(TAG, "printPlayers: ");
            for (Aya aya : player.getAyahs()) {
                Log.d(TAG, "printPlayers: " + aya.getText());
            }
        }


    }

}
