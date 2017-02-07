package apps.junkuvo.alertapptowalksafely;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import apps.junkuvo.alertapptowalksafely.models.HistoryItemModel;
import apps.junkuvo.alertapptowalksafely.utils.DateUtil;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;


/**
 *
 */
public class HistoryItemRecyclerViewAdapter extends RealmRecyclerViewAdapter<HistoryItemModel, HistoryItemViewHolder> {

    private final OrderedRealmCollection<HistoryItemModel> mValues;
    private final HistoryItemFragment.OnListFragmentInteractionListener mListener;

    public HistoryItemRecyclerViewAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<HistoryItemModel> data, boolean autoUpdate, HistoryItemFragment.OnListFragmentInteractionListener listener) {
        super(context, data, autoUpdate);
        this.mValues = data;
        this.mListener = listener;
    }

    @Override
    public HistoryItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_historyitem, parent, false);
        return new HistoryItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final HistoryItemViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mStepCountView.setText(mValues.get(position).getStepCount());
        holder.mStepCountAlertView.setText(mValues.get(position).getStepCountAlert());
        holder.mStartDateTimeView.setText(DateUtil.convertDateToString(mValues.get(position).getStartDateTime(), DateUtil.DATE_FORMAT.HHmmss));
        holder.mEndDateTimeView.setText(DateUtil.convertDateToString(mValues.get(position).getEndDateTime(), DateUtil.DATE_FORMAT.HHmmss));

        holder.tvTitle.setText(DateUtil.convertDateToString(mValues.get(position).getStartDateTime(), DateUtil.DATE_FORMAT.YYYYMMDD));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
        holder.ivDelete.setTag(mValues.get(position).getId());

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDeleteButtonClick(v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
