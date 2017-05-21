package apps.junkuvo.alertapptowalksafely;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import apps.junkuvo.alertapptowalksafely.models.HistoryItemModel;
import apps.junkuvo.alertapptowalksafely.utils.RealmUtil;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class HistoryItemFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private HistoryItemViewHolder tappedHistoryItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HistoryItemFragment() {
    }

    @SuppressWarnings("unused")
    public static HistoryItemFragment newInstance(int columnCount) {
        HistoryItemFragment fragment = new HistoryItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historyitem_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            mLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(mLayoutManager);
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            RealmResults<HistoryItemModel> realmObjects = RealmUtil.selectAllHistoryItemAsync(Realm.getDefaultInstance(), "endDateTime", Sort.DESCENDING);
            mHelper.attachToRecyclerView(recyclerView);
            HistoryItemRecyclerViewAdapter historyItemRecyclerViewAdapter = new HistoryItemRecyclerViewAdapter(getContext(), realmObjects, true, mListener);
            historyItemRecyclerViewAdapter.registerAdapterDataObserver(adapterDataObserver);
            recyclerView.setAdapter(historyItemRecyclerViewAdapter);
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(RealmObject item);

        void onDeleteButtonClick(View view);
    }

    /**
     * RecyclerViewのSwipe実現用
     */
    private ItemTouchHelper mHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//            final int fromPos = viewHolder.getAdapterPosition();
//            final int toPos = target.getAdapterPosition();
//            adapter.notifyItemMoved(fromPos, toPos);
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int fromPos = viewHolder.getAdapterPosition();
            RealmResults<HistoryItemModel> realmResults = RealmUtil.selectHistoryItemById(Realm.getDefaultInstance(), (long) ((HistoryItemViewHolder) viewHolder).ivDelete.getTag());
            RealmUtil.copyToRealmObject(realmResults.get(0), historyItemModel);
            RealmUtil.deleteHistoryItem(Realm.getDefaultInstance(), (long) ((HistoryItemViewHolder) viewHolder).ivDelete.getTag());
            recyclerView.getAdapter().notifyItemRemoved(fromPos);
        }
    });

    /**
     * スワイプで削除されたアイテムを元に戻すために一時保持しておく
     */
    private HistoryItemModel historyItemModel = new HistoryItemModel();

    private RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {

        @Override
        public void onItemRangeRemoved(final int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            Snackbar snackbar = Snackbar.make(recyclerView, R.string.delete_complete, Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            snackbar.setAction(getString(R.string.delete_undo), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RealmUtil.insertHistoryItemAsync(Realm.getDefaultInstance(), historyItemModel, null);
                    recyclerView.getAdapter().notifyDataSetChanged();
//                    recyclerView.getAdapter().notifyItemInserted(positionStart);
                }
            });
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);

                }

                @Override
                public void onShown(Snackbar sb) {
                    super.onShown(sb);
                }
            });
            snackbar.show();
        }
    };
}
