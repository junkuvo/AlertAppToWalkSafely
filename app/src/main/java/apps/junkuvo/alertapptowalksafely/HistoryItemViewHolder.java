package apps.junkuvo.alertapptowalksafely;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import apps.junkuvo.alertapptowalksafely.models.HistoryItemModel;

public class HistoryItemViewHolder extends RecyclerView.ViewHolder {

    public final View mView;
    public final TextView mStepCountView;
    public final TextView mStepCountAlertView;
    public final TextView mStartDateTimeView;
    public final TextView mEndDateTimeView;
    public HistoryItemModel mItem;

    public HistoryItemViewHolder(View view) {
        super(view);
        mView = view;
        mStepCountView = (TextView) view.findViewById(R.id.step_count);
        mStepCountAlertView = (TextView) view.findViewById(R.id.step_count_alert);
        mStartDateTimeView = (TextView) view.findViewById(R.id.start_time);
        mEndDateTimeView = (TextView) view.findViewById(R.id.end_time);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mStartDateTimeView.getText() + "'";
    }

}
