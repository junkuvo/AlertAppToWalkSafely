package apps.junkuvo.alertapptowalksafely;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;

import apps.junkuvo.alertapptowalksafely.utils.RealmUtil;
import io.realm.RealmObject;

public class HistoryActivity extends AbstractActivity implements HistoryItemFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public void onListFragmentInteraction(RealmObject item) {

    }

    @Override
    public void onDeleteButtonClick(View view) {
        final long id = (long) view.getTag();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(this.getString(R.string.delete));
        alertDialog.setIcon(R.drawable.ic_delete_deep_455a64_24dp);
        alertDialog.setMessage(this.getString(R.string.dialog_messge_delete));
        alertDialog.setPositiveButton(this.getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RealmUtil.deleteHistoryItem(realm, id);
            }
        });
        alertDialog.setNegativeButton(this.getString(R.string.dialog_button_cancel), null);
        alertDialog.show();
    }
}
