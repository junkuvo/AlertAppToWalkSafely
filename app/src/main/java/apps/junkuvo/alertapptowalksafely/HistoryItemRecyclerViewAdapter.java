package apps.junkuvo.alertapptowalksafely;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.stkent.amplify.utils.StringUtils;

import apps.junkuvo.alertapptowalksafely.models.HistoryItemModel;
import apps.junkuvo.alertapptowalksafely.utils.DateUtil;
import apps.junkuvo.alertapptowalksafely.utils.RealmUtil;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;


/**
 *
 */
public class HistoryItemRecyclerViewAdapter extends RealmRecyclerViewAdapter<HistoryItemModel, HistoryItemViewHolder> {

    private final OrderedRealmCollection<HistoryItemModel> mValues;
    private final HistoryItemFragment.OnListFragmentInteractionListener mListener;
    private Context context;

    public HistoryItemRecyclerViewAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<HistoryItemModel> data, boolean autoUpdate, HistoryItemFragment.OnListFragmentInteractionListener listener) {
        super(context, data, autoUpdate);
        this.mValues = data;
        this.mListener = listener;
        this.context = context;
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
        holder.etMemo.setText(mValues.get(position).getMemo());
        if (StringUtils.isNotBlank(mValues.get(position).getMemo())) {
            holder.ivEdit.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        } else {
            holder.ivEdit.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);
        }

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
                updateEditMode(isExpanded, position);
            }
        });

        holder.btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEditMode(isExpanded, position);
            }
        });

        holder.btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RealmUtil.updateHistoryMemoAsync(Realm.getDefaultInstance(), (long) holder.ivDelete.getTag(), holder.etMemo.getText().toString()
                        , new RealmUtil.realmTransactionCallbackListener() {
                            @Override
                            public void OnSuccess() {
                                updateEditMode(isExpanded, position);
                                Snackbar.make(v, "保存しました", Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void OnError() {
                                Snackbar.make(v, "エラーが発生しました", Snackbar.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    private void updateEditMode(boolean isExpanded, int position) {
        mExpandedPosition = isExpanded ? INITIAL_VALUE : position;
        // アニメーション入れたら邪魔だったので、コメントアウト
//                TransitionManager.beginDelayedTransition(mRecyclerView);
        notifyDataSetChanged();
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
