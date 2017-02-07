package apps.junkuvo.alertapptowalksafely;

import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import apps.junkuvo.alertapptowalksafely.models.HistoryItemModel;

public class HistoryItemViewHolder extends RecyclerView.ViewHolder {

    public final View mView;
    public final AppCompatTextView tvTitle;
    public final AppCompatTextView mStepCountView;
    public final AppCompatTextView mStepCountAlertView;
    public final AppCompatTextView mStartDateTimeView;
    public final AppCompatTextView mEndDateTimeView;
    public final AppCompatImageButton ivDelete;
    public HistoryItemModel mItem;

    public HistoryItemViewHolder(View view) {
        super(view);
        mView = view;
        tvTitle = (AppCompatTextView) view.findViewById(R.id.txtTitle);
        mStepCountView = (AppCompatTextView) view.findViewById(R.id.step_count);
        mStepCountAlertView = (AppCompatTextView) view.findViewById(R.id.step_count_alert);
        mStartDateTimeView = (AppCompatTextView) view.findViewById(R.id.start_time);
        mEndDateTimeView = (AppCompatTextView) view.findViewById(R.id.end_time);
        ivDelete = (AppCompatImageButton) view.findViewById(R.id.ivDelete);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mStartDateTimeView.getText() + "'";
    }

}
