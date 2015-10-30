package com.example.okubo.onsenkensaku;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class ActivityShowOnsenInfo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onsen_detail);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        OnsenData data = ActivitySearchOnsen.sOnsens[Integer.parseInt(id)];

        ((TextView)findViewById(R.id.lblNameData)).setText(data.getName());
        ((TextView)findViewById(R.id.lblKanaData)).setText(data.getKana());
        ((TextView)findViewById(R.id.lblAddressData)).setText(data.getAddress());
        ((TextView)findViewById(R.id.lblTelData)).setOnClickListener(mClickListenerTel);

        SpannableString content = new SpannableString(data.getTel());
        content.setSpan(new UnderlineSpan(), 0, data.getTel().length(), 0);
        ((TextView) findViewById(R.id.lblTelData)).setText(content);

        ((TextView)findViewById(R.id.lblPriceData)).setText(data.getPrice());
        ((TextView)findViewById(R.id.lblCloseDayData)).setText(data.getClose_day());
        ((TextView)findViewById(R.id.lblOpenHourData)).setText(data.getOpen_hour());
        ((TextView)findViewById(R.id.lblSpringQualityData)).setText(data.getSpring_quality());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                finish();
                return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener mClickListenerTel = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String tel = ((TextView) v).getText().toString().split("\\／")[0];

            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tel));
            startActivity(intent);
        }
    };
}
