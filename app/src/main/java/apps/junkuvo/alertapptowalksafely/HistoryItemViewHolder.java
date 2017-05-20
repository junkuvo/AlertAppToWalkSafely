package apps.junkuvo.alertapptowalksafely;

import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import apps.junkuvo.alertapptowalksafely.models.HistoryItemModel;

class HistoryItemViewHolder extends RecyclerView.ViewHolder {

    final View mView;
    final AppCompatTextView tvTitle;
    final AppCompatTextView mStepCountView;
    final AppCompatTextView mStepCountAlertView;
    final AppCompatTextView mStartDateTimeView;
    final AppCompatTextView mEndDateTimeView;
    final AppCompatImageButton ivDelete;
    final AppCompatImageButton ivEdit;
    final ViewGroup editViews;
    HistoryItemModel mItem;
    AppCompatButton btSave;
    AppCompatButton btCancel;
    AppCompatEditText etMemo;

    public HistoryItemViewHolder(View view) {
        super(view);
        mView = view;
        tvTitle = (AppCompatTextView) view.findViewById(R.id.txtTitle);
        mStepCountView = (AppCompatTextView) view.findViewById(R.id.step_count);
        mStepCountAlertView = (AppCompatTextView) view.findViewById(R.id.step_count_alert);
        mStartDateTimeView = (AppCompatTextView) view.findViewById(R.id.start_time);
        mEndDateTimeView = (AppCompatTextView) view.findViewById(R.id.end_time);
        ivDelete = (AppCompatImageButton) view.findViewById(R.id.ivDelete);
        editViews = (ViewGroup) view.findViewById(R.id.edit_views);
        ivEdit = (AppCompatImageButton) view.findViewById(R.id.ivEdit);
        btSave = (AppCompatButton) view.findViewById(R.id.bt_save);
        btCancel = (AppCompatButton) view.findViewById(R.id.bt_cancel);
        etMemo = (AppCompatEditText) view.findViewById(R.id.et_memo);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mStartDateTimeView.getText() + "'";
    }
}
