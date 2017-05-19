package apps.junkuvo.alertapptowalksafely;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
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
        setHasStableIds(true);
    }

    private RecyclerView mRecyclerView;


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public HistoryItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_historyitem, parent, false);
        return new HistoryItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final HistoryItemViewHolder holder, final int position) {
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

//        http://stackoverflow.com/questions/27203817/recyclerview-expand-collapse-items
        final boolean isExpanded = position == mExpandedPosition;
        holder.editViews.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);
        holder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? INITIAL_VALUE : position;
//                TransitionManager.beginDelayedTransition(mRecyclerView);
                notifyDataSetChanged();
            }
        });

    }

    private static final int INITIAL_VALUE = -1;
    private int mExpandedPosition = INITIAL_VALUE;

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public long getItemId(int position) {
        return mValues.get(position).getId();
    }

    public OrderedRealmCollection<HistoryItemModel> getmValues() {
        return mValues;
    }
}
