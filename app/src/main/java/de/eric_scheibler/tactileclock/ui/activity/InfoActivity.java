package de.eric_scheibler.tactileclock.ui.activity;

import android.os.Build;
import android.os.Bundle;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;

import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import de.eric_scheibler.tactileclock.BuildConfig;
import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.ui.activity.AbstractActivity;


public class InfoActivity extends AbstractActivity {

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(
                getResources().getString(R.string.infoActivityTitle));

        TextView labelApplicationVersion = (TextView) findViewById(R.id.labelApplicationVersion);
        labelApplicationVersion.setText(
                String.format(
                    "%1$s%2$s",
                    getResources().getString(R.string.labelApplicationVersion),
                    settingsManagerInstance.getApplicationVersion())
                );

        TextView labelEmailAddress = (TextView) findViewById(R.id.labelEmailAddress);
        labelEmailAddress.setText(
                String.format(
                    getResources().getString(R.string.labelEmailAddress),
                    BuildConfig.CONTACT_EMAIL_ADDRESS)
                );

        TextView labelWebsite = (TextView) findViewById(R.id.labelWebsite);
        labelWebsite.setMovementMethod(LinkMovementMethod.getInstance());
        labelWebsite.setText(
                fromHtml(
                    String.format(
                        getResources().getString(R.string.labelWebsite),
                        BuildConfig.PROJECT_WEBSITE)
                    )
                );
    }

    @SuppressWarnings("deprecation")
    private static Spanned fromHtml(String html){
        if (html == null) {
            return new SpannableString("");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

}
