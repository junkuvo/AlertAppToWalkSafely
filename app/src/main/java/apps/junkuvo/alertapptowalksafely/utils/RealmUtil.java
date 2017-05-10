package apps.junkuvo.alertapptowalksafely.utils;

import apps.junkuvo.alertapptowalksafely.models.HistoryItemModel;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class RealmUtil {

    public static void insertHistoryItem(Realm realm, final HistoryItemModel historyItemModel) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
//                realm.copyToRealm(historyItemModel);
                copyToRealmObject(historyItemModel, realm.createObject(HistoryItemModel.class, System.currentTimeMillis()));
            }
        });
    }

    public static void insertHistoryItemAsync(final Realm realm, final HistoryItemModel historyItemModel, final realmTransactionCallbackListener realmTransactionCallbackListener) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                HistoryItemModel realmHistoryItem = bgRealm.createObject(HistoryItemModel.class, System.currentTimeMillis());
                copyToRealmObject(historyItemModel, realmHistoryItem);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (realmTransactionCallbackListener != null) {
                    realmTransactionCallbackListener.OnSuccess();
                }
//                // Transaction was a success.
//                // FIXME : これはここでいいのか？
//                realm.close();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
                if (realmTransactionCallbackListener != null) {
                    realmTransactionCallbackListener.OnError();
                }
//                // Transaction failed and was automatically canceled.
//                // FIXME : これはここでいいのか？
//                realm.close();
            }
        });
    }

    public static RealmResults<HistoryItemModel> selectAllHistoryItem(Realm realm) {
        RealmResults<HistoryItemModel> realmResults = null;
        try {
            realmResults = realm.where(HistoryItemModel.class).findAll()
                    .sort("id", Sort.ASCENDING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realmResults;
    }

    public static RealmResults<HistoryItemModel> selectAllHistoryItemAsync(Realm realm, String key, Sort sort) {
        RealmResults<HistoryItemModel> realmResults = null;
        try {
            realmResults = realm.where(HistoryItemModel.class).findAllAsync()
                    .sort(key, sort);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realmResults;
    }

    public static RealmResults<HistoryItemModel> selectHistoryItemById(Realm realm, long id) {
        RealmResults<HistoryItemModel> realmResults = null;
        try {
            realmResults = realm.where(HistoryItemModel.class).equalTo("id", id).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realmResults;
    }

    public static void copyToRealmObject(HistoryItemModel from, HistoryItemModel to) {
        to.setStartDateTime(from.getStartDateTime());
        to.setEndDateTime(from.getEndDateTime());
        to.setStepCount(from.getStepCount());
        to.setStepCountAlert(from.getStepCountAlert());
    }

    public static void deleteHistoryItem(Realm realm, long id) {
        final RealmResults<HistoryItemModel> historyItemModels = selectHistoryItemById(realm, id);
        // All changes to data must happen in a transaction
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // remove single match
                historyItemModels.deleteFirstFromRealm();
            }
        });
    }

    public interface realmTransactionCallbackListener {
        void OnSuccess();

        void OnError();
    }
}
