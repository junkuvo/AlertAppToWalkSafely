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

    // FIXME MEMOに依存している、こういうのDIで解決できる？
    public static void updateHistoryMemo(Realm realm, final RealmResults realmResults, final String body) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // This will create a new object in Realm or throw an exception if the
                // object already exists (same primary key)
                // realm.copyToRealm(obj);
                ((HistoryItemModel) realmResults.get(0)).setMemo(body);

                // This will update an existing object with the same primary key
                // or create a new object if an object with no primary key = 42
                realm.copyToRealmOrUpdate(realmResults);
            }
        });
    }

    // FIXME MEMOに依存している、こういうのDIで解決できる？
    public static void updateHistoryMemoAsync(Realm realm, final long id, final String body, final realmTransactionCallbackListener realmTransactionCallbackListener) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // This will create a new object in Realm or throw an exception if the
                // object already exists (same primary key)
                // realm.copyToRealm(obj);
                RealmResults realmResults = RealmUtil.selectHistoryItemById(Realm.getDefaultInstance(), id);
                if (realmResults.size() == 1) {
                    ((HistoryItemModel) realmResults.get(0)).setMemo(body);

                    // This will update an existing object with the same primary key
                    // or create a new object if an object with no primary key = 42
                    realm.copyToRealmOrUpdate(realmResults);
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (realmTransactionCallbackListener != null) {
                    realmTransactionCallbackListener.OnSuccess();
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
                if (realmTransactionCallbackListener != null) {
                    realmTransactionCallbackListener.OnError();
                }
            }
        });
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
