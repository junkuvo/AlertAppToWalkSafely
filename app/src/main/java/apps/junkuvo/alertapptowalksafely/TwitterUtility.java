package apps.junkuvo.alertapptowalksafely;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TwitterUtility {
    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    private static final String PREF_NAME = "twitter_access_token";
    
    /**
     * Twitterインスタンスを取得します。アクセストークンが保存されていれば自動的にセットします。
     *
     * @param context
     * @return
     */
    public static Twitter getTwitterInstance(Context context) {
        String consumerKey = context.getString(R.string.twitter_consumer_key);
        String consumerSecret = context.getString(R.string.twitter_consumer_secret);

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);

        if (hasAccessToken(context)) {
            twitter.setOAuthAccessToken(loadAccessToken(context));
        }
        return twitter;
    }

    private static SecretKeySpec sKey;// = new SecretKeySpec(TWITTER_ENCRYPT_KEY.getBytes(), "AES");// default : utf-8

    /**
     * アクセストークンをプリファレンスに保存します。
     *
     * @param context
     * @param accessToken
     */
    public static void storeAccessToken(Context context, AccessToken accessToken) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String token;
        String tokenSecret;
        SecretKeySpec key = new SecretKeySpec(context.getString(R.string.twitter_encrypt_key).getBytes(), "AES");
        try {
            token = Utility.stringEncrypt(key, accessToken.getToken());
            tokenSecret = Utility.stringEncrypt(key, accessToken.getTokenSecret());
            editor.putString(TOKEN, token);
            editor.putString(TOKEN_SECRET, tokenSecret);
            editor.commit();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * アクセストークンをプリファレンスから読み込みます。
     *
     * @param context
     * @return
     */
    public static AccessToken loadAccessToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN, null);
        String tokenSecret = preferences.getString(TOKEN_SECRET, null);
        SecretKeySpec key = new SecretKeySpec(context.getString(R.string.twitter_encrypt_key).getBytes(), "AES");
        if (token != null && tokenSecret != null) {
            String tokenDecrypt;
            String tokenSecretDecrypt;
            try {
                tokenDecrypt = Utility.stringDecrypt(key, Base64.decode(token, Base64.DEFAULT));
                tokenSecretDecrypt = Utility.stringDecrypt(key, Base64.decode(tokenSecret, Base64.DEFAULT));
            }catch (Exception ex){
                ex.printStackTrace();
                return null;
            }

            return new AccessToken(tokenDecrypt, tokenSecretDecrypt);
        } else {
            return null;
        }
    }

    /**
     * アクセストークンが存在する場合はtrueを返します。
     *
     * @return
     */
    public static boolean hasAccessToken(Context context) {
        return loadAccessToken(context) != null;
    }
}
